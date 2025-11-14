package com.example.rustoreapplicationshowcases

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.rustoreapplicationshowcases.data.PreferencesManager
import com.example.rustoreapplicationshowcases.navigation.AppNavHost
import com.example.rustoreapplicationshowcases.ui.theme.RuStoreApplicationShowcasesTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferencesManager(this)
        val showOnboarding = !prefs.isOnboardingShown()

        enableEdgeToEdge()

        setContent {
            RuStoreApplicationShowcasesTheme {
                val navController = rememberNavController()

                AppNavHost(
                    navController = navController,
                    showOnboarding = showOnboarding,
                    onFinishOnboarding = {
                        prefs.setOnboardingShown()
                    }
                )
            }
        }
    }
}
