package com.example.rustoreapplicationshowcases.data.local

import android.content.Context
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class JsonDataSource(private val context: Context) {

    fun loadAppsFromAssets(): List<AppInfo> {
        return try {
            val json = context.assets.open("apps.json")
                .bufferedReader()
                .use { it.readText() }

            val type = object : TypeToken<List<AppInfo>>() {}.type
            Gson().fromJson(json, type)

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}