package com.example.rustoreapplicationshowcases.ui.home

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.example.rustoreapplicationshowcases.data.repository.AppRepository

class HomeViewModel : ViewModel() {

    private val repo = AppRepository

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
}