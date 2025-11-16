package com.example.rustoreapplicationshowcases.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rustoreapplicationshowcases.AppViewModelFactory
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    navController: NavController
) {
    val context = LocalContext.current.applicationContext as android.app.Application
    val viewModel: AdminViewModel = viewModel(
        factory = AppViewModelFactory(context)
    )
    
    // Очищаем сообщения при первом запуске
    LaunchedEffect(Unit) {
        viewModel.clearMessages()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Режим администратора") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Статус режима администратора
            AdminModeCard(
                isEnabled = viewModel.isAdminModeEnabled,
                onToggle = { viewModel.toggleAdminMode() }
            )
            
            // Информация о текущих данных
            DataInfoCard(
                appsCount = viewModel.currentAppsCount,
                lastSyncTime = viewModel.lastSyncTime
            )
            
            // Настройка API URL
            ApiUrlCard(
                apiUrl = viewModel.apiUrl,
                onUrlChange = { viewModel.updateApiUrl(it) },
                onSave = { viewModel.saveApiUrl() }
            )
            
            // Кнопки управления
            ManagementButtonsCard(
                isLoading = viewModel.isLoading,
                onLoadFromApi = { viewModel.loadAppsFromApi() },
                onLoadFromDefaultApi = { viewModel.loadAppsFromDefaultApi() },
                onResetToLocal = { viewModel.resetToLocalData() }
            )
            
            // Сообщения об ошибках и успехе
            viewModel.errorMessage?.let { error ->
                ErrorCard(
                    message = error,
                    onDismiss = { viewModel.clearMessages() }
                )
            }
            
            viewModel.successMessage?.let { success ->
                SuccessCard(
                    message = success,
                    onDismiss = { viewModel.clearMessages() }
                )
            }
            
            // Форма добавления нового приложения
            AddAppFormCard(viewModel = viewModel)
            
            // Информация о рекомендациях
            RecommendationsInfoCard(viewModel = viewModel)
        }
    }
}

@Composable
fun AdminModeCard(
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Режим администратора",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isEnabled) "Включен" else "Выключен",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

@Composable
fun DataInfoCard(
    appsCount: Int,
    lastSyncTime: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Информация о данных",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Приложений в базе:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$appsCount",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Последняя синхронизация:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = lastSyncTime,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ApiUrlCard(
    apiUrl: String,
    onUrlChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "URL API для загрузки данных",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = apiUrl,
                onValueChange = onUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("URL API") },
                placeholder = { Text("https://example.com/api/") },
                singleLine = true
            )
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Сохранить URL")
            }
            Text(
                text = "Пример: https://raw.githubusercontent.com/user/repo/main/apps.json",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ManagementButtonsCard(
    isLoading: Boolean,
    onLoadFromApi: () -> Unit,
    onLoadFromDefaultApi: () -> Unit,
    onResetToLocal: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Управление данными",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = onLoadFromApi,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Загрузить из указанного API")
            }
            
            OutlinedButton(
                onClick = onLoadFromDefaultApi,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Загрузить из API по умолчанию")
            }
            
            OutlinedButton(
                onClick = onResetToLocal,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Сбросить к локальным данным")
            }
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
fun SuccessCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun RecommendationsInfoCard(viewModel: AdminViewModel) {
    val editorChoiceApps = viewModel.getEditorChoiceApps()
    val recommendedApps = viewModel.getRecommendedApps()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Система рекомендаций",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Выбор редакции (топ по рейтингу):",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            editorChoiceApps.forEach { app ->
                Text(
                    text = "• ${app.name} (${app.rating})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Рекомендуемые (топ по загрузкам):",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            recommendedApps.forEach { app ->
                Text(
                    text = "• ${app.name} (${app.downloads} загрузок)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppFormCard(viewModel: AdminViewModel) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf("") }
    var downloads by remember { mutableStateOf("") }
    var reviews by remember { mutableStateOf("") }
    var ageRating by remember { mutableStateOf("0+") }
    var publisher by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }
    
    val categories = viewModel.getAvailableCategories()
    
    // Очищаем форму при успешном добавлении
    LaunchedEffect(viewModel.successMessage) {
        if (viewModel.successMessage != null && viewModel.successMessage!!.contains("успешно добавлено")) {
            name = ""
            category = ""
            rating = ""
            downloads = ""
            reviews = ""
            ageRating = "0+"
            publisher = ""
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Добавить новое приложение",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название приложения *") },
                singleLine = true
            )
            
            // Выбор категории
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("Категория *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) }
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                expandedCategory = false
                            }
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = rating,
                    onValueChange = { rating = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Рейтинг *") },
                    placeholder = { Text("4.5") },
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = downloads,
                    onValueChange = { downloads = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Загрузки") },
                    placeholder = { Text("1000") },
                    singleLine = true
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = reviews,
                    onValueChange = { reviews = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Отзывы") },
                    placeholder = { Text("100") },
                    singleLine = true
                )
                
                var expandedAgeRating by remember { mutableStateOf(false) }
                val ageRatings = listOf("0+", "10+", "13+", "17+", "18+")
                
                ExposedDropdownMenuBox(
                    expanded = expandedAgeRating,
                    onExpandedChange = { expandedAgeRating = !expandedAgeRating }
                ) {
                    OutlinedTextField(
                        value = ageRating,
                        onValueChange = { ageRating = it },
                        readOnly = true,
                        modifier = Modifier
                            .weight(1f)
                            .menuAnchor(),
                        label = { Text("Возраст") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAgeRating) }
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAgeRating,
                        onDismissRequest = { expandedAgeRating = false }
                    ) {
                        ageRatings.forEach { age ->
                            DropdownMenuItem(
                                text = { Text(age) },
                                onClick = {
                                    ageRating = age
                                    expandedAgeRating = false
                                }
                            )
                        }
                    }
                }
            }
            
            OutlinedTextField(
                value = publisher,
                onValueChange = { publisher = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Издатель") },
                singleLine = true
            )
            
            Button(
                onClick = {
                    viewModel.addApp(
                        name = name,
                        category = category,
                        rating = rating,
                        downloads = downloads,
                        reviews = reviews,
                        ageRating = ageRating,
                        publisher = publisher
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && category.isNotBlank() && rating.isNotBlank() && !viewModel.isLoading
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Добавить приложение")
            }
        }
    }
}

