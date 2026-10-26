package com.alphaxk.recoilpad

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View

@SuppressLint("ViewConstructor")
class FloatingTriggerView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb((Prefs.triggerAlpha * 255).toInt(), 255, 0, 0)
        style = Paint.Style.FILL
    }

    private var downX = 0f
    private var downY = 0f
    private var isDown = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(width / 2f, height / 2f, width / 2f, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isDown = true
                downX = event.rawX
                downY = event.rawY
                performClick()
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (isDown) {
                    isDown = false
                    RecoilEngine.fireAndRecoil(
                        Prefs.triggerX.toFloat(),
                        Prefs.triggerY.toFloat(),
                        Prefs.triggerX.toFloat(),
                        Prefs.triggerY.toFloat()
                    )
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun refreshAppearance() {
        paint.color = Color.argb((Prefs.triggerAlpha * 255).toInt(), 255, 0, 0)
        invalidate()
    }
}
