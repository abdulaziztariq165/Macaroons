package com.alphaxk.recoilpad

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {
    companion object {
        private const val REQ_SHIZUKU = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val status = TextView(this).apply {
            text = "Shizuku: ${if (ShizukuBridge.isReady()) "✅" else "❌"}\n" +
                   "Overlay: ${if (Settings.canDrawOverlays(this@MainActivity)) "✅" else "❌"}\n" +
                   "Accessibility: ${if (RecoilAccessibilityService.instance != null) "✅" else "❌"}"
        }
        layout.addView(status)

        layout.addView(Button(this).apply {
            text = "1. طلب صلاحية Shizuku"
            setOnClickListener {
                if (Shizuku.pingBinder()) {
                    if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                        Shizuku.requestPermission(REQ_SHIZUKU)
                    }
                }
            }
        })

        layout.addView(Button(this).apply {
            text = "2. صلاحية Overlay"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")))
                }
            }
        })

        layout.addView(Button(this).apply {
            text = "3. تفعيل Accessibility"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        })

        layout.addView(Button(this).apply {
            text = "4. تشغيل Overlay"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    startForegroundService(Intent(this@MainActivity, OverlayService::class.java))
                }
            }
        })

        layout.addView(Button(this).apply {
            text = "5. إيقاف Overlay"
            setOnClickListener {
                stopService(Intent(this@MainActivity, OverlayService::class.java))
            }
        })

        setContentView(layout)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_SHIZUKU && grantResults.isNotEmpty() &&
            grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ShizukuBridge.init()
        }
    }
}
