package com.example.rustoreapplicationshowcases.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rustoreapplicationshowcases.ui.home.AppCard
import com.example.rustoreapplicationshowcases.data.repository.AppRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Витрина приложений") },
                actions = {
                    TextButton(onClick = {
                        nav.navigate("categories")
                    }) {
                        Text("Категории")
                    }
                }
            )
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {
            AppRepository.apps.forEach { app ->
                AppCard(app)
            }
        }
    }
}