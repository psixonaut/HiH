package com.example.rustoreapplicationshowcases.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.example.rustoreapplicationshowcases.data.model.User
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "googleplay.db"
        private const val ASSETS_DB_NAME = "googleplay.sqlite"
        private const val DATABASE_VERSION = 3
        
        // Users table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_NAME = "name"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_PASSWORD = "password"
        
        // Apps table - реальная структура из БД в assets
        private const val TABLE_APPS = "apps"
        private const val COLUMN_APP = "App"  // Название приложения
        private const val COLUMN_CATEGORY = "Category"
        private const val COLUMN_RATING = "Rating"
        private const val COLUMN_REVIEWS = "Reviews"
        private const val COLUMN_SIZE = "Size"
        private const val COLUMN_INSTALLS = "Installs"
        private const val COLUMN_TYPE = "Type"
        private const val COLUMN_PRICE = "Price"
        private const val COLUMN_CONTENT_RATING = "Content_Rating"
        private const val COLUMN_GENRES = "Genres"
        private const val COLUMN_LAST_UPDATED = "Last_Updated"
        private const val COLUMN_CURRENT_VER = "Current_Ver"
        private const val COLUMN_ANDROID_VER = "Android_Ver"
        
        // Для обратной совместимости с нашей моделью
        private const val COLUMN_APP_ID = "id"
        private const val COLUMN_APP_NAME = "name"
        private const val COLUMN_APP_ICON = "icon"
        private const val COLUMN_APP_RATING = "rating"
        private const val COLUMN_APP_CATEGORY = "category"
        private const val COLUMN_APP_DOWNLOADS = "downloads"
    }

    init {
        // Копируем БД из assets при первом запуске, если её еще нет
        copyDatabaseFromAssets(context)
    }
    
    private fun copyDatabaseFromAssets(context: Context) {
        val dbPath = context.getDatabasePath(DATABASE_NAME)
        val dbFile = File(dbPath.parent)
        
        if (!dbFile.exists()) {
            dbFile.mkdirs()
        }
        
        // Проверяем, существует ли уже БД
        if (dbPath.exists()) {
            Log.d("DatabaseHelper", "Database already exists")
            return
        }
        
        try {
            // Копируем БД из assets
            val inputStream = context.assets.open(ASSETS_DB_NAME)
            val outputStream = FileOutputStream(dbPath)
            
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            
            Log.d("DatabaseHelper", "Database copied from assets successfully")
            // Проверяем и создаем таблицы, если их нет
            ensureTablesExist()
        } catch (e: IOException) {
            Log.e("DatabaseHelper", "Error copying database from assets", e)
            // Если не удалось скопировать, создаем новую БД
            createNewDatabase(context)
        }
    }
    
    private fun createNewDatabase(context: Context) {
        Log.d("DatabaseHelper", "Creating new database")
        // БД будет создана через onCreate при первом обращении
        ensureTablesExist()
    }

    override fun onCreate(db: SQLiteDatabase) {
        // onCreate вызывается только если БД не существует
        // Создаем таблицы
        val createUsersTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT NOT NULL,
                $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_USER_PASSWORD TEXT
            )
        """.trimIndent()
        
        val createAppsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_APPS (
                $COLUMN_APP_ID TEXT PRIMARY KEY,
                $COLUMN_APP_NAME TEXT NOT NULL,
                $COLUMN_APP_ICON TEXT NOT NULL,
                $COLUMN_APP_RATING REAL NOT NULL,
                $COLUMN_APP_CATEGORY TEXT NOT NULL,
                $COLUMN_APP_DOWNLOADS INTEGER DEFAULT 0
            )
        """.trimIndent()
        
        db.execSQL(createUsersTable)
        db.execSQL(createAppsTable)
        Log.d("DatabaseHelper", "Database tables created in onCreate")
    }
    
    /**
     * Проверяет и создает необходимые таблицы, если их нет
     * Вызывается после копирования БД из assets
     */
    fun ensureTablesExist() {
        val db = readableDatabase
        try {
            // Проверяем наличие таблицы apps
            val cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_APPS'",
                null
            )
            val appsTableExists = cursor.count > 0
            cursor.close()
            
            // Проверяем наличие таблицы users
            val cursorUsers = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_USERS'",
                null
            )
            val usersTableExists = cursorUsers.count > 0
            cursorUsers.close()
            
            if (!appsTableExists || !usersTableExists) {
                db.close()
                val writableDb = writableDatabase
                if (!usersTableExists) {
                    val createUsersTable = """
                        CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                            $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                            $COLUMN_USER_NAME TEXT NOT NULL,
                            $COLUMN_USER_EMAIL TEXT UNIQUE NOT NULL,
                            $COLUMN_USER_PASSWORD TEXT
                        )
                    """.trimIndent()
                    writableDb.execSQL(createUsersTable)
                    Log.d("DatabaseHelper", "Users table created")
                }
                if (!appsTableExists) {
                    val createAppsTable = """
                        CREATE TABLE IF NOT EXISTS $TABLE_APPS (
                            $COLUMN_APP_ID TEXT PRIMARY KEY,
                            $COLUMN_APP_NAME TEXT NOT NULL,
                            $COLUMN_APP_ICON TEXT NOT NULL,
                            $COLUMN_APP_RATING REAL NOT NULL,
                            $COLUMN_APP_CATEGORY TEXT NOT NULL,
                            $COLUMN_APP_DOWNLOADS INTEGER DEFAULT 0
                        )
                    """.trimIndent()
                    writableDb.execSQL(createAppsTable)
                    Log.d("DatabaseHelper", "Apps table created")
                }
                writableDb.close()
            } else {
                Log.d("DatabaseHelper", "All tables exist")
            }
        } finally {
            db.close()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            onCreate(db)
        }
        if (oldVersion < 3) {
            val createAppsTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_APPS (
                    $COLUMN_APP_ID TEXT PRIMARY KEY,
                    $COLUMN_APP_NAME TEXT NOT NULL,
                    $COLUMN_APP_ICON TEXT NOT NULL,
                    $COLUMN_APP_RATING REAL NOT NULL,
                    $COLUMN_APP_CATEGORY TEXT NOT NULL,
                    $COLUMN_APP_DOWNLOADS INTEGER DEFAULT 0
                )
            """.trimIndent()
            db.execSQL(createAppsTable)
            Log.d("DatabaseHelper", "Apps table created in upgrade")
        }
    }

    fun insertUser(user: User): Long {
        val db = writableDatabase
        val values = android.content.ContentValues().apply {
            put(COLUMN_USER_NAME, user.name)
            put(COLUMN_USER_EMAIL, user.email)
            put(COLUMN_USER_PASSWORD, user.password)
        }
        val id = db.insert(TABLE_USERS, null, values)
        db.close()
        return id
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID, COLUMN_USER_NAME, COLUMN_USER_EMAIL, COLUMN_USER_PASSWORD),
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )
        
        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD))
            )
            cursor.close()
            db.close()
            user
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    fun updateUser(user: User): Boolean {
        val db = writableDatabase
        val values = android.content.ContentValues().apply {
            put(COLUMN_USER_NAME, user.name)
            put(COLUMN_USER_EMAIL, user.email)
            if (user.password != null) {
                put(COLUMN_USER_PASSWORD, user.password)
            }
        }
        val rowsAffected = db.update(
            TABLE_USERS,
            values,
            "$COLUMN_USER_ID = ?",
            arrayOf(user.id.toString())
        )
        db.close()
        return rowsAffected > 0
    }

    fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return hashPassword(password) == hashedPassword
    }
    
    // Apps table methods
    fun insertApp(app: AppInfo): Long {
        val db = writableDatabase
        
        // Проверяем структуру таблицы
        val cursor = db.rawQuery("PRAGMA table_info($TABLE_APPS)", null)
        val columns = mutableListOf<String>()
        while (cursor.moveToNext()) {
            columns.add(cursor.getString(1))
        }
        cursor.close()
        
        val hasNewStructure = columns.contains(COLUMN_APP)
        
        val values = android.content.ContentValues()
        if (hasNewStructure) {
            // Новая структура из assets
            values.put(COLUMN_APP, app.name)
            values.put(COLUMN_CATEGORY, app.category)
            values.put(COLUMN_RATING, app.rating)
            values.put(COLUMN_INSTALLS, "${app.downloads}+")
        } else {
            // Старая структура
            values.put(COLUMN_APP_ID, app.id)
            values.put(COLUMN_APP_NAME, app.name)
            values.put(COLUMN_APP_ICON, app.icon)
            values.put(COLUMN_APP_RATING, app.rating)
            values.put(COLUMN_APP_CATEGORY, app.category)
            values.put(COLUMN_APP_DOWNLOADS, app.downloads)
        }
        
        val result = db.insertWithOnConflict(TABLE_APPS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
        return result
    }
    
    fun insertApps(apps: List<AppInfo>): Int {
        val db = writableDatabase
        
        // Проверяем структуру таблицы один раз
        val cursor = db.rawQuery("PRAGMA table_info($TABLE_APPS)", null)
        val columns = mutableListOf<String>()
        while (cursor.moveToNext()) {
            columns.add(cursor.getString(1))
        }
        cursor.close()
        val hasNewStructure = columns.contains(COLUMN_APP)
        
        var count = 0
        try {
            db.beginTransaction()
            apps.forEach { app ->
                val values = android.content.ContentValues()
                if (hasNewStructure) {
                    // Новая структура
                    values.put(COLUMN_APP, app.name)
                    values.put(COLUMN_CATEGORY, app.category)
                    values.put(COLUMN_RATING, app.rating)
                    values.put(COLUMN_INSTALLS, "${app.downloads}+")
                } else {
                    // Старая структура
                    values.put(COLUMN_APP_ID, app.id)
                    values.put(COLUMN_APP_NAME, app.name)
                    values.put(COLUMN_APP_ICON, app.icon)
                    values.put(COLUMN_APP_RATING, app.rating)
                    values.put(COLUMN_APP_CATEGORY, app.category)
                    values.put(COLUMN_APP_DOWNLOADS, app.downloads)
                }
                db.insertWithOnConflict(TABLE_APPS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
                count++
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
        Log.d("DatabaseHelper", "Inserted $count apps")
        return count
    }
    
    fun getAllApps(): List<AppInfo> {
        val db = readableDatabase
        val apps = mutableListOf<AppInfo>()
        
        // Сначала проверяем, какая структура у таблицы
        val cursor = db.rawQuery("PRAGMA table_info($TABLE_APPS)", null)
        val columns = mutableListOf<String>()
        while (cursor.moveToNext()) {
            columns.add(cursor.getString(1)) // column name
        }
        cursor.close()
        
        // Определяем, какая структура БД используется
        val hasNewStructure = columns.contains(COLUMN_APP)
        val hasOldStructure = columns.contains(COLUMN_APP_NAME)
        
        if (hasNewStructure) {
            // Используем реальную структуру из assets
            val queryCursor = db.query(
                TABLE_APPS,
                arrayOf(COLUMN_APP, COLUMN_CATEGORY, COLUMN_RATING, COLUMN_REVIEWS, COLUMN_INSTALLS),
                null, null, null, null, null
            )
            
            var index = 0
            while (queryCursor.moveToNext()) {
                val appName = queryCursor.getString(queryCursor.getColumnIndex(COLUMN_APP))
                val category = queryCursor.getString(queryCursor.getColumnIndex(COLUMN_CATEGORY))
                val rating = queryCursor.getDouble(queryCursor.getColumnIndex(COLUMN_RATING))
                
                // Парсим количество установок из строки (например, "1,000,000+")
                val installsStr = queryCursor.getString(queryCursor.getColumnIndex(COLUMN_INSTALLS))
                val downloads = parseInstallsToDownloads(installsStr)
                
                // Генерируем ID на основе названия приложения
                val appId = appName.hashCode().toString()
                
                apps.add(
                    AppInfo(
                        id = appId,
                        name = appName,
                        icon = "ic_rustoresvglogo", // Дефолтная иконка
                        rating = rating,
                        category = category ?: "Неизвестно",
                        downloads = downloads
                    )
                )
                index++
            }
            queryCursor.close()
        } else if (hasOldStructure) {
            // Используем старую структуру
            val queryCursor = db.query(
                TABLE_APPS,
                arrayOf(COLUMN_APP_ID, COLUMN_APP_NAME, COLUMN_APP_ICON, COLUMN_APP_RATING, COLUMN_APP_CATEGORY, COLUMN_APP_DOWNLOADS),
                null, null, null, null, null
            )
            
            while (queryCursor.moveToNext()) {
                apps.add(
                    AppInfo(
                        id = queryCursor.getString(queryCursor.getColumnIndexOrThrow(COLUMN_APP_ID)),
                        name = queryCursor.getString(queryCursor.getColumnIndexOrThrow(COLUMN_APP_NAME)),
                        icon = queryCursor.getString(queryCursor.getColumnIndexOrThrow(COLUMN_APP_ICON)),
                        rating = queryCursor.getDouble(queryCursor.getColumnIndexOrThrow(COLUMN_APP_RATING)),
                        category = queryCursor.getString(queryCursor.getColumnIndexOrThrow(COLUMN_APP_CATEGORY)),
                        downloads = queryCursor.getInt(queryCursor.getColumnIndexOrThrow(COLUMN_APP_DOWNLOADS))
                    )
                )
            }
            queryCursor.close()
        }
        
        db.close()
        Log.d("DatabaseHelper", "Loaded ${apps.size} apps from database")
        return apps
    }
    
    /**
     * Парсит строку установок в число
     * Например: "1,000,000+" -> 1000000, "500+" -> 500
     */
    private fun parseInstallsToDownloads(installsStr: String?): Int {
        if (installsStr.isNullOrBlank()) return 0
        return try {
            // Удаляем все символы кроме цифр
            val cleaned = installsStr.replace(Regex("[^0-9]"), "")
            cleaned.toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }
    
    fun getAppById(id: String): AppInfo? {
        val db = readableDatabase
        
        // Проверяем структуру таблицы
        val cursor = db.rawQuery("PRAGMA table_info($TABLE_APPS)", null)
        val columns = mutableListOf<String>()
        while (cursor.moveToNext()) {
            columns.add(cursor.getString(1))
        }
        cursor.close()
        
        val hasNewStructure = columns.contains(COLUMN_APP)
        
        if (hasNewStructure) {
            // Ищем по названию приложения (используем id как hash названия)
            val allApps = getAllApps()
            db.close()
            return allApps.find { it.id == id }
        } else {
            // Старая структура
            val queryCursor = db.query(
                TABLE_APPS,
                arrayOf(COLUMN_APP_ID, COLUMN_APP_NAME, COLUMN_APP_ICON, COLUMN_APP_RATING, COLUMN_APP_CATEGORY, COLUMN_APP_DOWNLOADS),
                "$COLUMN_APP_ID = ?",
                arrayOf(id),
                null, null, null
            )
            
            return if (queryCursor.moveToFirst()) {
                val app = AppInfo(
                    id = queryCursor.getString(queryCursor.getColumnIndexOrThrow(COLUMN_APP_ID)),
                    name = queryCursor.getString(queryCursor.getColumnIndexOrThrow(COLUMN_APP_NAME)),
                    icon = queryCursor.getString(queryCursor.getColumnIndexOrThrow(COLUMN_APP_ICON)),
                    rating = queryCursor.getDouble(queryCursor.getColumnIndexOrThrow(COLUMN_APP_RATING)),
                    category = queryCursor.getString(queryCursor.getColumnIndexOrThrow(COLUMN_APP_CATEGORY)),
                    downloads = queryCursor.getInt(queryCursor.getColumnIndexOrThrow(COLUMN_APP_DOWNLOADS))
                )
                queryCursor.close()
                db.close()
                app
            } else {
                queryCursor.close()
                db.close()
                null
            }
        }
    }
    
    fun updateApp(app: AppInfo): Boolean {
        val db = writableDatabase
        
        // Проверяем структуру таблицы
        val cursor = db.rawQuery("PRAGMA table_info($TABLE_APPS)", null)
        val columns = mutableListOf<String>()
        while (cursor.moveToNext()) {
            columns.add(cursor.getString(1))
        }
        cursor.close()
        
        val hasNewStructure = columns.contains(COLUMN_APP)
        
        if (hasNewStructure) {
            // Для новой структуры обновляем только если есть соответствующие колонки
            // В новой структуре нет downloads, поэтому просто возвращаем true
            // Можно обновить Rating, если нужно
            val values = android.content.ContentValues().apply {
                put(COLUMN_RATING, app.rating)
            }
            // Ищем по названию приложения
            val rowsAffected = db.update(
                TABLE_APPS,
                values,
                "$COLUMN_APP = ?",
                arrayOf(app.name)
            )
            db.close()
            return rowsAffected > 0
        } else {
            // Старая структура
            val values = android.content.ContentValues().apply {
                put(COLUMN_APP_NAME, app.name)
                put(COLUMN_APP_ICON, app.icon)
                put(COLUMN_APP_RATING, app.rating)
                put(COLUMN_APP_CATEGORY, app.category)
                put(COLUMN_APP_DOWNLOADS, app.downloads)
            }
            val rowsAffected = db.update(
                TABLE_APPS,
                values,
                "$COLUMN_APP_ID = ?",
                arrayOf(app.id)
            )
            db.close()
            return rowsAffected > 0
        }
    }
    
    fun deleteAllApps(): Int {
        val db = writableDatabase
        val count = db.delete(TABLE_APPS, null, null)
        db.close()
        Log.d("DatabaseHelper", "Deleted $count apps")
        return count
    }
    
    fun getAppsCount(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_APPS", null)
        val count = if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
        cursor.close()
        db.close()
        return count
    }
}

