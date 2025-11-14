package com.example.rustoreapplicationshowcases.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.rustoreapplicationshowcases.ui.home.model.AppInfo

@Composable
fun AppCard(app: AppInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {

            Image(
                painter = painterResource(app.icon),
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
