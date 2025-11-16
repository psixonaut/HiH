package com.example.rustoreapplicationshowcases.data.model

data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val password: String? = null // Хранится только хеш пароля
)

