package com.example.rustoreapplicationshowcases.data

import android.content.Context

class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    fun isOnboardingShown(): Boolean {
        return prefs.getBoolean("onboarding_shown", false)
    }

    fun setOnboardingShown() {
        prefs.edit().putBoolean("onboarding_shown", true).apply()
    }

    fun getThemePreference(): String? {
        return prefs.getString("theme_preference", null)
    }

    fun setThemePreference(theme: String) {
        prefs.edit().putString("theme_preference", theme).apply()
    }

    fun getSelectedCategories(): String? {
        return prefs.getString("selected_categories", null)
    }

    fun setSelectedCategories(categories: String) {
        prefs.edit().putString("selected_categories", categories).apply()
    }
}