package com.example.rustoreapplicationshowcases.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource
import com.example.rustoreapplicationshowcases.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import android.app.Application
import com.example.rustoreapplicationshowcases.AppViewModelFactory
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.lazy.itemsIndexed
import android.net.Uri
import androidx.compose.runtime.collectAsState
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nav: NavController,
    categoryFilter: String? = null,
    onToggleTheme: () -> Unit = {},
    isDarkTheme: Boolean = false
) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: HomeViewModel = viewModel(
        factory = AppViewModelFactory(context)
    )

    val apps = viewModel.apps
    val filtered = viewModel.getFilteredApps(categoryFilter)
    val searchQuery = viewModel.searchQuery

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryFilter ?: "Главная") },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            painter = painterResource(
                                id = if (isDarkTheme) R.drawable.ic_lighttheme
                                else R.drawable.ic_nighttheme
                            ),
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { nav.navigate("categories") }) {
                        Icon(Icons.Default.List, contentDescription = "Категории")
                    }
                }
            )
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding)) {

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Поиск приложений") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            val listState = rememberLazyListState()

            LazyColumn(state = listState) {

                itemsIndexed(filtered) { index, app ->

                    val itemOffset =
                        if (listState.firstVisibleItemIndex == index)
                            listState.firstVisibleItemScrollOffset
                        else 0

                    val fadeEnd = 350f
                    val alpha = ((fadeEnd - itemOffset) / fadeEnd)
                        .coerceIn(0f, 1f)

                    AppCard(
                        app = app,
                        modifier = Modifier.graphicsLayer {
                            this.alpha = alpha

                            val minScale = 0.85f
                            val scale = minScale + alpha * (1f - minScale)
                            scaleX = scale
                            scaleY = scale
                        },
                        onClick = {
                            viewModel.onAppClicked(app)
                            val encodedName = Uri.encode(app.name)
                            nav.navigate("details/$encodedName")
                        }
                    )
                }
            }
        }
    }
}