package com.example.rustoreapplicationshowcases.data.model

data class AppInfo(
    val id: String,
    val icon: String,
    val name: String,
    val rating: Double,
    val category: String,
    var downloads: Int = 0
)