package com.example.klondikesolitaire.ui.game.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized sizing so the Game UI scales consistently across devices.
 *
 * You compute this inside BoxWithConstraints (because it depends on available maxWidth/maxHeight).
 */
data class GameDimensions(
    val isPortrait: Boolean,

    val sidePadding: Dp,
    val columnGap: Dp,
    val topRowGap: Dp,

    val cardWidth: Dp,
    val cardHeight: Dp,

    val slotCornerRadius: Dp,
    val slotBorderWidth: Dp,

    val overlapFaceDown: Dp,
    val overlapFaceUp: Dp,

    val bottomBarHeight: Dp,
    val hudPaddingTop: Dp
) {
    companion object {
        /**
         * Compute dimensions using the spec:
         * Portrait:
         *  cardWidth = min( (maxWidth - sidePadding*2 - columnGaps*6) / 7, 70dp )
         *  cardHeight = cardWidth * 1.38
         *
         * Landscape:
         *  cap cardWidth to 90dp and reduce overlaps slightly.
         */
        fun calculate(
            maxWidth: Dp,
            maxHeight: Dp,
            sidePadding: Dp = 16.dp,
            columnGapPortrait: Dp = 8.dp,
            columnGapLandscape: Dp = 10.dp,
        ): GameDimensions {
            val isPortrait = maxHeight >= maxWidth

            val columnGap = if (isPortrait) columnGapPortrait else columnGapLandscape
            val topRowGap = if (isPortrait) 12.dp else 10.dp

            val cap = if (isPortrait) 70.dp else 90.dp

            // (maxWidth - sidePadding*2 - columnGaps*6) / 7
            val available = maxWidth - (sidePadding * 2) - (columnGap * 6)
            val raw = available / 7

            // Guard against tiny widths (should never be negative, but safe is good).
            val cardWidth = raw.coerceAtLeast(1.dp).coerceAtMost(cap)
            val cardHeight = cardWidth * 1.38f

            val overlapFaceDown = if (isPortrait) 12.dp else 10.dp
            val overlapFaceUp = if (isPortrait) 22.dp else 18.dp

            return GameDimensions(
                isPortrait = isPortrait,

                sidePadding = sidePadding,
                columnGap = columnGap,
                topRowGap = topRowGap,

                cardWidth = cardWidth,
                cardHeight = cardHeight,

                slotCornerRadius = if (isPortrait) 10.dp else 12.dp,
                slotBorderWidth = 1.dp,

                overlapFaceDown = overlapFaceDown,
                overlapFaceUp = overlapFaceUp,

                bottomBarHeight = 72.dp,
                hudPaddingTop = 12.dp
            )
        }
    }
}