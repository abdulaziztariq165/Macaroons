package com.alphaxk.recoilpad

import android.os.IBinder
import android.os.Parcel
import android.os.SystemClock
import android.view.MotionEvent
import rikka.shizuku.Shizuku

object ShizukuBridge {
    private var inputManager: Any? = null
    private var injectMethod: java.lang.reflect.Method? = null
    private var ready = false

    fun init(): Boolean {
        if (!Shizuku.pingBinder()) return false
        if (Shizuku.isPreV11() || Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return try {
            val smClass = Class.forName("android.os.ServiceManager")
            val getService = smClass.getMethod("getService", String::class.java)
            val binder = getService.invoke(null, "input") as IBinder
            val stubClass = Class.forName("android.hardware.input.IInputManager\$Stub")
            inputManager = stubClass.getMethod("asInterface", IBinder::class.java).invoke(null, binder)
            injectMethod = inputManager!!.javaClass.getMethod(
                "injectInputEvent", android.view.InputEvent::class.java, Int::class.javaPrimitiveType
            )
            ready = true
            true
        } catch (e: Exception) {
            ready = false
            false
        }
    }

    fun isReady() = ready

    fun injectTap(x: Float, y: Float): Boolean {
        if (!ready) return false
        return try {
            val downTime = SystemClock.uptimeMillis()
            val event = MotionEvent.obtain(
                downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0
            )
            injectMethod!!.invoke(inputManager, event, 0)
            event.recycle()

            val upTime = SystemClock.uptimeMillis() + 1
            val up = MotionEvent.obtain(
                downTime, upTime, MotionEvent.ACTION_UP, x, y, 0
            )
            injectMethod!!.invoke(inputManager, up, 0)
            up.recycle()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun injectSwipe(x1: Float, y1: Float, x2: Float, y2: Float, durationMs: Int): Boolean {
        if (!ready) return false
        return try {
            val downTime = SystemClock.uptimeMillis()
            val steps = (durationMs / 16).coerceAtLeast(1)
            val dx = (x2 - x1) / steps
            val dy = (y2 - y1) / steps

            var event = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x1, y1, 0)
            injectMethod!!.invoke(inputManager, event, 0)
            event.recycle()

            for (i in 1..steps) {
                val t = downTime + (durationMs * i / steps)
                event = MotionEvent.obtain(downTime, t, MotionEvent.ACTION_MOVE, x1 + dx * i, y1 + dy * i, 0)
                injectMethod!!.invoke(inputManager, event, 0)
                event.recycle()
            }

            val upTime = downTime + durationMs
            event = MotionEvent.obtain(downTime, upTime, MotionEvent.ACTION_UP, x2, y2, 0)
            injectMethod!!.invoke(inputManager, event, 0)
            event.recycle()
            true
        } catch (e: Exception) {
            false
        }
    }
}
