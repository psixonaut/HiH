package com.example.rustoreapplicationshowcases.ui.details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.example.rustoreapplicationshowcases.data.repository.AppRepository

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

    private val extraInfo: Map<String, AppExtraInfo> = mapOf(
        "Сбербанк" to AppExtraInfo(
            developer = "Сбербанк",
            ageRating = "0+",
            description = "Удобное банковское приложение для управления картами, переводами, платежами и инвестициями."
        )
    )

    private val reviews: Map<String, List<AppReview>> = mapOf(
        "Сбербанк" to listOf(
            AppReview("Анна", 5f, "12.11.2025",
                "Очень удобно оплачивать коммуналку и переводить деньги по номеру телефона. Интерфейс понятный."),
            AppReview("Игорь", 4f, "10.11.2025",
                "Все основные функции под рукой, иногда только долго загружается история операций."),
            AppReview("Мария", 5f, "05.11.2025",
                "Нравится дизайн и пуши по операциям. Чувствую контроль над финансами.")
        )
    )

    fun getAppByName(name: String): AppInfo? =
        repo.getAllApps().find { it.name == name }

    fun getExtraInfo(name: String): AppExtraInfo? =
        extraInfo[name]

    fun getReviews(name: String): List<AppReview> =
        reviews[name].orEmpty()

    fun getSimilarApps(current: AppInfo, limit: Int = 5): List<AppInfo> =
        repo.getAllApps()
            .filter { it.category == current.category && it.name != current.name }
            .take(limit)
}
