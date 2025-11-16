package com.example.rustoreapplicationshowcases.ui.home

import android.app.Application
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rustoreapplicationshowcases.AppViewModelFactory
import com.example.rustoreapplicationshowcases.R
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionScreen(
    navController: NavController,
    onToggleTheme: () -> Unit = {},
    isDarkTheme: Boolean = false
) {
    val context = LocalContext.current
    val viewModel: CategorySelectionViewModel = viewModel(
        factory = AppViewModelFactory(context.applicationContext as Application)
    )
    
    // Force recomposition when selectedCategories changes
    val selectedCategories = viewModel.selectedCategories

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выбор категорий") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    // Кнопка смены темы убрана
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    val selectedCategoriesString = viewModel.getSelectedCategoriesAsString()
                    navController.previousBackStackEntry?.savedStateHandle?.set("selectedCategories", selectedCategoriesString)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Применить изменения")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(bottom = 80.dp) // Space for bottom button
        ) {
            // Selected categories section (sorted alphabetically)
            val sortedSelected = selectedCategories.sorted()
            if (sortedSelected.isNotEmpty()) {
                item {
                    Text(
                        text = "Выбранные категории",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                items(sortedSelected) { category ->
                    CategorySelectionItem(
                        category = category,
                        isSelected = true,
                        onCategoryClick = { viewModel.toggleCategorySelection(it) }
                    )
                }
                
                item {
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }
                
                item {
                    Text(
                        text = "Все категории",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            
            // All other categories (sorted alphabetically)
            val sortedUnselected = viewModel.allCategories
                .filter { !selectedCategories.contains(it) }
                .sorted()
            
            items(sortedUnselected) { category ->
                CategorySelectionItem(
                    category = category,
                    isSelected = false,
                    onCategoryClick = { viewModel.toggleCategorySelection(it) }
                )
            }
        }
    }
}

@Composable
fun CategorySelectionItem(
    category: String,
    isSelected: Boolean,
    onCategoryClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = category, 
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .weight(1f)
                .clickable { onCategoryClick(category) }
        )
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onCategoryClick(category) }
        )
    }
}
