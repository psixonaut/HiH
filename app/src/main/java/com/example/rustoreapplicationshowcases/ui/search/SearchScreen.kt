package com.example.rustoreapplicationshowcases.ui.search

import android.app.Activity
import android.app.Application
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.ViewGroup
import android.view.ViewOutlineProvider
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rustoreapplicationshowcases.R
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rustoreapplicationshowcases.AppViewModelFactory
import com.example.rustoreapplicationshowcases.ui.home.HomeViewModel
import com.example.rustoreapplicationshowcases.ui.common.CustomBottomNavigationBar
import com.example.rustoreapplicationshowcases.data.model.SortType
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur

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
    
    // Получаем актуальные данные из StateFlow
    val appsState = viewModel.apps.collectAsState()
    val apps = appsState.value
    
    var searchQuery by remember { mutableStateOf("") }
    
    // Вычисляем категории на основе актуальных данных
    val topCategories = remember(apps) {
        viewModel.getTopCategoriesByAppCount(8)
    }
    
           // Состояние сортировки
           var sortType by remember { mutableStateOf<SortType?>(null) }
           var showSortMenu by remember { mutableStateOf(false) }
           
           // Вычисляем результаты поиска на основе актуальных данных и применяем сортировку
           val searchResults = remember(searchQuery, apps, sortType) {
               if (searchQuery.isBlank()) {
                   emptyList()
               } else {
                   val results = viewModel.searchApps(searchQuery)
                   if (sortType != null) {
                       viewModel.sortApps(results, sortType!!)
                   } else {
                       results
                   }
               }
           }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    var selectedTab by remember { mutableStateOf("search") }
    
    // Убрали автоматический фокус при открытии экрана
    // Фокус будет устанавливаться только при повторном нажатии на поиск
    
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
                           // Показываем кнопку сортировки только если есть результаты поиска
                           if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
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
                       }
                   )
               },
        bottomBar = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                // ---------- BLUR BACKGROUND ----------
                val barHeight = 80.dp
                val corner = barHeight / 2
                val cornerPx = with(LocalDensity.current) { corner.toPx() }

                AndroidView(
                    factory = { context ->
                        val activity = context as Activity
                        val rootView =
                            activity.window.decorView.findViewById<ViewGroup>(android.R.id.content)

                        val blur = BlurView(context)

                        // Capsule форма (полукруги)
                        val drawable = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = cornerPx       // ← важно: высота/2
                            setColor(android.graphics.Color.TRANSPARENT)
                        }

                        blur.apply {
                            setupWith(rootView)
                                .setBlurAlgorithm(RenderScriptBlur(context))
                                .setBlurRadius(18f) // лучше 22
                                .setBlurAutoUpdate(true)

                            setOverlayColor(Color.White.copy(alpha = 0.14f).toArgb())

                            background = drawable

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                clipToOutline = true
                                outlineProvider = ViewOutlineProvider.BACKGROUND
                            }
                        }

                        blur
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 10.dp, vertical = 24.dp) // horizontal лучше 70
                        .fillMaxWidth()
                        .height(barHeight)
                        .clip(RoundedCornerShape(corner))   // ← Compose-обрезка тоже capsule
                )

                CustomBottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        when (tab) {
                            "home" -> {
                                selectedTab = tab
                                navController.navigate("main") {
                                    popUpTo("main") { inclusive = false }
                                }
                            }
                            "search" -> {
                                // Повторное нажатие на поиск - форсим курсор в поле ввода
                                if (selectedTab == "search") {
                                    focusRequester.requestFocus()
                                    keyboardController?.show()
                                    // Если есть запрос, очищаем его
                                    if (searchQuery.isNotEmpty()) {
                                        searchQuery = ""
                                    }
                                } else {
                                    selectedTab = tab
                                }
                            }
                            "profile" -> {
                                selectedTab = tab
                                navController.navigate("profile") {
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
    ) { padding ->
        if (searchQuery.isBlank()) {
            // Show categories when search is empty (история поиска убрана)
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
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
                contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp, top = 16.dp),
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
                                navController.navigate("details/${app.name}")
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

