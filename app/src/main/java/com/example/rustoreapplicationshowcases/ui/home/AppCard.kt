package com.example.rustoreapplicationshowcases.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rustoreapplicationshowcases.data.model.AppInfo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon

@SuppressLint("LocalContextResourcesRead")
@Composable
fun AppCard(
    app: AppInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        val context = LocalContext.current

        val iconResId = context.resources.getIdentifier(app.icon, "drawable", context.packageName)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(4.dp) // Тестовая тень, может удалим
        ) {
            Row(modifier = Modifier.padding(16.dp)) {

                val iconResId =
                    context.resources.getIdentifier(app.icon, "drawable", context.packageName)

                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = app.name,
                    modifier = Modifier.size(56.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(app.name, style = MaterialTheme.typography.titleMedium)
                    Text("Оценка: ${app.rating}", style = MaterialTheme.typography.bodyMedium)
                    Text(app.category, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@SuppressLint("LocalContextResourcesRead")
@Composable
fun AppCardHorizontal(
    app: AppInfo,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val iconResId = context.resources.getIdentifier(app.icon, "drawable", context.packageName)

    Card(
        modifier = modifier
            .width(120.dp)
            .height(140.dp)
            .padding(horizontal = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (iconResId != 0) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = app.name,
                    modifier = Modifier.size(48.dp)
                )
            } else {
                // Placeholder shapes if icon not found
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Рейтинг",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = app.rating.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
