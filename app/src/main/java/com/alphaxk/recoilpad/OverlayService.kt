package com.alphaxk.recoilpad

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout

class OverlayService : Service() {
    private lateinit var wm: WindowManager
    private var triggerView: FloatingTriggerView? = null
    private var panelView: FloatingPanelView? = null
    private var moveBtn: Button? = null
    private var moving = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(1, buildNotification())
        ShizukuBridge.init()
        showTrigger()
        showPanel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel("recoil", "RecoilPad", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            Notification.Builder(this, "recoil")
                .setContentTitle("RecoilPad")
                .setContentText("Overlay aktif")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .build()
        else
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("RecoilPad")
                .setContentText("Overlay aktif")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .build()
    }

    private fun showTrigger() {
        if (triggerView != null) return
        val size = Prefs.triggerSize
        val params = WindowManager.LayoutParams(
            size, size,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = Prefs.triggerX
            y = Prefs.triggerY
        }
        triggerView = FloatingTriggerView(this)
        wm.addView(triggerView, params)

        // tombol move di atas trigger
        val btnParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = Prefs.triggerX
            y = Prefs.triggerY - 80
        }
        moveBtn = Button(this).apply {
            text = "نقل"
            setOnClickListener { moving = !moving }
        }
        wm.addView(moveBtn, btnParams)
    }

    private fun showPanel() {
        if (panelView != null) return
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 100
        }
        panelView = FloatingPanelView(this)
        wm.addView(panelView, params)
    }

    override fun onDestroy() {
        triggerView?.let { wm.removeView(it) }
        moveBtn?.let { wm.removeView(it) }
        panelView?.let { wm.removeView(it) }
        triggerView = null
        moveBtn = null
        panelView = null
        super.onDestroy()
    }
}
