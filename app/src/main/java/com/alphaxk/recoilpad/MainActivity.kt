package com.alphaxk.recoilpad

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {
    private val permListener = Shizuku.OnRequestPermissionResultListener { _, _ ->
        runOnUiThread { render() }
    }
    private val binderListener = Shizuku.OnBinderReceivedListener {
        runOnUiThread { render() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        Shizuku.addRequestPermissionResultListener(permListener)
        Shizuku.addBinderReceivedListenerSticky(binderListener)
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
        }
        render()
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permListener)
        Shizuku.removeBinderReceivedListener(binderListener)
        super.onDestroy()
    }

    private fun alive() = try { Shizuku.pingBinder() } catch (_: Throwable) { false }
    private fun granted() = try {
        Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (_: Throwable) { false }

    private fun row(title: String, status: String, ok: Boolean, btn: String?, action: () -> Unit): View {
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, Ui.dp(this@MainActivity, 8), 0, Ui.dp(this@MainActivity, 8))
        }
        val info = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        info.addView(Ui.text(this, title, 16f, Ui.TEXT, true))
        info.addView(Ui.text(this, status, 13f, if (ok) Ui.GREEN else Ui.AMBER))
        r.addView(info, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        if (!ok && btn != null) r.addView(Ui.button(this, btn, Ui.BLUE, small = true, onClick = action))
        return r
    }

    private fun render() {
        val a = alive()
        val g = a && granted()
        val o = Settings.canDrawOverlays(this)
        val running = OverlayService.running

        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val p = Ui.dp(this@MainActivity, 18)
            setPadding(p, p * 2, p, p)
        }
        col.addView(Ui.text(this, "مُعوّض الارتداد", 28f, Ui.TEXT, true))
        col.addView(Ui.text(this, "زر عائم يعوّض ارتداد السلاح تلقائياً أثناء الإطلاق", 14f, Ui.MUTED).apply {
            setPadding(0, 0, 0, Ui.dp(this@MainActivity, 16))
        })

        // الصلاحيات
        val perms = Ui.card(this)
        perms.addView(Ui.text(this, "الصلاحيات", 13f, Ui.MUTED))
        perms.addView(row(
            "Shizuku",
            when {
                !a -> "غير مشغّل — شغّل تطبيق Shizuku أولاً"
                !g -> "يعمل — يحتاج إلى إذنك"
                else -> "جاهز ✓"
            }, g, if (!a) "فتح Shizuku" else "منح الإذن"
        ) {
            if (!a) {
                val i = packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                if (i != null) startActivity(i)
                else Toast.makeText(this, "تطبيق Shizuku غير مثبّت", Toast.LENGTH_LONG).show()
            } else {
                Shizuku.requestPermission(1001)
            }
        })
        perms.addView(row(
            "الظهور فوق التطبيقات",
            if (o) "جاهز ✓" else "مطلوب لعرض الزر العائم", o, "منح الإذن"
        ) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        })
        col.addView(perms)

        // زر التشغيل
        val ready = g && o
        val main = Ui.button(
            this,
            if (running) "إيقاف الطبقة العائمة" else "تشغيل الطبقة العائمة",
            if (running) Ui.RED else if (ready) Ui.GREEN else Ui.GRAY
        ) {
            val svc = Intent(this, OverlayService::class.java)
            if (running) stopService(svc)
            else if (ready) startForegroundService(svc)
            else Toast.makeText(this, "أكمل الصلاحيات أولاً", Toast.LENGTH_SHORT).show()
            window.decorView.postDelayed({ render() }, 450)
        }
        col.addView(main, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = Ui.dp(this@MainActivity, 12) })

        // الإعدادات
        val set = Ui.card(this)
        set.addView(Ui.text(this, "الإعدادات", 13f, Ui.MUTED))
        set.addView(Ui.settings(this))
        col.addView(set)

        // طريقة الاستخدام
        val how = Ui.card(this)
        how.addView(Ui.text(this, "طريقة الاستخدام", 13f, Ui.MUTED))
        listOf(
            "١) شغّل الطبقة ثم افتح اللعبة.",
            "٢) اضغط «ضبط»، ثم ضع الحلقة الزرقاء فوق زر الإطلاق في اللعبة، والخضراء في منطقة تحريك الكاميرا، والدائرة الحمراء حيث يريحك إصبعك.",
            "٣) اضغط «تم ✓» ثم اضغط مطوّلاً على الدائرة الحمراء للإطلاق.",
            "٤) زد «قوة السحب» إذا كان التعويض ضعيفاً، وقلّلها إذا كان زائداً."
        ).forEach {
            how.addView(Ui.text(this, it, 14f, Ui.TEXT).apply {
                setPadding(0, Ui.dp(this@MainActivity, 6), 0, 0)
            })
        }
        col.addView(how)

        col.addView(Ui.text(
            this,
            "تنبيه: استخدام أدوات كهذه في الألعاب الجماعية قد يخالف شروطها ويعرّض حسابك للحظر. الاستخدام على مسؤوليتك.",
            12f, Ui.MUTED
        ))

        setContentView(ScrollView(this).apply {
            setBackgroundColor(Ui.BG)
            layoutDirection = View.LAYOUT_DIRECTION_RTL
            addView(col)
        })
    }
}
