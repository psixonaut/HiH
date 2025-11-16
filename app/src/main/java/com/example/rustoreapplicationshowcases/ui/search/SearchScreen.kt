package com.example.rustoreapplicationshowcases.ui.search

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.rustoreapplicationshowcases.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rustoreapplicationshowcases.AppViewModelFactory
import com.example.rustoreapplicationshowcases.ui.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    onToggleTheme: () -> Unit = {},
    isDarkTheme: Boolean = false
) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: HomeViewModel = viewModel(
        factory = AppViewModelFactory(context)
    )
    
    var searchQuery by remember { mutableStateOf("") }
    val topCategories = remember { viewModel.getTopCategoriesByAppCount(8) }
    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList() else viewModel.searchApps(searchQuery)
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        focusRequester = focusRequester
                    )
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
        if (searchQuery.isBlank()) {
            // Show categories when search is empty
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    text = "Категории",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(topCategories) { (category, count) ->
                        CategoryCard(
                            category = category,
                            appCount = count,
                            onClick = {
                                navController.navigate("categoryApps/$category")
                            }
                        )
                    }
                }
                
                Button(
                    onClick = {
                        navController.navigate("allCategories")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Показать больше")
                }
            }
        } else {
            // Show search results
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (searchResults.isEmpty()) {
                    item {
                        Text(
                            text = "Ничего не найдено",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "Найдено: ${searchResults.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(searchResults) { app ->
                        com.example.rustoreapplicationshowcases.ui.home.AppCard(
                            app = app,
                            onClick = {
                                viewModel.onAppClicked(app)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Поиск приложений и игр") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Поиск")
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        shape = RoundedCornerShape(24.dp),
        singleLine = true
    )
}

@Composable
fun CategoryCard(
    category: String,
    appCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2
            )
            Text(
                text = "$appCount приложений",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

