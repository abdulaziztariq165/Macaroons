package com.alphaxk.recoilpad

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView

class OverlayService : Service() {
    companion object {
        @Volatile var running = false
    }

    private lateinit var wm: WindowManager
    private lateinit var ui: Context
    private lateinit var trigger: CircleView
    private lateinit var scopeBtn: CircleView
    private lateinit var fireMarker: CircleView
    private lateinit var aimMarker: CircleView
    private lateinit var scopeMarker: CircleView
    private lateinit var editBtn: Button
    private lateinit var panel: View
    private val params = HashMap<View, WindowManager.LayoutParams>()
    private var editing = false
    private var panelShown = false

    private val baseFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        running = true
        ui = ContextThemeWrapper(this, R.style.Theme_RecoilPad)
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        startAsForeground()

        val dm = resources.displayMetrics
        Prefs.ensureDefaults(maxOf(dm.widthPixels, dm.heightPixels), minOf(dm.widthPixels, dm.heightPixels))
        ShizukuBridge.init()
        buildViews()
    }

    private fun startAsForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel("recoil", "الطبقة العائمة", NotificationManager.IMPORTANCE_LOW)
        )
        val n = Notification.Builder(this, "recoil")
            .setContentTitle("مُعوّض الارتداد")
            .setContentText("الطبقة العائمة تعمل")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, n)
        }
    }

    private fun addWindow(v: View, size: Int, cx: Int, cy: Int, touchable: Boolean) {
        val flags = baseFlags or (if (touchable) 0 else WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        val p = WindowManager.LayoutParams(
            size, size, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags, PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = cx - size / 2
            y = cy - size / 2
            // أندرويد 12+ يحجب اللمس عبر نوافذ شفافيتها أعلى من 0.8
            alpha = 0.7f
        }
        params[v] = p
        wm.addView(v, p)
    }

    private fun setTouchable(v: View, touchable: Boolean) {
        val p = params[v] ?: return
        p.flags = if (touchable) p.flags and WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE.inv()
        else p.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        wm.updateViewLayout(v, p)
    }

    private fun dragHandler(save: (Int, Int) -> Unit): (View, MotionEvent) -> Boolean {
        var ox = 0f
        var oy = 0f
        return { v, e ->
            val p = params[v]!!
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> { ox = e.rawX - p.x; oy = e.rawY - p.y }
                MotionEvent.ACTION_MOVE -> {
                    p.x = (e.rawX - ox).toInt()
                    p.y = (e.rawY - oy).toInt()
                    wm.updateViewLayout(v, p)
                }
                MotionEvent.ACTION_UP -> save(p.x + p.width / 2, p.y + p.height / 2)
            }
            true
        }
    }

    private fun buildViews() {
        val markerSize = Ui.dp(ui, 68)

        fireMarker = CircleView(ui, "زر الإطلاق", Ui.BLUE, false)
        aimMarker = CircleView(ui, "التصويب", Ui.GREEN, false)
        scopeMarker = CircleView(ui, "زر المنظار", Ui.AMBER, false)
        trigger = CircleView(ui, "إطلاق", Ui.RED, true)
        scopeBtn = CircleView(ui, "منظار", Ui.AMBER, true)

        addWindow(fireMarker, markerSize, Prefs.fireX, Prefs.fireY, false)
        addWindow(aimMarker, markerSize, Prefs.aimX, Prefs.aimY, false)
        addWindow(scopeMarker, markerSize, Prefs.scopeX, Prefs.scopeY, false)
        addWindow(trigger, Prefs.triggerSize, Prefs.trigX, Prefs.trigY, true)
        addWindow(scopeBtn, Prefs.triggerSize, Prefs.scBtnX, Prefs.scBtnY, true)
        fireMarker.visibility = View.GONE
        aimMarker.visibility = View.GONE
        scopeMarker.visibility = View.GONE

        val dFire = dragHandler { x, y -> Prefs.fireX = x; Prefs.fireY = y }
        val dAim = dragHandler { x, y -> Prefs.aimX = x; Prefs.aimY = y }
        val dScopeM = dragHandler { x, y -> Prefs.scopeX = x; Prefs.scopeY = y }
        val dTrig = dragHandler { x, y -> Prefs.trigX = x; Prefs.trigY = y }
        val dScopeB = dragHandler { x, y -> Prefs.scBtnX = x; Prefs.scBtnY = y }
        fireMarker.setOnTouchListener { v, e -> dFire(v, e) }
        aimMarker.setOnTouchListener { v, e -> dAim(v, e) }
        scopeMarker.setOnTouchListener { v, e -> dScopeM(v, e) }

        trigger.setOnTouchListener { v, e ->
            if (editing) {
                dTrig(v, e)
            } else {
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN -> RecoilEngine.press(
                        Prefs.fireX.toFloat(), Prefs.fireY.toFloat(),
                        Prefs.aimX.toFloat(), Prefs.aimY.toFloat(),
                        resources.displayMetrics.heightPixels.toFloat()
                    )
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> RecoilEngine.release()
                }
                true
            }
        }

        scopeBtn.setOnTouchListener { v, e ->
            if (editing) {
                dScopeB(v, e)
            } else {
                when (e.actionMasked) {
                    MotionEvent.ACTION_DOWN ->
                        RecoilEngine.scopeDown(Prefs.scopeX.toFloat(), Prefs.scopeY.toFloat())
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> RecoilEngine.scopeUp()
                }
                true
            }
        }

        // زر «ضبط» الصغير
        editBtn = Ui.button(ui, "ضبط", Ui.GRAY, small = true) { setEditing(!editing) }
        val bp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, baseFlags, PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = Ui.dp(ui, 10); y = Ui.dp(ui, 44); alpha = 0.7f
        }
        params[editBtn] = bp
        wm.addView(editBtn, bp)

        // لوحة الإعدادات (تُضاف فقط في وضع الضبط)
        val content = LinearLayout(ui).apply {
            orientation = LinearLayout.VERTICAL
            background = Ui.rounded(Ui.CARD, Ui.dp(ui, 18).toFloat())
            setPadding(Ui.dp(ui, 16), Ui.dp(ui, 12), Ui.dp(ui, 16), Ui.dp(ui, 12))
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            addView(Ui.text(ui, "الإعدادات", 17f, Ui.TEXT, true))
            addView(Ui.text(ui, "اسحب الدوائر لتحديد أماكنها", 12f, Ui.MUTED))
            addView(Ui.settings(ui) { resizeButtons() })
            addView(Ui.button(ui, "إيقاف الطبقة", Ui.RED, small = true) { stopSelf() })
        }
        panel = ScrollView(ui).apply { addView(content) }
    }

    private fun resizeButtons() {
        for (v in listOf(trigger, scopeBtn)) {
            val p = params[v] ?: continue
            val cx = p.x + p.width / 2
            val cy = p.y + p.height / 2
            val s = Prefs.triggerSize
            p.width = s; p.height = s
            p.x = cx - s / 2; p.y = cy - s / 2
            wm.updateViewLayout(v, p)
        }
    }

    private fun setEditing(on: Boolean) {
        RecoilEngine.release()
        RecoilEngine.scopeUp()
        editing = on
        editBtn.text = if (on) "تم ✓" else "ضبط"
        for (m in listOf(fireMarker, aimMarker, scopeMarker)) {
            m.visibility = if (on) View.VISIBLE else View.GONE
            setTouchable(m, on)
        }
        if (on) showPanel() else hidePanel()
    }

    private fun showPanel() {
        if (panelShown) return
        val dm = resources.displayMetrics
        val w = minOf(Ui.dp(ui, 300), (dm.widthPixels * 0.5f).toInt())
        val h = dm.heightPixels - Ui.dp(ui, 130)
        val p = WindowManager.LayoutParams(
            w, h, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, baseFlags, PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = Ui.dp(ui, 10); y = Ui.dp(ui, 100); alpha = 0.95f
        }
        wm.addView(panel, p)
        panelShown = true
    }

    private fun hidePanel() {
        if (!panelShown) return
        try { wm.removeView(panel) } catch (_: Throwable) {}
        panelShown = false
    }

    override fun onDestroy() {
        RecoilEngine.release()
        RecoilEngine.scopeUp()
        hidePanel()
        for (v in params.keys) {
            try { wm.removeView(v) } catch (_: Throwable) {}
        }
        params.clear()
        running = false
        super.onDestroy()
    }
}
