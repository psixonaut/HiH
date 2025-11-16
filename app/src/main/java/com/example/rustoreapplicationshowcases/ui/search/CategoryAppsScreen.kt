package com.example.rustoreapplicationshowcases.ui.search

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.rustoreapplicationshowcases.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rustoreapplicationshowcases.AppViewModelFactory
import com.example.rustoreapplicationshowcases.ui.home.AppCard
import com.example.rustoreapplicationshowcases.ui.home.HomeViewModel
import com.example.rustoreapplicationshowcases.data.model.SortType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryAppsScreen(
    navController: NavController,
    categoryName: String,
    onToggleTheme: () -> Unit = {},
    isDarkTheme: Boolean = false
) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: HomeViewModel = viewModel(
        factory = AppViewModelFactory(context)
    )
    
    // Получаем актуальные данные из StateFlow
    val allAppsState = viewModel.apps.collectAsState()
    val allApps = allAppsState.value
    
    // Состояние сортировки
    var sortType by remember { mutableStateOf<SortType?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    // Вычисляем приложения категории на основе актуальных данных и применяем сортировку
    val apps = remember(categoryName, allApps, sortType) {
        val categoryApps = viewModel.getAppsByCategory(categoryName)
        if (sortType != null) {
            viewModel.sortApps(categoryApps, sortType!!)
        } else {
            categoryApps
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Сортировка")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("По алфавиту") },
                                onClick = {
                                    sortType = SortType.ALPHABETICAL
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("По оценке") },
                                onClick = {
                                    sortType = SortType.RATING
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("По скачиваниям") },
                                onClick = {
                                    sortType = SortType.DOWNLOADS
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Сбросить") },
                                onClick = {
                                    sortType = null
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps) { app ->
                AppCard(
                    app = app,
                    onClick = {
                        viewModel.onAppClicked(app)
                        navController.navigate("details/${app.name}")
                    }
                )
            }
        }
    }
}

