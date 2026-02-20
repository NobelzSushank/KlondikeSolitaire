package com.example.klondikesolitaire.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.klondikesolitaire.game.model.Card
import com.example.klondikesolitaire.game.model.Color

/**
 * No-image card rendering (works without external assets).
 *
 * Supports:
 * - Face styles (Classic/Modern) via simple typography differences
 * - Back styles (placeholder patterns)
 */
@Composable
fun CardView(
    card: Card,
    faceStyle: String,
    backStyle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
    }

    Surface(
        modifier = modifier
            .border(1.dp, borderColor, MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = onClick,
                onDoubleClick = onDoubleClick
            ),
        shape = MaterialTheme.shapes.small,
        tonalElevation = 2.dp
    ) {
        if (!card.faceUp) {
            // Back styles (placeholders)
            val backBrush = when (backStyle) {
                "modern_back" -> Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
                    )
                )
                "classic_back" -> Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.90f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.80f)
                    )
                )
                else -> Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.75f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backBrush)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                // Tiny "pattern" text to fake texture
                Text(
                    text = "◆ ◆",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        } else {
            // Face styles
            val isModern = faceStyle == "modern_face"
            val label = card.shortLabel()

            val faceTextStyle = if (isModern) {
                MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                // "Classic": a slightly heavier serif-ish feel (still uses system fonts)
                MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            }

            val color = when (card.color) {
                Color.Red -> MaterialTheme.colorScheme.error
                Color.Black -> MaterialTheme.colorScheme.onSurface
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(PaddingValues(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = faceTextStyle,
                    color = color
                )
            }
        }
    }
}