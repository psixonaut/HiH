package com.example.rustoreapplicationshowcases.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rustoreapplicationshowcases.AppViewModelFactory
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.lazy.itemsIndexed
import android.net.Uri
import androidx.compose.runtime.collectAsState
import com.example.rustoreapplicationshowcases.R
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.example.rustoreapplicationshowcases.data.PreferencesManager
import java.text.SimpleDateFormat
import java.util.*

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

    // Load saved categories if selectedCategories is null (first launch)
    val savedCategories = preferencesManager.getSelectedCategories()
    val categoriesToUse = selectedCategories ?: savedCategories

    val apps = viewModel.getFilteredApps(categoryFilter)
    val recommendedApps = apps.take(6)
    val preferenceApps = apps.drop(6).take(6)

    val dynamicCategories = remember(categoriesToUse) {
        (categoriesToUse?.split(",")?.filter { it.isNotBlank() } ?: emptyList()).sorted()
    }

    val dynamicCategoryApps = remember(dynamicCategories) {
        dynamicCategories.mapNotNull { category ->
            viewModel.getFilteredApps(category).take(6).takeIf { it.isNotEmpty() }
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
                    IconButton(onClick = { nav.navigate("categorySelection") }) {
                        Icon(Icons.Default.Menu, contentDescription = "Выбрать категории")
                    }
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            painter = painterResource(
                                id = if (isDarkTheme) R.drawable.ic_lighttheme
                                else R.drawable.ic_nighttheme
                            ),
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding(),)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 80.dp)

            ){
                // Header Section
                // Hero Image Section
                item {
                    HeroImageSection(
                        onClick = {
                            // Можно добавить навигацию на детали приложения или специальную страницу
                            if (apps.isNotEmpty()) {
                                viewModel.onAppClicked(apps.first())
                            }
                        }
                    )
                }

                // "Вам может быть интересно" Section
                item {
                    SectionWithTitle(
                        title = "Вам может быть интересно",
                        apps = recommendedApps,
                        onAppClick = { app -> viewModel.onAppClicked(app) }
                    )
                }

                // "На основе ваших предпочтений" Section
                item {
                    SectionWithTitle(
                        title = "На основе ваших предпочтений",
                        apps = preferenceApps,
                        onAppClick = { app -> viewModel.onAppClicked(app) }
                    )
                }

                dynamicCategoryApps.forEach { categoryApps ->
                    item {
                        SectionWithTitle(
                            title = categoryApps.first().category, // Assuming all apps in a dynamicCategoryApps list have the same category
                            apps = categoryApps,
                            onAppClick = { app -> viewModel.onAppClicked(app) }
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
                            // Можно добавить навигацию на экран профиля
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
        // Hero image placeholder - using Material Design colors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() }
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            // Overlay text
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Выбор редакции. топ недели",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
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

@Composable
fun CustomBottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Цвета для навигационной панели
    val lightLavender = Color(0xFFE8D5FF) // Светло-лавандовый фон
    val darkPurple = Color(0xFF6200EE) // Тёмно-фиолетовый для иконок и активного фона

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 25.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(lightLavender)
            .padding(horizontal = 5.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home Icon
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (selectedTab == "home") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(darkPurple)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Главная",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = { onTabSelected("home") },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Главная",
                        tint = darkPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Search Icon
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (selectedTab == "search") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(darkPurple)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = { onTabSelected("search") },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск",
                        tint = darkPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Profile Icon
        Box(
            modifier = Modifier
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (selectedTab == "profile") {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(darkPurple)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Профиль",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                IconButton(
                    onClick = { onTabSelected("profile") },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Профиль",
                        tint = darkPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
