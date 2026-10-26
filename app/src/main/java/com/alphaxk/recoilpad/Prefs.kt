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

    var strength: Int
        get() = int("strength", 6)
        set(v) = put("strength", v)
    var durationMs: Int
        get() = int("duration", 600)
        set(v) = put("duration", v)
    var delayMs: Int
        get() = int("delay", 80)
        set(v) = put("delay", v)
    var triggerSize: Int
        get() = int("trigger_size", 160)
        set(v) = put("trigger_size", v)

    // مراكز العناصر على الشاشة (بالبكسل). -1 = لم تُحدَّد بعد
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

    // --- المتغيرات المضافة حديثاً ---
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

    /** w = الضلع الأطول، h = الضلع الأقصر (وضع أفقي) */
    fun ensureDefaults(w: Int, h: Int) {
        if (trigX < 0) { trigX = (w * 0.88f).toInt(); trigY = (h * 0.78f).toInt() }
        if (fireX < 0) { fireX = (w * 0.78f).toInt(); fireY = (h * 0.62f).toInt() }
        if (aimX < 0) { aimX = (w * 0.65f).toInt(); aimY = (h * 0.38f).toInt() }
        
        // إحداثيات افتراضية للمنظار (المنتصف العلوي) وزر المنظار (اليسار السفلي)
        if (scopeX < 0) { scopeX = (w * 0.50f).toInt(); scopeY = (h * 0.30f).toInt() }
        if (scBtnX < 0) { scBtnX = (w * 0.12f).toInt(); scBtnY = (h * 0.78f).toInt() }
    }
}
