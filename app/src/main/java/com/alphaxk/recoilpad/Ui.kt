package com.alphaxk.recoilpad

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

object Ui {
    val BG = Color.parseColor("#0F1115")
    val CARD = Color.parseColor("#1A1D24")
    val TEXT = Color.parseColor("#F2F4F8")
    val MUTED = Color.parseColor("#9AA3B2")
    val RED = Color.parseColor("#EF4444")
    val BLUE = Color.parseColor("#3B82F6")
    val GREEN = Color.parseColor("#22C55E")
    val AMBER = Color.parseColor("#F59E0B")
    val GRAY = Color.parseColor("#2B303B")

    fun dp(c: Context, v: Int) = (v * c.resources.displayMetrics.density).toInt()

    fun rounded(color: Int, radius: Float) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = radius
    }

    fun text(c: Context, s: String, sp: Float, color: Int, bold: Boolean = false) =
        TextView(c).apply {
            text = s
            textSize = sp
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    fun button(c: Context, label: String, color: Int, small: Boolean = false, onClick: () -> Unit) =
        Button(c).apply {
            text = label
            isAllCaps = false
            setTextColor(Color.WHITE)
            textSize = if (small) 13f else 16f
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(color, dp(c, 14).toFloat())
            stateListAnimator = null
            minHeight = 0
            minimumHeight = 0
            val ph = dp(c, if (small) 14 else 20)
            val pv = dp(c, if (small) 8 else 14)
            setPadding(ph, pv, ph, pv)
            setOnClickListener { onClick() }
        }

    // --- تم إرجاع دالة card المفقودة هنا ---
    fun card(c: Context) = LinearLayout(c).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(CARD, dp(c, 18).toFloat())
        setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14))
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = dp(c, 12) }
    }
    // ----------------------------------------

    fun switch(c: Context, label: String, get: () -> Boolean, set: (Boolean) -> Unit): View {
        val box = LinearLayout(c).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(c, 8), 0, dp(c, 8))
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        val tv = text(c, label, 14f, TEXT).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val sw = Switch(c).apply {
            isChecked = get()
            setOnCheckedChangeListener { _, isChecked -> set(isChecked) }
        }
        box.addView(tv)
        box.addView(sw)
        return box
    }

    fun slider(
        c: Context, label: String, min: Int, max: Int,
        get: () -> Int, set: (Int) -> Unit, onChange: () -> Unit = {},
        fmt: (Int) -> String = { it.toString() }
    ): View {
        val box = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(c, 6), 0, dp(c, 6))
        }
        val tv = text(c, "$label: ${fmt(get())}", 14f, TEXT)
        val sb = SeekBar(c)
        sb.min = min
        sb.max = max
        sb.progress = get().coerceIn(min, max)
        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                set(p)
                tv.text = "$label: ${fmt(p)}"
                if (fromUser) onChange()
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        box.addView(tv)
        box.addView(sb)
        return box
    }

    fun settings(c: Context, onSize: () -> Unit = {}, onAlpha: () -> Unit = {}) = LinearLayout(c).apply {
        orientation = LinearLayout.VERTICAL
        
        // إعدادات الارتداد
        addView(text(c, "--- إعدادات التعويض ---", 15f, AMBER, true))
        addView(slider(c, "قوة السحب (رأسي Y)", 1, 100, { Prefs.strength }, { Prefs.strength = it }))
        addView(slider(c, "قوة السحب (أفقي X)", -100, 100, { Prefs.strengthX }, { Prefs.strengthX = it }))
        addView(slider(c, "انحناء الارتداد (Curve)", 10, 30, { Prefs.curveExp }, { Prefs.curveExp = it }, fmt = { "%.1f".format(it / 10f) }))
        
        addView(slider(c, "مدة التعويض (مللي ثانية)", 100, 300000, { Prefs.durationMs }, { Prefs.durationMs = it }))
        addView(slider(c, "تأخير البداية (مللي ثانية)", 0, 500, { Prefs.delayMs }, { Prefs.delayMs = it }))
        
        // إعدادات الرشاش
        addView(text(c, "--- إعدادات الرشاش ---", 15f, AMBER, true))
        addView(switch(c, "تفعيل الضغط المتكرر (رشاش)", { Prefs.autoFireEnabled }, { Prefs.autoFireEnabled = it }))
        addView(slider(c, "سرعة الرشاش (مللي ثانية)", 20, 500, { Prefs.autoFireIntervalMs }, { Prefs.autoFireIntervalMs = it }))

        // المظهر
        addView(text(c, "--- المظهر ---", 15f, AMBER, true))
        addView(slider(c, "شفافية القائمة (%)", 10, 100, { Prefs.panelAlpha }, { Prefs.panelAlpha = it }, onAlpha))

        // الأحجام
        addView(text(c, "--- أحجام الأزرار ---", 15f, AMBER, true))
        addView(slider(c, "حجم زر الإطلاق", 80, 320, { Prefs.sizeTrigger }, { Prefs.sizeTrigger = it }, onSize))
        addView(slider(c, "حجم زر المنظار", 80, 320, { Prefs.sizeScopeBtn }, { Prefs.sizeScopeBtn = it }, onSize))
        addView(slider(c, "حجم علامة الإطلاق", 30, 136, { Prefs.sizeFireMarker }, { Prefs.sizeFireMarker = it }, onSize))
        addView(slider(c, "حجم علامة التصويب", 30, 136, { Prefs.sizeAimMarker }, { Prefs.sizeAimMarker = it }, onSize))
        addView(slider(c, "حجم علامة المنظار", 30, 136, { Prefs.sizeScopeMarker }, { Prefs.sizeScopeMarker = it }, onSize))
    }
}

class CircleView(
    c: Context,
    private val label: String,
    private val color: Int,
    private val filled: Boolean
) : View(c) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        val r = minOf(width, height) / 2f - 4f
        val cx = width / 2f
        val cy = height / 2f
        if (filled) {
            p.style = Paint.Style.FILL
            p.color = color
            p.alpha = 170
            canvas.drawCircle(cx, cy, r, p)
        }
        p.style = Paint.Style.STROKE
        p.strokeWidth = 6f
        p.color = color
        p.alpha = 255
        canvas.drawCircle(cx, cy, r, p)
        p.style = Paint.Style.FILL
        p.color = Color.WHITE
        p.textAlign = Paint.Align.CENTER
        p.typeface = Typeface.DEFAULT_BOLD
        p.textSize = r * 0.36f
        canvas.drawText(label, cx, cy - (p.descent() + p.ascent()) / 2f, p)
    }
}
