package com.example.rustoreapplicationshowcases.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File
import java.io.FileOutputStream

object PreloadedDatabase {

    private const val DB_NAME = "googleplay.sqlite"

    fun copyDatabaseIfNeeded(context: Context) {
        val dbPath = context.getDatabasePath(DB_NAME)

        // каталог может отсутствовать
        if (!dbPath.parentFile.exists()) {
            dbPath.parentFile.mkdirs()
        }

        // если база уже есть — не копируем
        if (dbPath.exists()) return

        context.assets.open(DB_NAME).use { input ->
            FileOutputStream(dbPath).use { output ->
                input.copyTo(output)
            }
        }
    }

    fun openDatabase(context: Context): SQLiteDatabase {
        val dbPath = context.getDatabasePath(DB_NAME).path
        return SQLiteDatabase.openDatabase(
            dbPath,
            null,
            SQLiteDatabase.OPEN_READWRITE
        )
    }
}