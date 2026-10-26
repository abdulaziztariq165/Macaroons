package com.alphaxk.recoilpad

import android.os.Handler
import android.os.Looper

object RecoilEngine {
    private val handler = Handler(Looper.getMainLooper())
    private var running = false

    fun startFire(x: Float, y: Float) {
        ShizukuBridge.injectTap(x, y)
    }

    fun startRecoil(centerX: Float, centerY: Float) {
        if (running) return
        running = true
        val strength = Prefs.recoilStrength
        val duration = Prefs.recoilDurationMs
        val steps = (duration / 16).coerceAtLeast(1)
        val deltaPerStep = (strength * 8f) / steps

        var step = 0
        val runnable = object : Runnable {
            override fun run() {
                if (!running || step >= steps) {
                    running = false
                    return
                }
                val targetY = centerY + deltaPerStep * (step + 1)
                ShizukuBridge.injectSwipe(centerX, centerY, centerX, targetY, 16)
                step++
                handler.postDelayed(this, 16)
            }
        }
        handler.post(runnable)
    }

    fun stopRecoil() {
        running = false
        handler.removeCallbacksAndMessages(null)
    }

    fun fireAndRecoil(x: Float, y: Float, centerX: Float, centerY: Float) {
        when (Prefs.mode) {
            0 -> { // hold
                startFire(x, y)
                startRecoil(centerX, centerY)
            }
            1 -> { // single
                startFire(x, y)
                handler.postDelayed({ startRecoil(centerX, centerY) }, 50)
            }
            2 -> { // burst
                val count = Prefs.burstCount
                val interval = Prefs.burstIntervalMs.toLong()
                for (i in 0 until count) {
                    handler.postDelayed({
                        startFire(x, y)
                        startRecoil(centerX, centerY)
                    }, i * interval)
                }
            }
        }
    }
}
