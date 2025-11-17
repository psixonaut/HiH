package com.example.rustoreapplicationshowcases.ui.common

import android.os.Build
import android.graphics.RenderEffect
import android.graphics.Shader
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asComposeRenderEffect

@Composable
fun CustomBottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 64.dp, vertical = 25.dp)
            .height(80.dp)
            .clickable(enabled = false, onClick = {}) // блокировка сквозных кликов
    ) {

        // ---- iOS Стеклянный фон (фейковый блюр) ----
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(45.dp))
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.graphicsLayer {
                            renderEffect = RenderEffect.createBlurEffect(
                                36f, 36f, Shader.TileMode.CLAMP
                            ).asComposeRenderEffect()
                        }
                    } else Modifier
                )
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.33f),
                                Color(0xFFACE3FF).copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.14f))
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("home", Icons.Default.Home, selectedTab, onTabSelected)
            NavItem("search", Icons.Default.Search, selectedTab, onTabSelected)
            NavItem("profile", Icons.Default.Person, selectedTab, onTabSelected)
        }
    }
}

@Composable
fun NavItem(
    id: String,
    icon: ImageVector,
    selected: String,
    onSelect: (String) -> Unit
) {
    val isSelected = id == selected

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.25f else 1f,
        label = "scale"
    )

    Box(
        modifier = Modifier.size(55.dp),
        contentAlignment = Alignment.Center
    ) {
        // Подсветка под активной кнопкой
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(55.dp)
                    .clip(CircleShape)
                    .blur(20.dp)
                    .background(Color.White.copy(0.28f))
            )
        }

        Icon(
            imageVector = icon,
            contentDescription = id,
            tint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                Color.Black.copy(alpha = 0.65f),
            modifier = Modifier
                .size(28.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clickable { onSelect(id) }
        )
    }
}
