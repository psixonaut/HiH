package com.example.rustoreapplicationshowcases.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.rustoreapplicationshowcases.R
import com.example.rustoreapplicationshowcases.data.model.AppInfo

private val categoryMap = mapOf(
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

class SQLiteDataSource(context: Context) {

    private val db: SQLiteDatabase

    private val ageRatingMap = mapOf(
        "Adults only 18+" to "18+",
        "Everyone" to "0+",
        "Everyone 10+" to "10+",
        "Mature 17+" to "17+",
        "Teen" to "13+",
        "Unrated" to "0+"
    )

    private fun mapAgeRating(raw: String?): String {
        if (raw == null) return "0+"
        return ageRatingMap[raw] ?: "0+"
    }

    init {
        PreloadedDatabase.copyDatabaseIfNeeded(context)
        db = PreloadedDatabase.openDatabase(context)
    }

    @SuppressLint("Range")
    fun getAllApps(): List<AppInfo> {
        val list = mutableListOf<AppInfo>()

        val cursor = db.rawQuery(
            """
            SELECT 
                App, 
                Category, 
                Rating, 
                Installs,
                Reviews,
                Content_Rating
            FROM apps
            """.trimIndent(), null
        )

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("App"))
                val rawCategory = cursor.getString(cursor.getColumnIndexOrThrow("Category"))
                val rating = cursor.getDouble(cursor.getColumnIndexOrThrow("Rating"))

                // ----- Installs -----
                val installsRaw = cursor.getString(cursor.getColumnIndexOrThrow("Installs"))
                val installs = installsRaw
                    ?.replace(",", "")
                    ?.replace("+", "")
                    ?.toIntOrNull() ?: 0

                // ----- Reviews (если нет столбца → 0) -----
                val reviews = try {
                    cursor.getInt(cursor.getColumnIndex("Reviews")).takeIf { it >= 0 } ?: 0
                } catch (_: Exception) {
                    0
                }

                // ----- Age rating (если нет → "0+") -----
                val rawAge = try {
                    cursor.getString(cursor.getColumnIndex("Content_Rating")) ?: "0+"
                } catch (_: Exception) {
                    "0+"
                }
                val age = mapAgeRating(rawAge)

                // русифицированная категория
                val category = categoryMap[rawCategory] ?: rawCategory

                val app = AppInfo(
                    id = name.hashCode().toString(),
                    name = name,
                    rating = rating,
                    category = category,
                    icon = "ic_app_placeholder",
                    downloads = installs,
                    reviews = reviews,
                    ageRating = age
                )

                list.add(app)

            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    fun incrementDownloads(appName: String) {
        db.execSQL(
            "UPDATE apps SET Installs = Installs + 1 WHERE App = ?",
            arrayOf(appName)
        )
    }

    fun getDistinctCategories(): List<String> {
        val list = mutableListOf<String>()

        val cursor = db.rawQuery("SELECT DISTINCT Category FROM apps", null)
        while (cursor.moveToNext()) {
            list.add(cursor.getString(0))
        }
        cursor.close()

        return list
    }
}