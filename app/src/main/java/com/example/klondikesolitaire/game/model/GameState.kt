package com.example.klondikesolitaire.game.model

/**
 * Full Klondike state:
 * - stock/waste
 * - 4 foundations
 * - 7 tableau columns
 */
enum class DrawMode { Draw1, Draw3 }

data class GameState(
    val drawMode: DrawMode = DrawMode.Draw1,

    val stock: List<Card> = emptyList(),                  // face-down (top is last)
    val waste: List<Card> = emptyList(),                  // face-up (top is last)

    val foundations: List<List<Card>> = List(4) { emptyList() }, // top is last
    val tableau: List<List<Card>> = List(7) { emptyList() },     // bottom is index 0, top is last

    val moves: Int = 0,
    val isWin: Boolean = false
)