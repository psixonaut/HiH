package com.example.rustoreapplicationshowcases.ui.home

import com.example.rustoreapplicationshowcases.ui.common.CustomBottomNavigationBar
import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.ViewGroup
import android.view.ViewOutlineProvider
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rustoreapplicationshowcases.*
import com.example.rustoreapplicationshowcases.data.PreferencesManager
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import com.example.rustoreapplicationshowcases.data.model.SortType

import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur
import java.text.SimpleDateFormat
import java.util.*
import com.example.rustoreapplicationshowcases.R

/* --------------------------------------------------------------
   HERO IMAGE
---------------------------------------------------------------- */
@Composable
fun HeroImageSection(onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 16.dp)
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .clickable { onClick() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            Image(
                painter = painterResource(id = R.drawable.ic_rustore_logo),
                contentDescription = "Выбор редакции",
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.2f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
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

/* --------------------------------------------------------------
   SECTION WITH TITLE
---------------------------------------------------------------- */
@Composable
fun SectionWithTitle(
    title: String,
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
        ) {
            items(apps) { app ->
                AppCardHorizontal(app = app, onClick = { onAppClick(app) })
            }
        }
    }
}

/* --------------------------------------------------------------
   ВСЁ СОДЕРЖИМОЕ ЭКРАНА (ДЛЯ ГЛАВНОЙ)
---------------------------------------------------------------- */
@Composable
fun MainHomeContent(
    apps: List<AppInfo>,
    dynamicCategoryApps: List<Pair<String, List<AppInfo>>>,
    recommendedApps: List<AppInfo>,
    preferenceApps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    viewModel: HomeViewModel,
    nav: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(bottom = 160.dp)
    ) {

        item {
            HeroImageSection(
                onClick = {
                    if (apps.isNotEmpty()) {
                        val app = apps.first()
                        viewModel.onAppClicked(app)
                        nav.navigate("details/${app.name}")
                    }
                }
            )
        }

        item {
            SectionWithTitle(
                title = "Вам может быть интересно",
                apps = recommendedApps,
                onAppClick = onAppClick
            )
        }

        item {
            SectionWithTitle(
                title = "На основе ваших предпочтений",
                apps = preferenceApps,
                onAppClick = onAppClick
            )
        }

        dynamicCategoryApps.forEach { (categoryName, categoryApps) ->
            item {
                SectionWithTitle(
                    title = categoryName,
                    apps = categoryApps,
                    onAppClick = onAppClick
                )
            }
        }
    }
}

/* --------------------------------------------------------------
   СПИСОК ТОЛЬКО ДЛЯ КАТЕГОРИЙ
---------------------------------------------------------------- */
@Composable
fun CategoriesListContent(
    apps: List<AppInfo>,
    categoryFilter: String,
    viewModel: HomeViewModel,
    nav: NavController
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "Категория: $categoryFilter",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
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
                AppCard(
                    app = app,
                    onClick = {
                        viewModel.onAppClicked(app)
                        nav.navigate("details/${app.name}")
                    }
                )
            }
        }
    }
}

/* --------------------------------------------------------------
   HOME SCREEN С РАБОЧИМ BLURVIEW
---------------------------------------------------------------- */
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
    val viewModel: HomeViewModel = viewModel(factory = AppViewModelFactory(context))
    val preferencesManager = PreferencesManager(context)

    val allApps = viewModel.apps.collectAsState().value

    var sortType by remember { mutableStateOf<SortType?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    val apps = remember(allApps, categoryFilter, sortType) {
        val filtered = viewModel.getFilteredApps(categoryFilter)
        if (categoryFilter != null && sortType != null)
            viewModel.sortApps(filtered, sortType!!)
        else filtered
    }

    val recommendedApps = apps.take(6)
    val preferenceApps = apps.drop(6).take(6)

    val savedCategories = preferencesManager.getSelectedCategories()
    val categoriesToUse = selectedCategories ?: savedCategories

    val selectedCategoriesList = remember(categoriesToUse, allApps) {
        if (categoriesToUse.isNullOrBlank())
            viewModel.getTopCategoriesByAppCount(6).map { it.first }
        else
            categoriesToUse.split(",").filter { it.isNotBlank() }
    }

    val dynamicCategoryApps = remember(selectedCategoriesList, allApps) {
        selectedCategoriesList.mapNotNull { category ->
            val catApps = viewModel.getFilteredApps(category).take(6)
            if (catApps.isNotEmpty()) category to catApps else null
        }
    }

    var selectedTab by remember { mutableStateOf("home") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val currentDate = remember {
                        SimpleDateFormat("d MMMM", Locale("ru")).format(Date())
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Сегодня",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentDate,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
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
                                    onClick = { sortType = SortType.ALPHABETICAL; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("По оценке") },
                                    onClick = { sortType = SortType.RATING; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("По скачиваниям") },
                                    onClick = { sortType = SortType.DOWNLOADS; showSortMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Сбросить") },
                                    onClick = { sortType = null; showSortMenu = false }
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

            // ---------- CONTENT ----------
            if (categoryFilter == null) {
                MainHomeContent(
                    apps = apps,
                    dynamicCategoryApps = dynamicCategoryApps,
                    recommendedApps = recommendedApps,
                    preferenceApps = preferenceApps,
                    onAppClick = { app ->
                        viewModel.onAppClicked(app)
                        nav.navigate("details/${app.name}")
                    },
                    viewModel = viewModel,
                    nav = nav
                )
            } else {
                CategoriesListContent(
                    apps = apps,
                    categoryFilter = categoryFilter,
                    viewModel = viewModel,
                    nav = nav
                )
            }

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

            // ---------- NAV BAR ----------
            CustomBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    selectedTab = tab
                    when (tab) {
                        "home" -> nav.navigate("main") {
                            popUpTo("main") { inclusive = false }
                        }
                        "search" -> nav.navigate("search") {
                            popUpTo("main") { inclusive = false }
                        }
                        "profile" -> nav.navigate("profile") {
                            popUpTo("main") { inclusive = false }
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 0.dp)
                    .clip(RoundedCornerShape(40.dp))
            )
        }
    }
}
