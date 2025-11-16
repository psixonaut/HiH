package com.example.rustoreapplicationshowcases.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rustoreapplicationshowcases.ui.onboarding.OnboardingScreen
import com.example.rustoreapplicationshowcases.ui.home.HomeScreen
import com.example.rustoreapplicationshowcases.ui.categories.CategoriesScreen
import com.example.rustoreapplicationshowcases.ui.details.AppDetailsScreen
import com.example.rustoreapplicationshowcases.ui.home.CategorySelectionScreen
import com.example.rustoreapplicationshowcases.ui.home.CategorySelectionViewModel
import com.example.rustoreapplicationshowcases.ui.search.SearchScreen
import com.example.rustoreapplicationshowcases.ui.search.AllCategoriesScreen
import com.example.rustoreapplicationshowcases.ui.search.CategoryAppsScreen
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun AppNavHost(
    navController: NavHostController,
    showOnboarding: Boolean,
    onFinishOnboarding: () -> Unit,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean
) {

    NavHost(
        navController = navController,
        startDestination = if (showOnboarding) "onboarding" else "main"
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onFinish = {
                    onFinishOnboarding()
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                onToggleTheme = onToggleTheme,
                isDarkTheme = isDarkTheme
            )
        }

        composable("categories") {
            CategoriesScreen(
                onCategoryClick = { selected ->
                    if (selected == null) {
                        navController.navigate("main")
                    } else {
                        navController.navigate("category/$selected")
                    }
                },
                onToggleTheme = onToggleTheme,
                isDarkTheme = isDarkTheme
            )
        }

        composable("main") {
            val selectedCategories = navController.currentBackStackEntry
                ?.savedStateHandle
                ?.getLiveData<String>("selectedCategories")?.observeAsState()

            HomeScreen(
                nav = navController,
                onToggleTheme = onToggleTheme,
                isDarkTheme = isDarkTheme,
                selectedCategories = selectedCategories?.value
            )
        }

        composable("categorySelection") {
            CategorySelectionScreen(
                navController = navController,
                onToggleTheme = onToggleTheme,
                isDarkTheme = isDarkTheme
            )
        }

        composable("category/{categoryName}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("categoryName")
            HomeScreen(
                nav = navController,
                categoryFilter = category,
                onToggleTheme = onToggleTheme,
                isDarkTheme = isDarkTheme
            )
        }
        composable("details/{appName}") { backStackEntry ->
            val appName = backStackEntry.arguments?.getString("appName") ?: ""

            AppDetailsScreen(
                nav = navController,
                appName = appName,
                onToggleTheme = onToggleTheme,
                isDarkTheme = isDarkTheme
            )
        }

        composable("search") {
            SearchScreen(
                navController = navController,
                onToggleTheme = onToggleTheme,
                isDarkTheme = isDarkTheme
            )
        }

        composable("allCategories") {
            AllCategoriesScreen(
                navController = navController,
                onToggleTheme = onToggleTheme,
                isDarkTheme = isDarkTheme
            )
        }

        composable("categoryApps/{categoryName}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("categoryName") ?: ""
            CategoryAppsScreen(
                navController = navController,
                categoryName = category,
                onToggleTheme = onToggleTheme,
                isDarkTheme = isDarkTheme
            )
        }
    }
}
