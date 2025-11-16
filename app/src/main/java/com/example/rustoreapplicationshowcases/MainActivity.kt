package com.example.rustoreapplicationshowcases

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.compose.rememberNavController
import com.example.rustoreapplicationshowcases.data.PreferencesManager
import com.example.rustoreapplicationshowcases.navigation.AppNavHost
import com.example.rustoreapplicationshowcases.ui.theme.RuStoreApplicationShowcasesTheme
import com.example.rustoreapplicationshowcases.data.local.PreloadedDatabase

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PreloadedDatabase.copyDatabaseIfNeeded(this)

        val prefs = PreferencesManager(this)
        val showOnboarding = true
        // val showOnboarding = !prefs.isOnboardingShown()

        enableEdgeToEdge()

        setContent {
            val systemDarkTheme = isSystemInDarkTheme()
            var themePreference by remember { mutableStateOf(prefs.getThemePreference()) }
            
            val darkTheme = when (themePreference) {
                "dark" -> true
                "light" -> false
                "system", null -> systemDarkTheme
                else -> systemDarkTheme
            }
            
            val toggleTheme: () -> Unit = {
                val currentPreference = prefs.getThemePreference()
                val newPreference = when (currentPreference) {
                    "dark" -> "light"
                    "light" -> "dark"
                    "system", null -> if (systemDarkTheme) "light" else "dark"
                    else -> if (systemDarkTheme) "light" else "dark"
                }
                prefs.setThemePreference(newPreference)
                themePreference = newPreference
            }
            
            RuStoreApplicationShowcasesTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                AppNavHost(
                    navController = navController,
                    showOnboarding = showOnboarding,
                    onFinishOnboarding = {
                        prefs.setOnboardingShown()
                    },
                    onToggleTheme = toggleTheme,
                    isDarkTheme = darkTheme
                )
            }
        }
    }
}
