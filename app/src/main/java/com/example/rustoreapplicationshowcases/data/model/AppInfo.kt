package com.example.rustoreapplicationshowcases.data.model

data class AppInfo(
    val id: String,
    val name: String,
    val rating: Double,
    val category: String,
    val icon: String,
    var downloads: Int = 0,
    var reviews: Int = 0,
    var ageRating: String = "0+",
    var publisher: String = ""
)