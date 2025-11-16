package com.example.rustoreapplicationshowcases.data.repository


import android.content.Context
import android.util.Log
import com.example.rustoreapplicationshowcases.data.PreferencesManager
import com.example.rustoreapplicationshowcases.data.local.SQLiteDataSource
import com.example.rustoreapplicationshowcases.data.local.ReviewData
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.example.rustoreapplicationshowcases.data.remote.ApiService
import com.example.rustoreapplicationshowcases.data.remote.RetrofitClient
import com.example.rustoreapplicationshowcases.utils.CategoryMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class AppRepository(context: Context) {

    private val db = SQLiteDataSource(context)
    private val apiService: ApiService = RetrofitClient.apiService
    private val preferencesManager = PreferencesManager(context)

    /** Получить все приложения (с учетом режима администратора) */
    fun getAllApps(): List<AppInfo> {
        val isAdminMode = preferencesManager.isAdminModeEnabled()
        val hasCachedApps = preferencesManager.hasCachedApps()
        
        return if (isAdminMode && hasCachedApps) {
            // В режиме администратора используем загруженные данные
            preferencesManager.getCachedApps()
        } else {
            // Иначе используем локальные данные из БД
            db.getAllApps()
        }
    }
    
    /** Получить приложения из кэша (загруженные приложения) */
    fun getCachedApps(): List<AppInfo> {
        return preferencesManager.getCachedApps()
    }
    
    /** Получить количество приложений */
    fun getAppsCount(): Int = getAllApps().size

    /** Получить список уникальных категорий */
    fun getAllCategories(): List<String> {
        val raw = db.getDistinctCategories()
        val mapped = raw.mapNotNull { CategoryMapper.toRussian(it) }
        val sorted = mapped.sorted()
        return sorted + "Другое"
    }
    
    /**
     * Загружает приложения из API
     * @param apiUrl Опциональный URL для загрузки (если null, используется дефолтный)
     * @return Результат загрузки: список приложений или null в случае ошибки
     */
    suspend fun loadAppsFromApi(apiUrl: String? = null): Result<List<AppInfo>> = withContext(Dispatchers.IO) {
        try {
            Log.d("AppRepository", "Attempting to load apps from API: ${apiUrl ?: "default"}")
            
            // Если URL указан, используем его напрямую
            val response = if (apiUrl != null) {
                // Используем полный URL напрямую
                apiService.getApps(apiUrl)
            } else {
                // Используем дефолтный URL
                val defaultUrl = "https://raw.githubusercontent.com/username/repo/main/apps.json"
                apiService.getApps(defaultUrl)
            }
            
            if (response.isSuccessful && response.body() != null) {
                val apps = response.body()!!.map { app ->
                    // Генерируем ID для приложений, если их нет
                    if (app.id.isBlank()) {
                        app.copy(id = UUID.randomUUID().toString())
                    } else {
                        app
                    }
                }
                Log.d("AppRepository", "Successfully loaded ${apps.size} apps from API")
                Result.success(apps)
            } else {
                Log.e("AppRepository", "API request failed: ${response.code()} - ${response.message()}")
                Result.failure(Exception("API request failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Error loading apps from API", e)
            Result.failure(e)
        }
    }
    
    /**
     * Обновляет приложения в кэше (для режима администратора)
     * Сохраняет загруженные приложения в SharedPreferences
     */
    fun updateAppsCache(apps: List<AppInfo>) {
        Log.d("AppRepository", "updateAppsCache called with ${apps.size} apps")
        preferencesManager.setCachedApps(apps)
        Log.d("AppRepository", "Cached ${apps.size} apps successfully")
    }
    
    /**
     * Сбрасывает кэш (для режима администратора)
     * Удаляет загруженные приложения из SharedPreferences
     */
    fun clearCache() {
        Log.d("AppRepository", "clearCache called")
        preferencesManager.clearCachedApps()
        Log.d("AppRepository", "Cache cleared successfully")
    }
    
    /**
     * Обновляет приложение в БД (например, при увеличении downloads)
     */
    fun updateApp(app: AppInfo) {
        db.incrementDownloads(app.name)
    }
    
    /**
     * Добавляет новое приложение в БД
     */
    fun addApp(app: AppInfo): Result<Long> {
        return try {
            val result = db.insertApp(app)
            if (result > 0) {
                Log.d("AppRepository", "App '${app.name}' added successfully with id: $result")
                Result.success(result)
            } else {
                Log.e("AppRepository", "Failed to add app '${app.name}'")
                Result.failure(Exception("Failed to add app to database"))
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Error adding app '${app.name}'", e)
            Result.failure(e)
        }
    }
    
    /**
     * Получает отзывы для приложения из БД
     */
    fun getReviewsForApp(appName: String): List<ReviewData> {
        return db.getReviewsForApp(appName)
    }
    
    /**
     * Добавляет новый отзыв в БД
     */
    fun addReview(appName: String, reviewText: String, userName: String = "Пользователь"): Result<Long> {
        return try {
            val result = db.addReview(appName, reviewText, userName)
            if (result > 0) {
                Log.d("AppRepository", "Review added successfully for app: $appName")
                Result.success(result)
            } else {
                Log.e("AppRepository", "Failed to add review for app: $appName")
                Result.failure(Exception("Failed to add review to database"))
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Error adding review for app: $appName", e)
            Result.failure(e)
        }
    }
}