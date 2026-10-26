package com.alphaxk.recoilpad

import android.content.Context
import android.content.SharedPreferences

object Prefs {
    private lateinit var sp: SharedPreferences

    fun init(c: Context) {
        if (!::sp.isInitialized) {
            sp = c.applicationContext.getSharedPreferences("recoil_prefs", Context.MODE_PRIVATE)
        }
    }

    private fun int(key: String, def: Int) = sp.getInt(key, def)
    private fun put(key: String, v: Int) = sp.edit().putInt(key, v).apply()
    private fun bool(key: String, def: Boolean) = sp.getBoolean(key, def)
    private fun put(key: String, v: Boolean) = sp.edit().putBoolean(key, v).apply()

    var strength: Int
        get() = int("strength", 20) // القوة الرأسية
        set(v) = put("strength", v)
        
    var strengthX: Int
        get() = int("strength_x", 0) // القوة الأفقية (0 يعني مفيش سحب يمين/شمال)
        set(v) = put("strength_x", v)
        
    var curveExp: Int
        get() = int("curve_exp", 15) // منحنى التعويض (من 10 لـ 30)
        set(v) = put("curve_exp", v)

    var durationMs: Int
        get() = int("duration", 300000)
        set(v) = put("duration", v)
        
    var delayMs: Int
        get() = int("delay", 80)
        set(v) = put("delay", v)

    var autoFireEnabled: Boolean
        get() = bool("auto_fire_enabled", false)
        set(v) = put("auto_fire_enabled", v)
    var autoFireIntervalMs: Int
        get() = int("auto_fire_interval", 100)
        set(v) = put("auto_fire_interval", v)

    var panelAlpha: Int
        get() = int("panel_alpha", 90)
        set(v) = put("panel_alpha", v)

    // --- أحجام الأزرار المستقلة ---
    var sizeTrigger: Int
        get() = int("size_trigger", 160)
        set(v) = put("size_trigger", v)
    var sizeScopeBtn: Int
        get() = int("size_scope_btn", 160)
        set(v) = put("size_scope_btn", v)
    var sizeFireMarker: Int
        get() = int("size_fire_marker", 68)
        set(v) = put("size_fire_marker", v)
    var sizeAimMarker: Int
        get() = int("size_aim_marker", 68)
        set(v) = put("size_aim_marker", v)
    var sizeScopeMarker: Int
        get() = int("size_scope_marker", 68)
        set(v) = put("size_scope_marker", v)

    var editX: Int
        get() = int("edit_x", -1)
        set(v) = put("edit_x", v)
    var editY: Int
        get() = int("edit_y", -1)
        set(v) = put("edit_y", v)

    var trigX: Int
        get() = int("trig_x", -1)
        set(v) = put("trig_x", v)
    var trigY: Int
        get() = int("trig_y", -1)
        set(v) = put("trig_y", v)
    var fireX: Int
        get() = int("fire_x", -1)
        set(v) = put("fire_x", v)
    var fireY: Int
        get() = int("fire_y", -1)
        set(v) = put("fire_y", v)
    var aimX: Int
        get() = int("aim_x", -1)
        set(v) = put("aim_x", v)
    var aimY: Int
        get() = int("aim_y", -1)
        set(v) = put("aim_y", v)
    var scopeX: Int
        get() = int("scope_x", -1)
        set(v) = put("scope_x", v)
    var scopeY: Int
        get() = int("scope_y", -1)
        set(v) = put("scope_y", v)
    var scBtnX: Int
        get() = int("sc_btn_x", -1)
        set(v) = put("sc_btn_x", v)
    var scBtnY: Int
        get() = int("sc_btn_y", -1)
        set(v) = put("sc_btn_y", v)

    fun ensureDefaults(w: Int, h: Int) {
        if (trigX < 0) { trigX = (w * 0.88f).toInt(); trigY = (h * 0.78f).toInt() }
        if (fireX < 0) { fireX = (w * 0.78f).toInt(); fireY = (h * 0.62f).toInt() }
        if (aimX < 0) { aimX = (w * 0.65f).toInt(); aimY = (h * 0.38f).toInt() }
        if (scopeX < 0) { scopeX = (w * 0.50f).toInt(); scopeY = (h * 0.30f).toInt() }
        if (scBtnX < 0) { scBtnX = (w * 0.12f).toInt(); scBtnY = (h * 0.78f).toInt() }
        if (editX < 0) { editX = 100; editY = 100 }
    }
}
