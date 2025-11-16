package com.example.rustoreapplicationshowcases.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.rustoreapplicationshowcases.data.PreferencesManager
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.example.rustoreapplicationshowcases.data.repository.AppRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdminViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = AppRepository(application)
    private val preferencesManager = PreferencesManager(application)
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var successMessage by mutableStateOf<String?>(null)
        private set
    
    var apiUrl by mutableStateOf<String>(preferencesManager.getAdminApiUrl() ?: "")
        private set
    
    var isAdminModeEnabled by mutableStateOf<Boolean>(preferencesManager.isAdminModeEnabled())
        private set
    
    val lastSyncTime: String
        get() {
            val timestamp = preferencesManager.getLastSyncTime()
            return if (timestamp > 0) {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))
                dateFormat.format(Date(timestamp))
            } else {
                "Никогда"
            }
        }
    
    val currentAppsCount: Int
        get() = repository.getAppsCount()
    
    fun updateApiUrl(url: String) {
        apiUrl = url
    }
    
    fun toggleAdminMode() {
        isAdminModeEnabled = !isAdminModeEnabled
        preferencesManager.setAdminModeEnabled(isAdminModeEnabled)
        if (!isAdminModeEnabled) {
            // При отключении режима администратора сбрасываем к локальным данным
            repository.clearCache()
        }
    }
    
    fun saveApiUrl() {
        preferencesManager.setAdminApiUrl(apiUrl.ifBlank { null })
        successMessage = "URL сохранен"
    }
    
    fun loadAppsFromApi() {
        if (apiUrl.isBlank()) {
            errorMessage = "Введите URL API"
            return
        }
        
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            
            try {
                // Используем URL как есть
                val result = repository.loadAppsFromApi(apiUrl)
                
                result.onSuccess { apps ->
                    repository.updateAppsCache(apps)
                    preferencesManager.setLastSyncTime(System.currentTimeMillis())
                    preferencesManager.setAdminApiUrl(apiUrl)
                    // Обновляем количество приложений
                    successMessage = "Успешно загружено ${apps.size} приложений. Всего в базе: ${repository.getAppsCount()}"
                }.onFailure { exception ->
                    errorMessage = exception.message ?: "Ошибка загрузки данных"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Неизвестная ошибка"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun loadAppsFromDefaultApi() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            
            try {
                val result = repository.loadAppsFromApi(null)
                
                result.onSuccess { apps ->
                    repository.updateAppsCache(apps)
                    preferencesManager.setLastSyncTime(System.currentTimeMillis())
                    // Обновляем количество приложений
                    successMessage = "Успешно загружено ${apps.size} приложений из API по умолчанию. Всего в базе: ${repository.getAppsCount()}"
                }.onFailure { exception ->
                    errorMessage = exception.message ?: "Ошибка загрузки данных"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Неизвестная ошибка"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun resetToLocalData() {
        repository.clearCache()
        preferencesManager.setLastSyncTime(0)
        successMessage = "Данные сброшены к локальным"
    }
    
    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
    
    fun getEditorChoiceApps(): List<AppInfo> {
        // Возвращаем топ приложений для "Выбор редакции"
        return repository.getCachedApps()
            .sortedByDescending { it.rating }
            .take(5)
    }
    
    fun getRecommendedApps(): List<AppInfo> {
        // Возвращаем рекомендуемые приложения
        return repository.getCachedApps()
            .sortedByDescending { it.downloads }
            .take(6)
    }
    
    fun getAvailableCategories(): List<String> {
        return repository.getAllCategories()
    }
    
    fun addApp(
        name: String,
        category: String,
        rating: String,
        downloads: String = "0",
        reviews: String = "0",
        ageRating: String = "0+",
        publisher: String = ""
    ) {
        if (name.isBlank() || category.isBlank() || rating.isBlank()) {
            errorMessage = "Заполните обязательные поля: название, категория, рейтинг"
            return
        }
        
        val ratingValue = rating.toDoubleOrNull()
        if (ratingValue == null || ratingValue < 0 || ratingValue > 5) {
            errorMessage = "Рейтинг должен быть числом от 0 до 5"
            return
        }
        
        val downloadsValue = downloads.toIntOrNull() ?: 0
        val reviewsValue = reviews.toIntOrNull() ?: 0
        
        val app = AppInfo(
            id = name.hashCode().toString(),
            name = name,
            rating = ratingValue,
            category = category,
            icon = "ic_app_placeholder",
            downloads = downloadsValue,
            reviews = reviewsValue,
            ageRating = ageRating,
            publisher = publisher
        )
        
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            
            try {
                val result = repository.addApp(app)
                result.onSuccess {
                    successMessage = "Приложение '${app.name}' успешно добавлено в базу данных"
                    // Обновляем количество приложений
                }.onFailure { exception ->
                    errorMessage = exception.message ?: "Ошибка при добавлении приложения"
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Неизвестная ошибка"
            } finally {
                isLoading = false
            }
        }
    }
}

