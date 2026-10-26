package com.alphaxk.recoilpad

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.*

@SuppressLint("ViewConstructor")
class FloatingPanelView(context: Context) : LinearLayout(context) {
    private val modeGroup: RadioGroup
    private val strengthBar: SeekBar
    private val durationBar: SeekBar
    private val burstBar: SeekBar

    init {
        orientation = VERTICAL
        setBackgroundColor(Color.argb(220, 20, 20, 20))
        setPadding(24, 24, 24, 24)

        modeGroup = RadioGroup(context).apply {
            orientation = HORIZONTAL
        }
        val rbHold = RadioButton(context).apply { text = "Hold"; id = 0; isChecked = Prefs.mode == 0 }
        val rbSingle = RadioButton(context).apply { text = "Single"; id = 1; isChecked = Prefs.mode == 1 }
        val rbBurst = RadioButton(context).apply { text = "Burst"; id = 2; isChecked = Prefs.mode == 2 }
        modeGroup.addView(rbHold)
        modeGroup.addView(rbSingle)
        modeGroup.addView(rbBurst)
        modeGroup.setOnCheckedChangeListener { _, checkedId -> Prefs.mode = checkedId }
        addView(modeGroup)

        strengthBar = SeekBar(context).apply {
            max = 100
            progress = (Prefs.recoilStrength * 50).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser) Prefs.recoilStrength = p / 50f
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        addView(TextView(context).apply { text = "Recoil Strength"; setTextColor(Color.WHITE) })
        addView(strengthBar)

        durationBar = SeekBar(context).apply {
            max = 500
            progress = Prefs.recoilDurationMs
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser) Prefs.recoilDurationMs = p.coerceAtLeast(50)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        addView(TextView(context).apply { text = "Recoil Duration (ms)"; setTextColor(Color.WHITE) })
        addView(durationBar)

        burstBar = SeekBar(context).apply {
            max = 10
            progress = Prefs.burstCount
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    if (fromUser) Prefs.burstCount = p.coerceAtLeast(1)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        addView(TextView(context).apply { text = "Burst Count"; setTextColor(Color.WHITE) })
        addView(burstBar)
    }
}
