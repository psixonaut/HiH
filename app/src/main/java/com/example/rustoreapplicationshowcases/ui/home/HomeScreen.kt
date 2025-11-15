package com.example.rustoreapplicationshowcases.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import com.example.rustoreapplicationshowcases.AppViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nav: NavController,
    categoryFilter: String? = null,
) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: HomeViewModel = viewModel(
        factory = AppViewModelFactory(context)
    )

    val searchQuery = viewModel.searchQuery
    val apps = viewModel.getFilteredApps(categoryFilter)

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

        Column(modifier = Modifier.padding(padding)) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Поиск приложений") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp) // Тестовое скругление
            )

            LazyColumn {
                items(apps) { app ->
                    AppCard(app)
                }
            }
        }
    }
}
