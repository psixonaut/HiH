package com.example.rustoreapplicationshowcases.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import com.example.rustoreapplicationshowcases.R
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import com.example.rustoreapplicationshowcases.AppViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onCategoryClick: (String?) -> Unit,
    onToggleTheme: () -> Unit = {},
    isDarkTheme: Boolean = false
) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: CategoriesViewModel = viewModel(
        factory = AppViewModelFactory(context)
    )

    val categories = viewModel.categories

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Категории") },
                actions = {
                    // Кнопка смены темы убрана
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {

            items(categories.size) { idx ->
                CategoryItem(
                    title = categories[idx],
                    onClick = { onCategoryClick(categories[idx]) }
                )
            }

            item {
                TextButton(
                    onClick = { onCategoryClick(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сброс категории")
                }
            }
        }
    }
}