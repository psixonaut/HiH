package com.example.rustoreapplicationshowcases.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.rustoreapplicationshowcases.data.local.DatabaseHelper
import com.example.rustoreapplicationshowcases.data.model.User
import com.example.rustoreapplicationshowcases.data.PreferencesManager

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val dbHelper = DatabaseHelper(application)
    private val preferencesManager = PreferencesManager(application)
    
    var currentUser by mutableStateOf<User?>(null)
        private set
    
    var isLoggedIn by mutableStateOf(false)
        private set
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        val userId = preferencesManager.getCurrentUserId()
        if (userId != null) {
            currentUser = getUserById(userId)
            isLoggedIn = currentUser != null
        }
    }
    
    fun login(email: String, password: String): Boolean {
        val user = dbHelper.getUserByEmail(email)
        return if (user != null && dbHelper.verifyPassword(password, user.password ?: "")) {
            currentUser = user
            isLoggedIn = true
            preferencesManager.setCurrentUserId(user.id)
            preferencesManager.setUserEmail(user.id, user.email)
            true
        } else {
            false
        }
    }
    
    fun register(name: String, email: String, password: String): Boolean {
        return try {
            val hashedPassword = dbHelper.hashPassword(password)
            val user = User(
                name = name,
                email = email,
                password = hashedPassword
            )
            val userId = dbHelper.insertUser(user)
            if (userId > 0) {
                val newUser = user.copy(id = userId)
                currentUser = newUser
                isLoggedIn = true
                preferencesManager.setCurrentUserId(userId)
                preferencesManager.setUserEmail(userId, email)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun logout() {
        currentUser = null
        isLoggedIn = false
        preferencesManager.clearCurrentUserId()
    }
    
    fun updateUser(name: String, email: String): Boolean {
        val user = currentUser ?: return false
        val updatedUser = user.copy(name = name, email = email)
        return if (dbHelper.updateUser(updatedUser)) {
            currentUser = updatedUser
            true
        } else {
            false
        }
    }
    
    private fun getUserById(id: Long): User? {
        // Простая реализация - можно улучшить
        val email = preferencesManager.getUserEmail(id)
        return if (email != null) {
            dbHelper.getUserByEmail(email)
        } else {
            null
        }
    }
}

