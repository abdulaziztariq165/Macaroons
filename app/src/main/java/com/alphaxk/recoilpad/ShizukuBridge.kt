package com.alphaxk.recoilpad

import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.view.InputDevice
import android.view.InputEvent
import android.view.MotionEvent
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.Method

/** حقن اللمس عبر Shizuku. إصبعان: 0 = الإطلاق، 1 = التصويب */
object ShizukuBridge {
    private var service: Any? = null
    private var injectMethod: Method? = null
    @Volatile private var ready = false

    private var fx = 0f
    private var fy = 0f
    private var ax = 0f
    private var ay = 0f
    private var downTime = 0L

    fun isReady() = ready

    fun init(): Boolean {
        ready = false
        return try {
            if (!Shizuku.pingBinder()) return false
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) return false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                HiddenApiBypass.addHiddenApiExemptions("L")
            }
            val binder: IBinder = ShizukuBinderWrapper(SystemServiceHelper.getSystemService("input"))
            val stub = Class.forName("android.hardware.input.IInputManager\$Stub")
            service = stub.getMethod("asInterface", IBinder::class.java).invoke(null, binder)
            val iface = Class.forName("android.hardware.input.IInputManager")
            injectMethod = iface.getMethod(
                "injectInputEvent", InputEvent::class.java, Int::class.javaPrimitiveType
            )
            ready = true
            true
        } catch (t: Throwable) {
            false
        }
    }

    private fun send(action: Int, pointers: Int) {
        if (!ready) return
        try {
            val props = Array(pointers) {
                MotionEvent.PointerProperties().apply {
                    id = it
                    toolType = MotionEvent.TOOL_TYPE_FINGER
                }
            }
            val coords = Array(pointers) {
                MotionEvent.PointerCoords().apply {
                    x = if (it == 0) fx else ax
                    y = if (it == 0) fy else ay
                    pressure = 1f
                    size = 1f
                }
            }
            val ev = MotionEvent.obtain(
                downTime, SystemClock.uptimeMillis(), action, pointers, props, coords,
                0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0
            )
            injectMethod!!.invoke(service, ev, 0)
            ev.recycle()
        } catch (t: Throwable) {
            ready = false
        }
    }

    private val shift = 8 // MotionEvent.ACTION_POINTER_INDEX_SHIFT

    fun fireDown(x: Float, y: Float) {
        fx = x; fy = y
        downTime = SystemClock.uptimeMillis()
        send(MotionEvent.ACTION_DOWN, 1)
    }

    fun aimDown(x: Float, y: Float) {
        ax = x; ay = y
        send(MotionEvent.ACTION_POINTER_DOWN or (1 shl shift), 2)
    }

    fun aimMove(y: Float) {
        ay = y
        send(MotionEvent.ACTION_MOVE, 2)
    }

    fun aimUp() = send(MotionEvent.ACTION_POINTER_UP or (1 shl shift), 2)

    fun fireUp() = send(MotionEvent.ACTION_UP, 1)
}
