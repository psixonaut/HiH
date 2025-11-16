package com.example.rustoreapplicationshowcases.data.repository


import android.content.Context
import com.example.rustoreapplicationshowcases.data.local.SQLiteDataSource
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.example.rustoreapplicationshowcases.utils.CategoryMapper

class AppRepository(context: Context) {

    private val db = SQLiteDataSource(context)

    /** Получить все приложения */
    fun getAllApps(): List<AppInfo> =
        db.getAllApps()

    /** Получить список уникальных категорий */
    fun getAllCategories(): List<String> {
        val raw = db.getDistinctCategories()
        val mapped = raw.mapNotNull { CategoryMapper.toRussian(it) }
        val sorted = mapped.sorted()
        return sorted + "Другое"
    }
}