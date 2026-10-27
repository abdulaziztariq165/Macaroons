package com.alphaxk.recoilpad

import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.os.SystemClock

object RecoilEngine {
    private const val SCOPE = 0
    private const val FIRE = 1
    private const val AIM = 2
    private const val PEEK_LEFT = 3
    private const val PEEK_RIGHT = 4

    private val thread = HandlerThread("recoil-input", Process.THREAD_PRIORITY_URGENT_DISPLAY).apply { start() }
    private val h = Handler(thread.looper)

    private var active = false
    private var aimOn = false
    private var applyCompensation = true
    private var t0 = 0L
    private var ax = 0f
    private var curX = 0f
    private var curY = 0f
    private var maxY = 0f

    private val tick = object : Runnable {
        override fun run() {
            if (!active) return
            val t = SystemClock.uptimeMillis() - t0
            val delay = Prefs.delayMs
            
            if (t >= delay && applyCompensation) {
                if (!aimOn) {
                    ShizukuBridge.down(AIM, curX, curY)
                    aimOn = true
                }
                
                if (t - delay <= Prefs.durationMs) {
                    val progress = (t - delay).toFloat() / Prefs.durationMs.toFloat()
                    val curveMultiplier = Math.pow(1.0 - progress, Prefs.curveExp / 10.0).toFloat()
                    
                    val moveY = (Prefs.strength / 10f) * 1.2f * curveMultiplier
                    curY = minOf(curY + moveY, maxY)

                    val moveX = if (Math.random() > 0.5) 
                        (Prefs.strengthXRight / 10f) * 0.5f * curveMultiplier 
                    else 
                        -(Prefs.strengthXLeft / 10f) * 0.5f * curveMultiplier
                    
                    curX += moveX
                    
                    ShizukuBridge.move(AIM, curX, curY)
                }
            }
            h.postDelayed(this, 8) 
        }
    }

    fun press(fireX: Float, fireY: Float, aimX: Float, aimY: Float, screenH: Float, compensate: Boolean = true) {
        h.post {
            if (active) return@post
            if (!ShizukuBridge.isReady() && !ShizukuBridge.init()) return@post
            active = true
            aimOn = false
            applyCompensation = compensate
            ax = aimX
            curX = aimX
            curY = aimY
            maxY = screenH - 2f
            t0 = SystemClock.uptimeMillis()
            ShizukuBridge.down(FIRE, fireX, fireY)
            
            if (compensate) {
                h.postDelayed(tick, 8)
            }
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

    fun peekDown(id: Int, x: Float, y: Float) {
        h.post {
            if (!ShizukuBridge.isReady() && !ShizukuBridge.init()) return@post
            ShizukuBridge.down(id, x, y)
        }
    }

    fun peekUp(id: Int) {
        h.post { ShizukuBridge.up(id) }
    }

    fun getPeekLeftId() = PEEK_LEFT
    fun getPeekRightId() = PEEK_RIGHT
}
