package com.example.rustoreapplicationshowcases.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object PreloadedDatabase {

    private const val DB_NAME = "apps.sqlite"
    private const val TAG = "PreloadedDatabase"

    fun copyDatabaseIfNeeded(context: Context) {
        val dbPath = context.getDatabasePath(DB_NAME)
        
        Log.d(TAG, "Database path: ${dbPath.absolutePath}")

        // каталог может отсутствовать
        if (!dbPath.parentFile.exists()) {
            dbPath.parentFile.mkdirs()
            Log.d(TAG, "Created database directory")
        }

        // если база уже есть — не копируем
        if (dbPath.exists()) {
            Log.d(TAG, "Database already exists, skipping copy")
            return
        }

        try {
            Log.d(TAG, "Copying database from assets...")
            context.assets.open(DB_NAME).use { input ->
                FileOutputStream(dbPath).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d(TAG, "Database copied successfully, size: ${dbPath.length()} bytes")
        } catch (e: Exception) {
            Log.e(TAG, "Error copying database from assets", e)
            throw e
        }
    }

    fun openDatabase(context: Context): SQLiteDatabase {
        val dbPath = context.getDatabasePath(DB_NAME)
        
        if (!dbPath.exists()) {
            Log.e(TAG, "Database file does not exist at: ${dbPath.absolutePath}")
            throw IllegalStateException("Database file not found")
        }
        
        Log.d(TAG, "Opening database from: ${dbPath.absolutePath}")
        val db = SQLiteDatabase.openDatabase(
            dbPath.path,
            null,
            SQLiteDatabase.OPEN_READWRITE
        )
        
        // Проверяем структуру таблицы apps
        val cursor = db.rawQuery("PRAGMA table_info(apps)", null)
        val columns = mutableListOf<String>()
        while (cursor.moveToNext()) {
            columns.add(cursor.getString(1))
        }
        cursor.close()
        Log.d(TAG, "Apps table columns: ${columns.joinToString(", ")}")
        
        return db
    }
}