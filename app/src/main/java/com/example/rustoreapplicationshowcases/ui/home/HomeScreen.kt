package com.example.rustoreapplicationshowcases.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.rustoreapplicationshowcases.ui.home.AppCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nav: NavHostController,
    categoryFilter: String? = null,
    viewModel: HomeViewModel = viewModel()
) {
    val apps = if (categoryFilter == null)
        viewModel.apps
    else
        viewModel.apps.filter { it.category == categoryFilter }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Витрина приложений") },
                actions = {
                    IconButton(onClick = { nav.navigate("categories") }) {
                        Icon(Icons.Default.List, contentDescription = "Категории")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {
            items(apps) { app ->
                AppCard(app)
            }
        }
    }
}
