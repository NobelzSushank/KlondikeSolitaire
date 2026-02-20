package com.example.klondikesolitaire.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Slot rendering for stock/waste/foundation placeholders:
 * rounded rectangle with subtle border + shadow.
 */
@Composable
fun SlotView(
    highlighted: Boolean,
    cornerRadius: Dp,
    borderWidth: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val borderColor = if (highlighted) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.95f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    }

    val bg = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (highlighted) 10.dp else 6.dp,
                shape = MaterialTheme.shapes.medium
            )
            .background(bg, MaterialTheme.shapes.medium)
            .border(borderWidth, borderColor, MaterialTheme.shapes.medium)
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(Modifier.fillMaxSize()) { content() }
    }
}

/**
 * A tiny decorative square slot (safe placeholder) so the layout matches premium screenshots.
 */
@Composable
fun DecorativeSquareSlot(
    highlighted: Boolean,
    modifier: Modifier = Modifier
) {
    SlotView(
        highlighted = highlighted,
        cornerRadius = 10.dp,
        borderWidth = 1.dp,
        modifier = modifier.aspectRatio(1f)
    ) {
        // leave empty (pure decoration)
    }
}