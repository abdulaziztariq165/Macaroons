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

/** حقن لمس متعدد الأصابع عبر Shizuku. كل الأصابع تُحقن كإيماءة واحدة متصلة. */
object ShizukuBridge {
    private class P(val id: Int, var x: Float, var y: Float)

    private var service: Any? = null
    private var injectMethod: Method? = null
    @Volatile private var ready = false

    private val active = ArrayList<P>()
    private var downTime = 0L
    private val shift = 8 // MotionEvent.ACTION_POINTER_INDEX_SHIFT

    fun isReady() = ready

    fun init(): Boolean {
        if (ready) return true
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
            active.clear()
            ready = true
            true
        } catch (t: Throwable) {
            false
        }
    }

    private fun send(action: Int) {
        if (!ready || active.isEmpty()) return
        try {
            val n = active.size
            val props = Array(n) {
                MotionEvent.PointerProperties().apply {
                    id = active[it].id
                    toolType = MotionEvent.TOOL_TYPE_FINGER
                }
            }
            val coords = Array(n) {
                MotionEvent.PointerCoords().apply {
                    x = active[it].x
                    y = active[it].y
                    pressure = 1f
                    size = 1f
                }
            }
            val ev = MotionEvent.obtain(
                downTime, SystemClock.uptimeMillis(), action, n, props, coords,
                0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0
            )
            injectMethod!!.invoke(service, ev, 0)
            ev.recycle()
        } catch (t: Throwable) {
            ready = false
        }
    }

    fun down(id: Int, x: Float, y: Float) {
        if (!ready) return
        active.removeAll { it.id == id }
        active.add(P(id, x, y))
        if (active.size == 1) {
            downTime = SystemClock.uptimeMillis()
            send(MotionEvent.ACTION_DOWN)
        } else {
            send(MotionEvent.ACTION_POINTER_DOWN or ((active.size - 1) shl shift))
        }
    }

    fun move(id: Int, x: Float, y: Float) {
        val p = active.firstOrNull { it.id == id } ?: return
        p.x = x
        p.y = y
        send(MotionEvent.ACTION_MOVE)
    }

    fun up(id: Int) {
        val idx = active.indexOfFirst { it.id == id }
        if (idx < 0) return
        if (active.size == 1) {
            send(MotionEvent.ACTION_UP)
        } else {
            send(MotionEvent.ACTION_POINTER_UP or (idx shl shift))
        }
        active.removeAt(idx)
    }
}
