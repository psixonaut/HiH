package com.example.rustoreapplicationshowcases.data.repository

import android.content.Context
import com.example.rustoreapplicationshowcases.data.local.JsonDataSource
import com.example.rustoreapplicationshowcases.data.model.AppInfo

class AppRepository(private val context: Context) {

    private val dataSource = JsonDataSource(context)

    val apps: List<AppInfo> by lazy {
        dataSource.loadAppsFromAssets()
    }

    fun getAllCategories(): List<String> =
        apps.map { it.category }.distinct()
}