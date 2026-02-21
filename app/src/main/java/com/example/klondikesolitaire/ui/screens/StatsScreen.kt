package com.example.klondikesolitaire.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.klondikesolitaire.viewmodel.AppViewModel

@Composable
fun StatsScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit
) {
    val stats by appViewModel.stats.collectAsState()

    val winRate = if (stats.gamesPlayed == 0) 0.0 else (stats.wins * 100.0 / stats.gamesPlayed)

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Statistics", style = MaterialTheme.typography.headlineSmall)

            StatCard("Games played", stats.gamesPlayed.toString())
            StatCard("Wins", stats.wins.toString())
            StatCard("Win rate", String.format("%.1f%%", winRate))
            StatCard("Current streak", stats.streak.toString())
            StatCard("Best time", formatBestTime(stats.bestWinTimeMs))

            Button(onClick = onBack) { Text("Back") }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

private fun formatBestTime(ms: Long): String {
    if (ms <= 0L) return "--"
    val totalSeconds = ms / 1000L
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
