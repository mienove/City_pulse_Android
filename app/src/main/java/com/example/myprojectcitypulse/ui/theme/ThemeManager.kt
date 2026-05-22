package com.example.myprojectcitypulse.ui.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    const val LIGHT = "light"
    const val DARK = "dark"
    const val SYSTEM = "system"

    fun apply(mode: String) {
        when (mode) {
            LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    fun save(context: Context, mode: String) {
        context.getSharedPreferences("theme", Context.MODE_PRIVATE)
            .edit()
            .putString("key", mode)
            .apply()
    }

    fun load(context: Context): String {
        return context.getSharedPreferences("theme", Context.MODE_PRIVATE)
            .getString("key", SYSTEM) ?: SYSTEM
    }
}