package com.alphaxk.recoilpad

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock

/**
 * أصابع مُحقنة: 0 = المنظار، 1 = الإطلاق، 2 = التصويب (السحب للأسفل).
 * كلها تُنفَّذ على خيط واحد بالترتيب.
 */
object RecoilEngine {
    private const val SCOPE = 0
    private const val FIRE = 1
    private const val AIM = 2

    private val thread = HandlerThread("recoil-input").apply { start() }
    private val h = Handler(thread.looper)

    private var active = false
    private var aimOn = false
    private var t0 = 0L
    private var ax = 0f
    private var curY = 0f
    private var maxY = 0f

    private val tick = object : Runnable {
        override fun run() {
            if (!active) return
            val t = SystemClock.uptimeMillis() - t0
            val delay = Prefs.delayMs
            if (t >= delay) {
                if (!aimOn) {
                    ShizukuBridge.down(AIM, ax, curY)
                    aimOn = true
                }
                if (t - delay <= Prefs.durationMs) {
                    curY = minOf(curY + Prefs.strength / 10f, maxY)
                    ShizukuBridge.move(AIM, ax, curY)
                }
            }
            h.postDelayed(this, 16)
        }
    }

    fun press(fireX: Float, fireY: Float, aimX: Float, aimY: Float, screenH: Float) {
        h.post {
            if (active) return@post
            if (!ShizukuBridge.isReady() && !ShizukuBridge.init()) return@post
            active = true
            aimOn = false
            ax = aimX
            curY = aimY
            maxY = screenH - 2f
            t0 = SystemClock.uptimeMillis()
            ShizukuBridge.down(FIRE, fireX, fireY)
            h.postDelayed(tick, 16)
        }
    }

    fun release() {
        h.post {
            if (!active) return@post
            active = false
            h.removeCallbacks(tick)
            if (aimOn) ShizukuBridge.up(AIM)
            ShizukuBridge.up(FIRE)
            aimOn = false
        }
    }

    fun scopeDown(x: Float, y: Float) {
        h.post {
            if (!ShizukuBridge.isReady() && !ShizukuBridge.init()) return@post
            ShizukuBridge.down(SCOPE, x, y)
        }
    }

    fun scopeUp() {
        h.post { ShizukuBridge.up(SCOPE) }
    }
}
