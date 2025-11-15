package com.example.rustoreapplicationshowcases.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.example.rustoreapplicationshowcases.data.repository.AppRepository

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(application)

    val apps: List<AppInfo> = repo.apps

    var searchQuery by mutableStateOf("")
        private set

    fun onSearchChange(newValue: String) {
        searchQuery = newValue
    }

    fun getFilteredApps(category: String?): List<AppInfo> {
        return apps
            .filter { category == null || it.category == category }
            .filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    fun onAppClicked(app: AppInfo) {
        app.downloads++
    }

    fun getTopApps(limit: Int = 5): List<AppInfo> {
        return apps.sortedByDescending { it.downloads }
            .take(limit)
    }
}