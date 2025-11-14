package com.example.rustoreapplicationshowcases

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.rustoreapplicationshowcases.navigation.AppNavGraph
import com.example.rustoreapplicationshowcases.ui.theme.RuStoreApplicationShowcasesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RuStoreApplicationShowcasesTheme {
                val navController = rememberNavController()
                AppNavGraph(navController)
            }
        }
    }
}