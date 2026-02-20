package com.example.klondikesolitaire.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ThemesScreen(onBack: () -> Unit) {
    Surface {
        Column(Modifier.padding(16.dp)) {
            Text("Themes (Coming Soon)", style = MaterialTheme.typography.headlineSmall)
            Text("This screen will let users pick light/dark/system and other themes.")
            Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Back") }
        }
    }
}