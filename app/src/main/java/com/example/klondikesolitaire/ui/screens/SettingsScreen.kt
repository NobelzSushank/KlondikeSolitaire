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
fun SettingsScreen(onBack: () -> Unit) {
    Surface {
        Column(Modifier.padding(16.dp)) {
            Text("Settings (Coming Soon)", style = MaterialTheme.typography.headlineSmall)
            Text("Future: sound, draw mode, animations, accessibility options, etc.")
            Button(onClick = onBack, modifier = Modifier.padding(top = 16.dp)) { Text("Back") }
        }
    }
}