package com.alphaxk.recoilpad

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private const val NAME = "recoil_prefs"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
    }

    var triggerX: Int
        get() = prefs.getInt("trigger_x", 500)
        set(v) = prefs.edit().putInt("trigger_x", v).apply()

    var triggerY: Int
        get() = prefs.getInt("trigger_y", 1200)
        set(v) = prefs.edit().putInt("trigger_y", v).apply()

    var triggerSize: Int
        get() = prefs.getInt("trigger_size", 120)
        set(v) = prefs.edit().putInt("trigger_size", v).apply()

    var triggerAlpha: Float
        get() = prefs.getFloat("trigger_alpha", 0.5f)
        set(v) = prefs.edit().putFloat("trigger_alpha", v).apply()

    var mode: Int // 0 = hold, 1 = single, 2 = burst
        get() = prefs.getInt("mode", 0)
        set(v) = prefs.edit().putInt("mode", v).apply()

    var recoilStrength: Float
        get() = prefs.getFloat("recoil_strength", 1.0f)
        set(v) = prefs.edit().putFloat("recoil_strength", v).apply()

    var recoilDurationMs: Int
        get() = prefs.getInt("recoil_duration", 200)
        set(v) = prefs.edit().putInt("recoil_duration", v).apply()

    var burstCount: Int
        get() = prefs.getInt("burst_count", 3)
        set(v) = prefs.edit().putInt("burst_count", v).apply()

    var burstIntervalMs: Int
        get() = prefs.getInt("burst_interval", 100)
        set(v) = prefs.edit().putInt("burst_interval", v).apply()
}
