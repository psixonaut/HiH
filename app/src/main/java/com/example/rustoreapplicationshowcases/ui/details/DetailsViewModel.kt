package com.example.rustoreapplicationshowcases.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rustoreapplicationshowcases.data.local.ReviewData
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.example.rustoreapplicationshowcases.data.repository.AppRepository
import com.example.rustoreapplicationshowcases.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class AppExtraInfo(
    val developer: String,
    val ageRating: String,
    val description: String
)

data class AppReview(
    val userName: String,
    val rating: Float,
    val date: String,
    val text: String
)

class DetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(application)
    private val preferencesManager = PreferencesManager(application)

    private val extraInfo: Map<String, AppExtraInfo> = mapOf(
        "Сбербанк" to AppExtraInfo(
            developer = "Сбербанк",
            ageRating = "0+",
            description = "Удобное банковское приложение для управления картами, переводами, платежами и инвестициями."
        )
    )

    // Кэш отзывов из БД
    private val _reviewsFromDb = MutableStateFlow<Map<String, List<AppReview>>>(emptyMap())
    val reviewsFromDb: StateFlow<Map<String, List<AppReview>>> = _reviewsFromDb

    fun getAppByName(name: String): AppInfo? =
        repo.getAllApps().find { it.name == name }

    fun getExtraInfo(name: String): AppExtraInfo? =
        extraInfo[name]

    fun getReviews(name: String): List<AppReview> {
        // Сначала получаем из БД
        val dbReviews = _reviewsFromDb.value[name] ?: emptyList()
        
        // Если есть старые отзывы в памяти, объединяем
        val oldReviews = mapOf(
            "Сбербанк" to listOf(
                AppReview("Анна", 5f, "12.11.2025",
                    "Очень удобно оплачивать коммуналку и переводить деньги по номеру телефона. Интерфейс понятный."),
                AppReview("Игорь", 4f, "10.11.2025",
                    "Все основные функции под рукой, иногда только долго загружается история операций."),
                AppReview("Мария", 5f, "05.11.2025",
                    "Нравится дизайн и пуши по операциям. Чувствую контроль над финансами.")
            )
        )
        
        val old = oldReviews[name] ?: emptyList()
        
        // Объединяем, убирая дубликаты
        return (old + dbReviews).distinctBy { it.text }
    }

    fun getSimilarApps(current: AppInfo, limit: Int = 5): List<AppInfo> =
        repo.getAllApps()
            .filter { it.category == current.category && it.name != current.name }
            .take(limit)
    
    /**
     * Загружает отзывы из БД для приложения
     */
    fun loadReviewsFromDb(appName: String) {
        viewModelScope.launch {
            android.util.Log.d("DetailsViewModel", "Loading reviews for app: $appName")
            val reviewDataList = repo.getReviewsForApp(appName)
            android.util.Log.d("DetailsViewModel", "Loaded ${reviewDataList.size} reviews from DB")
            
            val appReviews = reviewDataList.map { reviewData ->
                // Конвертируем ReviewData в AppReview
                val userName = reviewData.userName.takeIf { it.isNotBlank() } ?: "Пользователь"
                
                // Используем полярность для определения рейтинга, если она есть
                // Иначе используем дефолтное значение
                val rating = if (reviewData.polarity != 0.0) {
                    when {
                        reviewData.polarity > 0.3 -> 5f
                        reviewData.polarity > 0.1 -> 4f
                        reviewData.polarity > -0.1 -> 3f
                        reviewData.polarity > -0.3 -> 2f
                        else -> 1f
                    }
                } else {
                    3f // Дефолтный рейтинг, если полярность не определена
                }
                
                android.util.Log.d("DetailsViewModel", "Converting review: userName=$userName, rating=$rating, polarity=${reviewData.polarity}")
                
                AppReview(
                    userName = userName,
                    rating = rating,
                    date = SimpleDateFormat("dd.MM.yyyy", Locale("ru")).format(Date()),
                    text = reviewData.reviewText
                )
            }
            
            val currentMap = _reviewsFromDb.value.toMutableMap()
            currentMap[appName] = appReviews
            _reviewsFromDb.value = currentMap
            android.util.Log.d("DetailsViewModel", "Updated reviews map. Total reviews for $appName: ${appReviews.size}")
        }
    }
    
    /**
     * Добавляет новый отзыв в БД
     */
    fun addReview(appName: String, reviewText: String, rating: Float, userName: String? = null) {
        viewModelScope.launch {
            android.util.Log.d("DetailsViewModel", "Adding review for app: $appName")
            val user = userName ?: preferencesManager.getCurrentUserId()?.let { 
                preferencesManager.getUserEmail(it) 
            } ?: "Пользователь"
            
            val result = repo.addReview(appName, reviewText, user)
            result.onSuccess {
                android.util.Log.d("DetailsViewModel", "Review added successfully, reloading...")
                // Обновляем список отзывов
                loadReviewsFromDb(appName)
            }.onFailure { exception ->
                android.util.Log.e("DetailsViewModel", "Failed to add review", exception)
            }
        }
    }
}
