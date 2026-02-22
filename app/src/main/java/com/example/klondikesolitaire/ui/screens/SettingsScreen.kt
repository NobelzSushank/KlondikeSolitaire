package com.example.klondikesolitaire.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.klondikesolitaire.viewmodel.AppViewModel

@Composable
fun SettingsScreen(
    appViewModel: AppViewModel,
    onBack: () -> Unit
) {
    val settings by appViewModel.settings.collectAsState()
    var showResetConfirm by remember { mutableStateOf(false) }

    Surface {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.headlineSmall)

            Text("Draw Mode", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DrawModeChip("Draw 1", selected = !settings.drawModeIsThree, onClick = { appViewModel.setDrawMode(false) }, modifier = Modifier.weight(1f))
                DrawModeChip("Draw 3", selected = settings.drawModeIsThree, onClick = { appViewModel.setDrawMode(true) }, modifier = Modifier.weight(1f))
            }

            SettingsToggle("Sound", settings.soundEnabled) { appViewModel.setSoundEnabled(it) }
            SettingsToggle("Haptics", settings.hapticsEnabled) { appViewModel.setHapticsEnabled(it) }
            SettingsToggle("Non-personalized ads", settings.nonPersonalizedAds) { appViewModel.setNonPersonalizedAds(it) }
            SettingsToggle("Left-handed mode", settings.leftHandedMode) { appViewModel.setLeftHandedMode(it) }
            SettingsToggle("Autocomplete", settings.autocompleteEnabled) { appViewModel.setAutocompleteEnabled(it) }

            Button(onClick = { showResetConfirm = true }) {
                Text("Reset stats")
            }

            Button(onClick = onBack) { Text("Back") }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("Reset stats?") },
            text = { Text("This clears games played, wins, streak, and best time.") },
            confirmButton = {
                Button(onClick = {
                    appViewModel.resetStats()
                    showResetConfirm = false
                }) { Text("Reset") }
            },
            dismissButton = {
                Button(onClick = { showResetConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DrawModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxedAction(
        label = label,
        selected = selected,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
private fun SettingsToggle(
    label: String,
    checked: Boolean,
    onChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}

@Composable
private fun BoxedAction(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
    }
}
