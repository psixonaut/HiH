package com.example.rustoreapplicationshowcases.ui.search

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.rustoreapplicationshowcases.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rustoreapplicationshowcases.AppViewModelFactory
import com.example.rustoreapplicationshowcases.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllCategoriesScreen(
    navController: NavController,
    onToggleTheme: () -> Unit = {},
    isDarkTheme: Boolean = false
) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: HomeViewModel = viewModel(
        factory = AppViewModelFactory(context)
    )
    
    val allCategories = remember { viewModel.getCategoriesWithAppCount() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Все категории") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            painter = painterResource(
                                id = if (isDarkTheme) R.drawable.ic_lighttheme else R.drawable.ic_nighttheme
                            ),
                            contentDescription = if (isDarkTheme) "Светлая тема" else "Тёмная тема"
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(allCategories) { (category, count) ->
                CategoryCard(
                    category = category,
                    appCount = count,
                    onClick = {
                        navController.navigate("categoryApps/$category")
                    }
                )
            }
        }
    }
}

