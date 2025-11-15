package com.example.rustoreapplicationshowcases.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rustoreapplicationshowcases.ui.onboarding.OnboardingScreen
import com.example.rustoreapplicationshowcases.ui.home.HomeScreen
import com.example.rustoreapplicationshowcases.ui.categories.CategoriesScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    showOnboarding: Boolean,
    onFinishOnboarding: () -> Unit
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
                }
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
                }
            )
        }

        composable("main") {
            HomeScreen(nav = navController)
        }

        composable("category/{categoryName}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("categoryName")
            HomeScreen(
                nav = navController,
                categoryFilter = category
            )
        }
    }
}
