package com.example.rustoreapplicationshowcases.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    var page by remember { mutableStateOf(0) }

    val pages = listOf(
        "Открывайте мир приложений",
        "Скачивайте быстро и безопасно",
        "Лучшие игры и сервисы — в одном месте"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(80.dp))

        AnimatedContent(
            targetState = page,
            transitionSpec = { fadeIn() with fadeOut() },
            modifier = Modifier.weight(1f)
        ) { targetPage ->
            Text(
                text = pages[targetPage],
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row {
            pages.indices.forEach { index ->
                val isActive = index == page
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .size(if (isActive) 12.dp else 8.dp)
                        .background(
                            color = if (isActive)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            shape = MaterialTheme.shapes.small
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (page < pages.lastIndex) {
                    page++
                } else {
                    onFinish()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (page == pages.lastIndex) "Начать" else "Далее")
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
