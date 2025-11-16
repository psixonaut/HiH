package com.example.rustoreapplicationshowcases.data.repository

import android.content.Context
import android.util.Log
import com.example.rustoreapplicationshowcases.data.local.JsonDataSource
import com.example.rustoreapplicationshowcases.data.model.AppInfo

class AppRepository(private val context: Context) {
    private val jsonDataSource = JsonDataSource(context)

    val apps: List<AppInfo> by lazy {
            // Fallback to JSON if SQLite fails or returns empty
            Log.w("AppRepository", "SQLite returned empty, falling back to JSON")
            val jsonApps = jsonDataSource.loadAppsFromAssets()
            Log.d("AppRepository", "Loaded ${jsonApps.size} apps from JSON")
            jsonApps
    }

    fun getAllCategories(): List<String> =
        apps.map { it.category }.distinct()
}