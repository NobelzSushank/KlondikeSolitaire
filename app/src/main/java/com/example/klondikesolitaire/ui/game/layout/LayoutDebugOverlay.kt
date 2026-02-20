package com.example.klondikesolitaire.ui.game.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Debug-only overlay to verify sizing on real devices.
 * Keep it small, readable, and optional.
 */
@Composable
fun LayoutDebugOverlay(
    dims: GameDimensions,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                shape = MaterialTheme.shapes.medium
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text = "card: ${dims.cardWidth} x ${dims.cardHeight}\n" +
                    "portrait: ${dims.isPortrait}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}