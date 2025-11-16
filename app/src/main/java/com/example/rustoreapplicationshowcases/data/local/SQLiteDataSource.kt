package com.example.rustoreapplicationshowcases.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.rustoreapplicationshowcases.R
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.example.rustoreapplicationshowcases.utils.CategoryMapper

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

        // Проверяем наличие столбца publisher
        val tableInfoCursor = db.rawQuery("PRAGMA table_info(apps)", null)
        val hasPublisherColumn = mutableListOf<String>()
        while (tableInfoCursor.moveToNext()) {
            hasPublisherColumn.add(tableInfoCursor.getString(1)) // column name
        }
        tableInfoCursor.close()
        
        android.util.Log.d("SQLiteDataSource", "Available columns: ${hasPublisherColumn.joinToString(", ")}")
        val hasPublisher = hasPublisherColumn.contains("publisher")
        android.util.Log.d("SQLiteDataSource", "Has publisher column: $hasPublisher")
        
        // Всегда включаем publisher в SELECT, если столбец существует
        val selectColumns = mutableListOf("App", "Category", "Rating", "Installs", "Reviews", "Content_Rating")
        if (hasPublisher) {
            selectColumns.add("publisher")
            android.util.Log.d("SQLiteDataSource", "Added publisher to SELECT query")
        } else {
            android.util.Log.w("SQLiteDataSource", "Publisher column not found in database schema!")
        }
        
        val query = "SELECT ${selectColumns.joinToString(", ")} FROM apps LIMIT 1"
        android.util.Log.d("SQLiteDataSource", "Sample query: $query")
        
        val cursor = db.rawQuery(
            """
            SELECT ${selectColumns.joinToString(", ")}
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

                // ----- Publisher -----
                val publisher = if (hasPublisher) {
                    try {
                        val publisherIndex = cursor.getColumnIndex("publisher")
                        if (publisherIndex >= 0) {
                            val pubValue = cursor.getString(publisherIndex)?.trim() ?: ""
                            // Логируем первые несколько приложений для отладки
                            if (list.size < 3) {
                                android.util.Log.d("SQLiteDataSource", "App: '$name', Publisher index: $publisherIndex, Value: '$pubValue'")
                            }
                            pubValue
                        } else {
                            android.util.Log.w("SQLiteDataSource", "Publisher column index not found for app: $name (available columns: ${cursor.columnNames.joinToString()})")
                            ""
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SQLiteDataSource", "Error reading publisher for $name: ${e.message}", e)
                        ""
                    }
                } else {
                    ""
                }

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
                    ageRating = age,
                    publisher = publisher
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
    
    /**
     * Вставляет новое приложение в БД
     */
    fun insertApp(app: AppInfo): Long {
        // Конвертируем русскую категорию в английскую для БД
        val englishCategory = CategoryMapper.toEnglish(app.category) ?: app.category
        
        val values = android.content.ContentValues().apply {
            put("App", app.name)
            put("Category", englishCategory)
            put("Rating", app.rating)
            put("Installs", "${app.downloads}+")
            put("Reviews", app.reviews)
            put("Content_Rating", when (app.ageRating) {
                "18+" -> "Adults only 18+"
                "17+" -> "Mature 17+"
                "13+" -> "Teen"
                "10+" -> "Everyone 10+"
                else -> "Everyone"
            })
            // Проверяем наличие столбца publisher перед добавлением
            val tableInfoCursor = db.rawQuery("PRAGMA table_info(apps)", null)
            val hasPublisher = mutableListOf<String>().apply {
                while (tableInfoCursor.moveToNext()) {
                    add(tableInfoCursor.getString(1))
                }
            }.contains("publisher")
            tableInfoCursor.close()
            
            if (hasPublisher && app.publisher.isNotBlank()) {
                put("publisher", app.publisher)
            }
        }
        
        return db.insertWithOnConflict("apps", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }
    
    /**
     * Получает отзывы для приложения из таблицы reviews
     */
    @SuppressLint("Range")
    fun getReviewsForApp(appName: String): List<ReviewData> {
        val list = mutableListOf<ReviewData>()
        
        // Сначала проверяем все таблицы в БД
        val allTablesCursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table'",
            null
        )
        val allTables = mutableListOf<String>()
        while (allTablesCursor.moveToNext()) {
            allTables.add(allTablesCursor.getString(0))
        }
        allTablesCursor.close()
        android.util.Log.d("SQLiteDataSource", "All tables in DB: ${allTables.joinToString(", ")}")
        
        // Проверяем наличие таблицы reviews (пробуем разные варианты названий)
        val possibleTableNames = listOf("reviews", "Reviews", "REVIEWS")
        var tableName: String? = null
        
        for (table in possibleTableNames) {
            val tableExistsCursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                arrayOf(table)
            )
            val tableExists = tableExistsCursor.count > 0
            tableExistsCursor.close()
            
            if (tableExists) {
                tableName = table
                android.util.Log.d("SQLiteDataSource", "Found reviews table: $table")
                break
            }
        }
        
        if (tableName == null) {
            android.util.Log.w("SQLiteDataSource", "Reviews table does not exist. Available tables: ${allTables.joinToString(", ")}")
            return emptyList()
        }
        
        // Проверяем структуру таблицы
        val tableInfoCursor = db.rawQuery("PRAGMA table_info($tableName)", null)
        val columns = mutableSetOf<String>()
        while (tableInfoCursor.moveToNext()) {
            columns.add(tableInfoCursor.getString(1))
        }
        tableInfoCursor.close()
        
        android.util.Log.d("SQLiteDataSource", "Reviews table columns: ${columns.joinToString(", ")}")
        
        // Определяем правильные имена столбцов
        val appColumn = columns.find { it.equals("App", ignoreCase = true) } ?: "App"
        val reviewColumn = columns.find { 
            it.equals("Translated_Review", ignoreCase = true) || 
            it.equals("Review", ignoreCase = true) ||
            it.equals("review", ignoreCase = true)
        } ?: "Translated_Review"
        
        // Ищем столбец с именем пользователя
        val userNameColumn = columns.find { 
            it.equals("User", ignoreCase = true) || 
            it.equals("UserName", ignoreCase = true) ||
            it.equals("user_name", ignoreCase = true) ||
            it.equals("Name", ignoreCase = true)
        }
        
        android.util.Log.d("SQLiteDataSource", "Using columns: App=$appColumn, Review=$reviewColumn, UserName=$userNameColumn")
        android.util.Log.d("SQLiteDataSource", "Searching reviews for app: '$appName'")
        
        // Формируем SELECT с учетом доступных столбцов
        val selectColumns = mutableListOf(appColumn, reviewColumn, "Sentiment", "Sentiment_Polarity", "Sentiment_Subjectivity")
        if (userNameColumn != null) {
            selectColumns.add(userNameColumn)
        }
        
        try {
            val cursor = db.rawQuery(
                """
                SELECT ${selectColumns.joinToString(", ")}
                FROM $tableName
                WHERE $appColumn = ?
                LIMIT 50
                """.trimIndent(),
                arrayOf(appName)
            )
            
            android.util.Log.d("SQLiteDataSource", "Found ${cursor.count} reviews for app: $appName")
            
            if (cursor.moveToFirst()) {
                do {
                    try {
                        val app = cursor.getString(cursor.getColumnIndexOrThrow(appColumn))
                        val reviewText = cursor.getString(cursor.getColumnIndexOrThrow(reviewColumn))
                        
                        // Проверяем, что это отзыв для нужного приложения
                        if (app != appName) {
                            continue
                        }
                        
                        android.util.Log.d("SQLiteDataSource", "Found review: app=$app, text=${reviewText.take(50)}...")
                        
                        val sentiment = try {
                            cursor.getString(cursor.getColumnIndex("Sentiment")) ?: ""
                        } catch (_: Exception) {
                            ""
                        }
                        val polarity = try {
                            cursor.getDouble(cursor.getColumnIndex("Sentiment_Polarity"))
                        } catch (_: Exception) {
                            0.0
                        }
                        val subjectivity = try {
                            cursor.getDouble(cursor.getColumnIndex("Sentiment_Subjectivity"))
                        } catch (_: Exception) {
                            0.0
                        }
                        
                        // Получаем имя пользователя, если есть такой столбец
                        val userName = if (userNameColumn != null) {
                            try {
                                val columnIndex = cursor.getColumnIndex(userNameColumn)
                                if (columnIndex >= 0) {
                                    cursor.getString(columnIndex)?.takeIf { it.isNotBlank() } ?: "Пользователь"
                                } else {
                                    "Пользователь"
                                }
                            } catch (e: Exception) {
                                android.util.Log.w("SQLiteDataSource", "Error reading userName column: ${e.message}")
                                "Пользователь"
                            }
                        } else {
                            "Пользователь"
                        }
                        
                        android.util.Log.d("SQLiteDataSource", "Review loaded: userName=$userName, polarity=$polarity")
                        
                        list.add(ReviewData(
                            appName = app,
                            reviewText = reviewText,
                            sentiment = sentiment,
                            polarity = polarity,
                            subjectivity = subjectivity,
                            userName = userName
                        ))
                    } catch (e: Exception) {
                        android.util.Log.e("SQLiteDataSource", "Error parsing review row", e)
                    }
                } while (cursor.moveToNext())
            } else {
                android.util.Log.d("SQLiteDataSource", "No reviews found for app: $appName")
            }
            cursor.close()
        } catch (e: Exception) {
            android.util.Log.e("SQLiteDataSource", "Error reading reviews for $appName", e)
            e.printStackTrace()
        }
        
        android.util.Log.d("SQLiteDataSource", "Returning ${list.size} reviews for app: $appName")
        return list
    }
    
    /**
     * Добавляет новый отзыв в таблицу reviews
     */
    fun addReview(appName: String, reviewText: String, userName: String = "Пользователь"): Long {
        android.util.Log.d("SQLiteDataSource", "Adding review for app: $appName, text: ${reviewText.take(50)}...")
        
        // Проверяем все таблицы
        val allTablesCursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table'",
            null
        )
        val allTables = mutableListOf<String>()
        while (allTablesCursor.moveToNext()) {
            allTables.add(allTablesCursor.getString(0))
        }
        allTablesCursor.close()
        
        // Ищем таблицу reviews
        val possibleTableNames = listOf("reviews", "Reviews", "REVIEWS")
        var tableName: String? = null
        
        for (table in possibleTableNames) {
            if (allTables.contains(table)) {
                tableName = table
                break
            }
        }
        
        if (tableName == null) {
            android.util.Log.e("SQLiteDataSource", "Reviews table does not exist. Available tables: ${allTables.joinToString(", ")}")
            // Создаем таблицу, если её нет
            try {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reviews (
                        App TEXT,
                        Translated_Review TEXT,
                        Sentiment TEXT,
                        Sentiment_Polarity REAL,
                        Sentiment_Subjectivity REAL,
                        UserName TEXT
                    )
                """.trimIndent())
                tableName = "reviews"
                android.util.Log.d("SQLiteDataSource", "Created reviews table")
            } catch (e: Exception) {
                android.util.Log.e("SQLiteDataSource", "Failed to create reviews table", e)
                return -1
            }
        }
        
        // Проверяем структуру таблицы для определения правильных имен столбцов
        val tableInfoCursor = db.rawQuery("PRAGMA table_info($tableName)", null)
        val columns = mutableSetOf<String>()
        while (tableInfoCursor.moveToNext()) {
            columns.add(tableInfoCursor.getString(1))
        }
        tableInfoCursor.close()
        
        val appColumn = columns.find { it.equals("App", ignoreCase = true) } ?: "App"
        val reviewColumn = columns.find { 
            it.equals("Translated_Review", ignoreCase = true) || 
            it.equals("Review", ignoreCase = true)
        } ?: "Translated_Review"
        
        // Ищем столбец для имени пользователя
        val userNameColumn = columns.find { 
            it.equals("User", ignoreCase = true) || 
            it.equals("UserName", ignoreCase = true) ||
            it.equals("user_name", ignoreCase = true) ||
            it.equals("Name", ignoreCase = true)
        } ?: "UserName"
        
        val values = android.content.ContentValues().apply {
            put(appColumn, appName)
            put(reviewColumn, reviewText)
            if (columns.contains("Sentiment")) {
                put("Sentiment", "Positive")
            }
            if (columns.contains("Sentiment_Polarity")) {
                put("Sentiment_Polarity", 0.5)
            }
            if (columns.contains("Sentiment_Subjectivity")) {
                put("Sentiment_Subjectivity", 0.5)
            }
            // Сохраняем имя пользователя, если столбец существует
            if (columns.contains(userNameColumn)) {
                put(userNameColumn, userName)
            } else if (columns.contains("UserName")) {
                put("UserName", userName)
            }
        }
        
        try {
            val result = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            android.util.Log.d("SQLiteDataSource", "Review added successfully with id: $result")
            return result
        } catch (e: Exception) {
            android.util.Log.e("SQLiteDataSource", "Error adding review", e)
            e.printStackTrace()
            return -1
        }
    }
}

/**
 * Модель данных отзыва из БД
 */
data class ReviewData(
    val appName: String,
    val reviewText: String,
    val sentiment: String = "",
    val polarity: Double = 0.0,
    val subjectivity: Double = 0.0,
    val userName: String = "Пользователь"
)