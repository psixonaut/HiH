package com.example.rustoreapplicationshowcases.data.model

data class AppInfo(
    val name: String,
    val rating: Double,
    val category: String,
    val icon: String,
    var downloads: Int = 0
)