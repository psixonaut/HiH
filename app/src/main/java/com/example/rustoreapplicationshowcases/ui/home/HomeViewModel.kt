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

    fun getAllCategories(): List<String> {
        return repo.getAllCategories()
    }
    
    fun getCategoriesWithAppCount(): List<Pair<String, Int>> {
        return repo.getAllCategories().map { category ->
            category to apps.count { it.category == category }
        }.sortedByDescending { it.second }
    }
    
    fun getTopCategoriesByAppCount(limit: Int = 8): List<Pair<String, Int>> {
        return getCategoriesWithAppCount().take(limit)
    }
    
    fun getAppsByCategory(category: String): List<AppInfo> {
        return apps.filter { it.category == category }
    }
    
    fun searchApps(query: String): List<AppInfo> {
        if (query.isBlank()) return emptyList()
        return apps.filter { 
            it.name.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }
}