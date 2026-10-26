package com.alphaxk.recoilpad

import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock

/**
 * عند الضغط: إصبع 1 يضغط زر الإطلاق في اللعبة،
 * ثم إصبع 2 يبدأ من نقطة التصويب ويسحب للأسفل تدريجياً لتعويض الارتداد.
 */
object RecoilEngine {
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
                    ShizukuBridge.aimDown(ax, curY)
                    aimOn = true
                }
                if (t - delay <= Prefs.durationMs) {
                    curY = minOf(curY + Prefs.strength, maxY)
                    ShizukuBridge.aimMove(curY)
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
            ShizukuBridge.fireDown(fireX, fireY)
            h.postDelayed(tick, 16)
        }
    }

    fun release() {
        h.post {
            if (!active) return@post
            active = false
            h.removeCallbacks(tick)
            if (aimOn) ShizukuBridge.aimUp()
            ShizukuBridge.fireUp()
            aimOn = false
        }
    }
}
