package com.example.rustoreapplicationshowcases.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rustoreapplicationshowcases.data.local.SQLiteDataSource
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SQLiteDataSource(application)

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps

    var searchQuery by mutableStateOf("")
        private set

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _apps.value = db.getAllApps()
        }
    }

    fun onSearchChange(newValue: String) {
        searchQuery = newValue
    }

    fun getFilteredApps(category: String?): List<AppInfo> {
        return _apps.value
            .filter { category == null || it.category == category }
            .filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    fun onAppClicked(app: AppInfo) {
        viewModelScope.launch {
            db.incrementDownloads(app.id)
            _apps.value = db.getAllApps()
        }
    }

    fun getTopApps(limit: Int = 5): List<AppInfo> {
        return _apps.value
            .sortedByDescending { it.downloads }
            .take(limit)
    }
}