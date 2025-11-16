package com.example.rustoreapplicationshowcases.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rustoreapplicationshowcases.AppViewModelFactory
import com.example.rustoreapplicationshowcases.R
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.example.rustoreapplicationshowcases.data.model.SortType
import com.example.rustoreapplicationshowcases.data.PreferencesManager
import com.example.rustoreapplicationshowcases.ui.common.CustomBottomNavigationBar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HeroImageSection(
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp)
    ) {
        // Hero image with background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() }
        ) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
            
            // Background image
            Image(
                painter = painterResource(id = R.drawable.ic_rustore_logo),
                contentDescription = "Выбор редакции",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.2f),
                contentScale = ContentScale.Crop
            )
            
            // Overlay text
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Выбор редакции",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Топ недели",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SectionWithTitle(
    title: String,
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp), // Adjusted padding
            modifier = Modifier.fillMaxWidth()
        ) {
            items(apps) { app ->
                AppCardHorizontal(
                    app = app,
                    onClick = { onAppClick(app) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nav: NavController,
    categoryFilter: String? = null,
    onToggleTheme: () -> Unit = {},
    isDarkTheme: Boolean = false,
    selectedCategories: String? = null
) {
    val context = LocalContext.current.applicationContext as android.app.Application
    val viewModel: HomeViewModel = viewModel(
        factory = AppViewModelFactory(context)
    )
    val preferencesManager = PreferencesManager(context)

    // Получаем актуальные данные из StateFlow
    val allAppsState = viewModel.apps.collectAsState()
    val allApps = allAppsState.value

    // Состояние сортировки (только для фильтра категории)
    var sortType by remember { mutableStateOf<SortType?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    
    // Вычисляем отфильтрованные приложения на основе актуальных данных и применяем сортировку
    val apps = remember(allApps, categoryFilter, sortType) {
        val filtered = viewModel.getFilteredApps(categoryFilter)
        if (categoryFilter != null && sortType != null) {
            viewModel.sortApps(filtered, sortType!!)
        } else {
            filtered
        }
    }
    val recommendedApps = remember(apps) { apps.take(6) }
    val preferenceApps = remember(apps) { apps.drop(6).take(6) }

    // Получаем выбранные категории (из параметра или из сохраненных настроек)
    val savedCategories = preferencesManager.getSelectedCategories()
    val categoriesToUse = selectedCategories ?: savedCategories

    // Парсим выбранные категории
    val selectedCategoriesList = remember(categoriesToUse, allApps) {
        if (categoriesToUse.isNullOrBlank()) {
            // Если категории не выбраны, используем топ категории из БД
            viewModel.getTopCategoriesByAppCount(6).map { it.first }
        } else {
            categoriesToUse.split(",").filter { it.isNotBlank() }
        }
    }

    // Создаем карусели для выбранных категорий, данные берем динамически из БД
    val dynamicCategoryApps = remember(selectedCategoriesList, allApps) {
        selectedCategoriesList.mapNotNull { category ->
            val categoryApps = viewModel.getFilteredApps(category).take(6)
            if (categoryApps.isNotEmpty()) {
                category to categoryApps
            } else {
                null
            }
        }
    }

    var selectedTab by remember { mutableStateOf("home") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val currentDate = remember {
                        val dateFormat = SimpleDateFormat("d MMMM", Locale("ru"))
                        dateFormat.format(Date())
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Сегодня",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                },
                       colors = TopAppBarDefaults.topAppBarColors(
                           containerColor = Color.Transparent
                       ),
                       actions = {
                           // Показываем кнопку сортировки только если есть фильтр категории
                           if (categoryFilter != null) {
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
                           } else {
                               IconButton(onClick = { nav.navigate("categorySelection") }) {
                                   Icon(Icons.Default.Menu, contentDescription = "Выбрать категории")
                               }
                           }
                       }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            // Если есть фильтр категории, показываем только отфильтрованные приложения
            if (categoryFilter != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(),
                    contentPadding = PaddingValues(
                        bottom = 80.dp,
                        start = 16.dp,
                        end = 16.dp,
                        top = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "Категория: $categoryFilter",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    if (apps.isEmpty()) {
                        item {
                            Text(
                                text = "Нет приложений в этой категории",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    } else {
                        items(apps) { app ->
                            com.example.rustoreapplicationshowcases.ui.home.AppCard(
                                app = app,
                                onClick = {
                                    viewModel.onAppClicked(app)
                                    nav.navigate("details/${app.name}")
                                }
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)

                ) {
                    // Header Section
                    // Hero Image Section
                    item {
                        HeroImageSection(
                            onClick = {
                                // Можно добавить навигацию на детали приложения или специальную страницу
                                if (apps.isNotEmpty()) {
                                    val app = apps.first()
                                    viewModel.onAppClicked(app)
                                    nav.navigate("details/${app.name}")
                                }
                            }
                        )
                    }

                    // "Вам может быть интересно" Section
                    item {
                        SectionWithTitle(
                            title = "Вам может быть интересно",
                            apps = recommendedApps,
                            onAppClick = { app ->
                                viewModel.onAppClicked(app)
                                nav.navigate("details/${app.name}")
                            }
                        )
                    }

                    // "На основе ваших предпочтений" Section
                    item {
                        SectionWithTitle(
                            title = "На основе ваших предпочтений",
                            apps = preferenceApps,
                            onAppClick = { app ->
                                viewModel.onAppClicked(app)
                                nav.navigate("details/${app.name}")
                            }
                        )
                    }

                dynamicCategoryApps.forEach { (categoryName, categoryApps) ->
                    item {
                        SectionWithTitle(
                            title = categoryName,
                            apps = categoryApps,
                            onAppClick = { app ->
                                viewModel.onAppClicked(app)
                                nav.navigate("details/${app.name}")
                            }
                        )
                    }
                }
                }

                CustomBottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        when (tab) {
                            "home" -> nav.navigate("main") {
                                popUpTo("main") { inclusive = false }
                            }

                            "search" -> {
                                nav.navigate("search") {
                                    popUpTo("main") { inclusive = false }
                                }
                            }

                            "profile" -> {
                                nav.navigate("profile") {
                                    popUpTo("main") { inclusive = false }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clip(RoundedCornerShape(50.dp))
                )
            }
        }
    }
}

