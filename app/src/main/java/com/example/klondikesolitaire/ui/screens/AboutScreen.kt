package com.example.klondikesolitaire.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("About") })
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
        ) {
            Text("Klondike Solitaire (Compose + MVVM + StateFlow)")
            Text("Ads: Banner + Interstitial + Rewarded (test IDs).")
            Text("No image assets required—cards are rendered as text.")

            Button(
                onClick = onBack,
                modifier = Modifier.padding(top = 16.dp)
            ) { Text("Back") }
        }
    }
}