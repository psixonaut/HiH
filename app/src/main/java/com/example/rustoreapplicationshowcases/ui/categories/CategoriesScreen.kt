package com.example.rustoreapplicationshowcases.ui.categories

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onCategoryClick: (String?) -> Unit,
    viewModel: CategoriesViewModel = viewModel()
) {
    val categories = viewModel.categories

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Категории") }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier.padding(padding)
        ) {

            item {
                TextButton(
                    onClick = { onCategoryClick(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Сброс категории")
                }
            }

            items(categories.size) { idx ->
                CategoryItem(
                    title = categories[idx],
                    onClick = { onCategoryClick(categories[idx]) }
                )
            }
        }
    }
}