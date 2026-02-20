package com.example.klondikesolitaire.ui.game.layout

import androidx.compose.ui.unit.Dp

/**
 * Very simple orientation detection that works naturally with BoxWithConstraints:
 * if height >= width => portrait.
 */
fun isPortrait(maxWidth: Dp, maxHeight: Dp): Boolean = maxHeight >= maxWidth