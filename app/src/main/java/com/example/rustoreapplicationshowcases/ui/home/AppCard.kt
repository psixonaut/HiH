package com.example.rustoreapplicationshowcases.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.rustoreapplicationshowcases.data.model.AppInfo

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
