package com.example.klondikesolitaire.game.model

/**
 * Moves are the only way to change state.
 * This makes validation + applying moves predictable and testable.
 */
sealed class Move {
    /**
     * Draw from stock into waste according to draw mode:
     * - Draw1: 1 card
     * - Draw3: up to 3 cards
     */
    data object DrawFromStock : Move()

    /**
     * Optional explicit recycle move. (Engine may also recycle when DrawFromStock and stock is empty.)
     * Recycle takes all waste, flips them face-down, and puts into stock (preserving typical Klondike order).
     */
    data object RecycleWasteToStock : Move()

    data class WasteToFoundation(val foundationIndex: Int) : Move()
    data class WasteToTableau(val toColumn: Int) : Move()

    /**
     * Move one card (top of a tableau column) to a foundation.
     */
    data class TableauToFoundation(
        val fromColumn: Int,
        val foundationIndex: Int
    ) : Move()

    /**
     * Move a face-up run from one tableau column to another.
     *
     * fromIndex is the starting index of the run within fromColumn.
     * Example: if column has 10 cards and fromIndex=7, moving cards [7..9].
     */
    data class TableauToTableau(
        val fromColumn: Int,
        val fromIndex: Int,
        val toColumn: Int
    ) : Move()
}