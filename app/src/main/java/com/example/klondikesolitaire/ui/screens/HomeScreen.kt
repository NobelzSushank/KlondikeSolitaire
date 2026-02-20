package com.example.klondikesolitaire.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.klondikesolitaire.viewmodel.AppViewModel

@Composable
fun HomeScreen(
    appViewModel: AppViewModel,
    onPlay: () -> Unit,
    onContinue: () -> Unit,
    onThemes: () -> Unit,
    onSettings: () -> Unit,
    onStats: () -> Unit
) {
    val settings by appViewModel.settings.collectAsState()
    val activity = LocalContext.current as? android.app.Activity

    // Back from Home exits app.
    BackHandler {
        activity?.finish()
    }

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Klondike Solitaire",
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                text = "Relax and play a classic.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Primary Play button (large)
            Button(
                onClick = {
                    // For now, create a fake saved game so Continue can appear later.
                    appViewModel.createFakeSavedGame()
                    onPlay()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("Play", style = MaterialTheme.typography.titleLarge)
            }

            // Continue appears only if saved game exists.
            if (settings.hasSavedGame) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Continue", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(6.dp))

            // Premium-feeling cards
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                HomeCard(title = "Themes", subtitle = "Colors & style", onClick = onThemes, modifier = Modifier.weight(1f))
                HomeCard(title = "Settings", subtitle = "Controls & more", onClick = onSettings, modifier = Modifier.weight(1f))
            }
            HomeCard(title = "Stats", subtitle = "Wins & streaks (soon)", onClick = onStats, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.weight(1f))

            // Footer row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Privacy / Ads settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        // Stub: open a future screen or consent form
                    }
                )
                Text(
                    text = "v1.0",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HomeCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(88.dp)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}