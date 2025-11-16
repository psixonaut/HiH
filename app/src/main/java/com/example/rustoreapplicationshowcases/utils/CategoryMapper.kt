package com.example.rustoreapplicationshowcases.utils

object CategoryMapper {

    private val categoryMap = mapOf(
        "1.9" to "Другое",

        "ART_AND_DESIGN" to "Искусство и дизайн",
        "AUTO_AND_VEHICLES" to "Авто и транспорт",
        "BEAUTY" to "Красота",
        "BOOKS_AND_REFERENCE" to "Книги и справочники",
        "BUSINESS" to "Бизнес",
        "COMICS" to "Комиксы",
        "COMMUNICATION" to "Связь",
        "DATING" to "Знакомства",
        "EDUCATION" to "Образование",
        "ENTERTAINMENT" to "Развлечения",
        "EVENTS" to "События",
        "FAMILY" to "Семья",
        "FINANCE" to "Финансы",
        "FOOD_AND_DRINK" to "Еда и напитки",
        "GAME" to "Игры",
        "HEALTH_AND_FITNESS" to "Здоровье и фитнес",
        "HOUSE_AND_HOME" to "Дом и быт",
        "LIBRARIES_AND_DEMO" to "Библиотеки и демо",
        "LIFESTYLE" to "Стиль жизни",
        "MAPS_AND_NAVIGATION" to "Карты и навигация",
        "MEDICAL" to "Медицина",
        "NEWS_AND_MAGAZINES" to "Новости и журналы",
        "PARENTING" to "Родительство",
        "PERSONALIZATION" to "Персонализация",
        "PHOTOGRAPHY" to "Фотография",
        "PRODUCTIVITY" to "Продуктивность",
        "SHOPPING" to "Покупки",
        "SOCIAL" to "Социальные",
        "SPORTS" to "Спорт",
        "TOOLS" to "Инструменты",
        "TRAVEL_AND_LOCAL" to "Путешествия",
        "VIDEO_PLAYERS" to "Видео",
        "WEATHER" to "Погода"
    )

    fun toRussian(category: String?): String? {
        if (category == null) return null
        return categoryMap[category] ?: category
    }
    
    fun toEnglish(category: String?): String? {
        if (category == null) return null
        // Обрабатываем "Другое"
        if (category == "Другое") return "1.9"
        // Создаем обратный маппинг
        val reverseMap = categoryMap.entries.associate { (key, value) -> value to key }
        return reverseMap[category] ?: category
    }
}