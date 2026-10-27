package com.alphaxk.recoilpad

object WeaponDB {
    data class Profile(
        val name: String,
        val strength: Int,
        val curve: Int,
        val xRight: Int,
        val xLeft: Int
    )

    val weapons = listOf(
        Profile("M416", 50, 15, 10, 10),
        Profile("AKM", 75, 25, 20, 10),
        Profile("Beryl M762", 85, 28, 25, 15),
        Profile("SCAR-L", 45, 12, 10, 10),
        Profile("M16A4", 55, 20, 15, 15),
        Profile("UMP45", 30, 10, 5, 5),
        Profile("Vector", 25, 8, 5, 5),
        Profile("DP-28", 90, 30, 15, 15),
        Profile("M249", 85, 25, 20, 20)
    )

    val scopes = listOf(
        "Red Dot / Holo" to 1.0f,
        "2x" to 1.1f,
        "3x" to 1.3f,
        "4x" to 1.5f,
        "6x" to 2.0f
    )

    fun applyProfile(wName: String, sName: String) {
        val w = weapons.find { it.name == wName } ?: weapons[0]
        val s = scopes.find { it.first == sName } ?: scopes[0]
        
        Prefs.strength = (w.strength * s.second).toInt()
        Prefs.strengthXRight = (w.xRight * s.second).toInt()
        Prefs.strengthXLeft = (w.xLeft * s.second).toInt()
        Prefs.curveExp = w.curve
        
        Prefs.currentWeapon = wName
        Prefs.currentScope = sName
    }
}
