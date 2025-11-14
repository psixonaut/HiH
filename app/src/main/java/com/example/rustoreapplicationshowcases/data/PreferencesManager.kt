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
}