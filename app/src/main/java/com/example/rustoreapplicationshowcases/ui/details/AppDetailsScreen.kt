package com.example.rustoreapplicationshowcases.ui.details

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rustoreapplicationshowcases.AppViewModelFactory
import com.example.rustoreapplicationshowcases.R
import com.example.rustoreapplicationshowcases.data.model.AppInfo

// Модель для одного скриншота
data class ScreenshotItem(
    val resId: Int,
    val title: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailsScreen(
    nav: NavController,
    appName: String,
    onToggleTheme: () -> Unit,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: DetailsViewModel = viewModel(
        factory = AppViewModelFactory(app = context)
    )

    val decodedName = Uri.decode(appName)
    val app = viewModel.getAppByName(decodedName)
    val extra = viewModel.getExtraInfo(decodedName)
    val reviews = viewModel.getReviews(decodedName)

    // реальные скриншоты из drawable
    val screenshots = remember {
        listOf(
            ScreenshotItem(R.drawable.sber_screen_1, "Главный экран"),
            ScreenshotItem(R.drawable.sber_screen_2, "Платежи и переводы"),
            ScreenshotItem(R.drawable.sber_screen_3, "История операций")
        )
    }

    var openedScreenshotIndex by remember { mutableStateOf<Int?>(null) }

    if (app == null) {
        NotFoundAppScreen(nav = nav)
        return
    }

    val iconResId = context.resources.getIdentifier(app.icon, "drawable", context.packageName)
    val recommendedApps = viewModel.getSimilarApps(app)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = app.name) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            painter = painterResource(
                                id = if (isDarkTheme) R.drawable.ic_lighttheme
                                else R.drawable.ic_nighttheme
                            ),
                            contentDescription = "Сменить тему"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // ---------- Верхний блок: иконка + название + кнопка скачать ----------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconResId != 0) {
                    Image(
                        painter = painterResource(id = iconResId),
                        contentDescription = app.name,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = extra?.developer ?: "Издатель не указан",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { /* TODO: логика скачивания/перехода в магазин */ },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = "Скачать")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- Рейтинг ----------
            Text(
                text = buildString {
                    append("Рейтинг: ${String.format("%.1f", app.rating)}")
                    if (reviews.isNotEmpty()) {
                        append(" (${reviews.size} отзывов)")
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "[Интерактивно: можно улучшить, добавив оценку по нажатию]",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ---------- Описание с возможностью разворота ----------
            Text(
                text = "Описание",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            var descriptionExpanded by remember { mutableStateOf(false) }
            val descriptionText = extra?.description ?: "Описание пока не добавлено."

            Text(
                text = if (descriptionExpanded) {
                    descriptionText
                } else {
                    descriptionText.take(120) + if (descriptionText.length > 120) "…" else ""
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable {
                    if (descriptionText.length > 120) {
                        descriptionExpanded = !descriptionExpanded
                    }
                }
            )

            if (descriptionText.length > 120) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (descriptionExpanded) "Свернуть" else "Развернуть",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        descriptionExpanded = !descriptionExpanded
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Скриншоты – карусель "полторы страницы" ----------
            Text(
                text = "Скриншоты",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            ScreenshotCarousel(
                screenshots = screenshots,
                onOpenFull = { index -> openedScreenshotIndex = index }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Отзывы клиентов ----------
            if (reviews.isNotEmpty()) {
                Text(
                    text = "Отзывы клиентов",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                reviews.forEach { review ->
                    ReviewCard(review = review)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // ---------- Кнопка "Оставьте свой комментарий" (более контрастная) ----------
            Button(
                modifier = Modifier
                    .fillMaxWidth(),
                onClick = { /* TODO: открыть форму добавления комментария */ },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Оставьте свой комментарий",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Блок 1: предложения от разработчика ----------
            Text(
                text = extra?.developer?.let { "Ещё от $it" } ?: "Другие приложения разработчика",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            MoreFromDeveloperBlock(
                app = app,
                developerName = extra?.developer,
                nav = nav,
                apps = recommendedApps
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Блок 2: похожие приложения (то, что уже было) ----------
            Text(
                text = "Похожие приложения",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (recommendedApps.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendedApps) { recommended ->
                        RecommendedAppRow(nav = nav, app = recommended)
                    }
                }
            } else {
                // Заглушки, если рекомендаций нет
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(5) { index ->
                        RecommendedPlaceholderCard(slotNumber = index + 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // ---------- Полноэкранный просмотр скриншота ----------
    openedScreenshotIndex?.let { index ->
        val item = screenshots[index]

        AlertDialog(
            onDismissRequest = { openedScreenshotIndex = null },
            confirmButton = {
                TextButton(onClick = { openedScreenshotIndex = null }) {
                    Text("Закрыть")
                }
            },
            title = { Text(item.title) },
            text = {
                Image(
                    painter = painterResource(id = item.resId),
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotFoundAppScreen(nav: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Приложение не найдено") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Не удалось найти данные о приложении")
        }
    }
}

// ---------- Скриншоты: горизонтальная карусель ----------

@Composable
private fun ScreenshotCarousel(
    screenshots: List<ScreenshotItem>,
    onOpenFull: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(screenshots) { index, item ->
            Card(
                modifier = Modifier
                    // каждый элемент ~75% ширины – видно полторы "страницы"
                    .fillParentMaxWidth(0.75f)
                    .aspectRatio(9f / 16f)
                    .clickable { onOpenFull(index) },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Image(
                    painter = painterResource(id = item.resId),
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ---------- Остальные вспомогательные блоки ----------

@Composable
private fun RatingBadge(rating: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = String.format("%.1f", rating),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ReviewCard(review: AppReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = review.userName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.width(8.dp))
                RatingStars(rating = review.rating)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = review.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = review.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RatingStars(rating: Float, maxStars: Int = 5) {
    Row {
        val fullStars = rating.toInt()
        val hasHalf = (rating - fullStars) >= 0.5f

        for (i in 1..maxStars) {
            val icon = when {
                i <= fullStars -> Icons.Filled.Star
                hasHalf && i == fullStars + 1 -> Icons.Filled.Star // можно заменить на ползвезды при наличии ресурса
                else -> Icons.Outlined.Star
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// Карточка "похожие приложения" для LazyRow
@Composable
private fun RecommendedAppRow(
    nav: NavController,
    app: AppInfo
) {
    val context = LocalContext.current
    val iconResId = context.resources.getIdentifier(app.icon, "drawable", context.packageName)

    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable {
                val encodedName = Uri.encode(app.name)
                nav.navigate("details/$encodedName")
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (iconResId != 0) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = app.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            Text(
                text = "Категория: ${app.category}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = String.format("%.1f", app.rating),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

// Блок "Ещё от разработчика" – как на первом скрине
@Composable
private fun MoreFromDeveloperBlock(
    app: AppInfo,
    developerName: String?,
    nav: NavController,
    apps: List<AppInfo>
) {
    val context = LocalContext.current
    val iconResId = context.resources.getIdentifier(app.icon, "drawable", context.packageName)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (iconResId != 0) {
                    Image(
                        painter = painterResource(id = iconResId),
                        contentDescription = app.name,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = developerName?.let { "More by $it" } ?: "More from this developer",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        // простая логика: открыть первое приложение из списка, если оно есть
                        apps.firstOrNull()?.let { target ->
                            val encodedName = Uri.encode(target.name)
                            nav.navigate("details/$encodedName")
                        }
                    },
                    shape = CircleShape
                ) {
                    Text(text = "More")
                }
            }
        }
    }
}

// Заглушка-карточка, когда рекомендаций нет
@Composable
private fun RecommendedPlaceholderCard(
    slotNumber: Int
) {
    Card(
        modifier = Modifier
            .width(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Рекомендуемое приложение $slotNumber",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Заглушка рекомендации",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
