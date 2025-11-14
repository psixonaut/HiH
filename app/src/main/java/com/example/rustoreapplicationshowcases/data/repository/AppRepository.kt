package com.example.rustoreapplicationshowcases.data.repository

import com.example.rustoreapplicationshowcases.R
import com.example.rustoreapplicationshowcases.ui.home.model.AppInfo

object AppRepository {

    val apps = listOf(
        AppInfo(R.drawable.ic_app_placeholder, "Сбербанк", 4.8, "Финансы"),
        AppInfo(R.drawable.ic_app_placeholder, "Госуслуги", 4.5, "Государственные"),
        AppInfo(R.drawable.ic_app_placeholder, "Яндекс Такси", 4.7, "Транспорт"),
        AppInfo(R.drawable.ic_app_placeholder, "VK", 4.4, "Инструменты"),
        AppInfo(R.drawable.ic_app_placeholder, "War Robots", 4.6, "Игры")
    )
}