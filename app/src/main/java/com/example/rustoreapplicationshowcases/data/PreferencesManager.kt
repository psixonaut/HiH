package com.example.rustoreapplicationshowcases.data

import android.content.Context
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

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
    
    // Методы для режима администратора
    fun isAdminModeEnabled(): Boolean {
        return prefs.getBoolean("admin_mode_enabled", false)
    }
    
    fun setAdminModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("admin_mode_enabled", enabled).apply()
    }
    
    fun getAdminApiUrl(): String? {
        return prefs.getString("admin_api_url", null)
    }
    
    fun setAdminApiUrl(url: String?) {
        if (url != null) {
            prefs.edit().putString("admin_api_url", url).apply()
        } else {
            prefs.edit().remove("admin_api_url").apply()
        }
    }
    
    fun getLastSyncTime(): Long {
        return prefs.getLong("last_sync_time", 0L)
    }
    
    fun setLastSyncTime(timestamp: Long) {
        prefs.edit().putLong("last_sync_time", timestamp).apply()
    }
    
    // Методы для работы с пользователями
    fun getCurrentUserId(): Long? {
        val userId = prefs.getLong("current_user_id", -1L)
        return if (userId == -1L) null else userId
    }
    
    fun setCurrentUserId(userId: Long) {
        prefs.edit().putLong("current_user_id", userId).apply()
    }
    
    fun clearCurrentUserId() {
        prefs.edit().remove("current_user_id").apply()
    }
    
    fun getUserEmail(userId: Long): String? {
        return prefs.getString("user_email_$userId", null)
    }
    
    fun setUserEmail(userId: Long, email: String) {
        prefs.edit().putString("user_email_$userId", email).apply()
    }
    
    // Методы для хранения загруженных приложений (для режима администратора)
    fun getCachedApps(): List<AppInfo> {
        val json = prefs.getString("cached_apps", null)
        return if (json != null) {
            try {
                val type = object : TypeToken<List<AppInfo>>() {}.type
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    fun setCachedApps(apps: List<AppInfo>) {
        val json = gson.toJson(apps)
        prefs.edit().putString("cached_apps", json).apply()
    }
    
    fun clearCachedApps() {
        prefs.edit().remove("cached_apps").apply()
    }
    
    fun hasCachedApps(): Boolean {
        return prefs.contains("cached_apps")
    }
}