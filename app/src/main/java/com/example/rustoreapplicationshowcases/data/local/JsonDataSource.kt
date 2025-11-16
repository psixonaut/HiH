package com.example.rustoreapplicationshowcases.data.local

import android.content.Context
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log

class JsonDataSource(private val context: Context) {

    fun loadAppsFromAssets(): List<AppInfo> {
        return try {
            Log.d("JsonDataSource", "Attempting to load apps from assets")
            val json = context.assets.open("apps.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<List<AppInfo>>() {}.type
            val apps: List<AppInfo> = Gson().fromJson(json, type) ?: emptyList()
            Log.d("JsonDataSource", "Successfully loaded ${apps.size} apps from assets")
            apps

        } catch (e: Exception) {
            Log.e("JsonDataSource", "Error loading apps from assets", e)
            emptyList()
        }
    }
}