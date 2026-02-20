package com.example.klondikesolitaire.game.engine

import com.example.klondikesolitaire.game.model.Card
import com.example.klondikesolitaire.game.model.DrawMode
import com.example.klondikesolitaire.game.model.GameState
import com.example.klondikesolitaire.game.model.Move
import com.example.klondikesolitaire.game.model.Rank
import com.example.klondikesolitaire.game.model.Suit
import kotlin.random.Random

/**
 * Pure game engine (no Android/Compose dependencies).
 *
 * The engine is written as immutable transformations:
 * - validateMove(state, move)
 * - applyMove(state, move)
 *
 * UI can call legalDestinations(...) to highlight valid targets.
 */
object KlondikeEngine {

    // ----------------------------
    // New game / setup
    // ----------------------------

    fun newGame(
        drawMode: DrawMode = DrawMode.Draw1,
        random: Random = Random.Default
    ): GameState {
        val deck = buildDeck()
            .map { it.flipDown() }
            .shuffled(random)

        // Deal 7 tableau columns (1..7 cards). Only top faceUp.
        val tableau = MutableList(7) { mutableListOf<Card>() }

        var idx = 0
        for (col in 0 until 7) {
            val count = col + 1
            val slice = deck.subList(idx, idx + count)
            idx += count

            val dealt = slice.mapIndexed { i, c ->
                if (i == slice.lastIndex) c.flipUp() else c.flipDown()
            }
            tableau[col].addAll(dealt)
        }

        val remaining = deck.subList(idx, deck.size)
        val stock = remaining.map { it.flipDown() }

        return recomputeWin(
            GameState(
                drawMode = drawMode,
                stock = stock,
                waste = emptyList(),
                foundations = List(4) { emptyList() },
                tableau = tableau.map { it.toList() },
                moves = 0,
                isWin = false
            )
        )
    }

    private fun buildDeck(): List<Card> {
        val suits = listOf(Suit.Spades, Suit.Clubs, Suit.Hearts, Suit.Diamonds)
        val ranks = Rank.entries
        val deck = mutableListOf<Card>()
        for (s in suits) for (r in ranks) deck += Card(suit = s, rank = r, faceUp = false)
        return deck
    }

    // ----------------------------
    // Validation / applying moves
    // ----------------------------

    fun validateMove(state: GameState, move: Move): Boolean = when (move) {
        Move.DrawFromStock -> state.stock.isNotEmpty() || state.waste.isNotEmpty()
        Move.RecycleWasteToStock -> state.stock.isEmpty() && state.waste.isNotEmpty()

        is Move.WasteToFoundation -> canMoveWasteToFoundation(state, move.foundationIndex)
        is Move.WasteToTableau -> canMoveWasteToTableau(state, move.toColumn)

        is Move.TableauToFoundation -> canMoveTableauTopToFoundation(state, move.fromColumn, move.foundationIndex)
        is Move.TableauToTableau -> canMoveTableauRunToTableau(state, move.fromColumn, move.fromIndex, move.toColumn)
    }

    /**
     * Apply move if valid; otherwise returns the original state unchanged.
     */
    fun applyMove(state: GameState, move: Move): GameState {
        if (!validateMove(state, move)) return state

        val moved = when (move) {
            Move.DrawFromStock -> drawFromStock(state)
            Move.RecycleWasteToStock -> recycleWasteToStock(state)

            is Move.WasteToFoundation -> wasteToFoundation(state, move.foundationIndex)
            is Move.WasteToTableau -> wasteToTableau(state, move.toColumn)

            is Move.TableauToFoundation -> tableauTopToFoundation(state, move.fromColumn, move.foundationIndex)
            is Move.TableauToTableau -> tableauRunToTableau(state, move.fromColumn, move.fromIndex, move.toColumn)
        }

        return recomputeWin(moved)
    }

    // ----------------------------
    // Legal targets helper
    // ----------------------------

    /**
     * A simple structure to report where a selected card/run can go.
     */
    data class LegalTargets(
        val foundationTargets: List<Int>,
        val tableauTargets: List<Int>
    )

    sealed class Selection {
        data object WasteTop : Selection()
        data class TableauRun(val fromColumn: Int, val fromIndex: Int) : Selection()
    }

    /**
     * Returns legal destination indices for a selection:
     * - foundationTargets: [0..3] indices you can drop onto
     * - tableauTargets: [0..6] column indices you can drop onto
     */
    fun legalTargetsForSelection(state: GameState, selection: Selection): LegalTargets {
        return when (selection) {
            Selection.WasteTop -> {
                val card = state.waste.lastOrNull() ?: return LegalTargets(emptyList(), emptyList())
                val f = (0..3).filter { canPlaceOnFoundation(card, state.foundations[it].lastOrNull()) }
                val t = (0..6).filter { canPlaceOnTableau(card, state.tableau[it].lastOrNull()) }
                LegalTargets(foundationTargets = f, tableauTargets = t)
            }

            is Selection.TableauRun -> {
                val col = state.tableau.getOrNull(selection.fromColumn) ?: return LegalTargets(emptyList(), emptyList())
                if (selection.fromIndex !in col.indices) return LegalTargets(emptyList(), emptyList())
                val run = col.subList(selection.fromIndex, col.size)
                if (run.isEmpty() || run.any { !it.faceUp } || !isValidRun(run)) {
                    return LegalTargets(emptyList(), emptyList())
                }

                // Only single-card run can go to foundation.
                val f = if (run.size == 1) {
                    val card = run.first()
                    (0..3).filter { canPlaceOnFoundation(card, state.foundations[it].lastOrNull()) }
                } else emptyList()

                val bottomCard = run.first()
                val t = (0..6)
                    .filter { it != selection.fromColumn }
                    .filter { canPlaceOnTableau(bottomCard, state.tableau[it].lastOrNull()) }

                LegalTargets(foundationTargets = f, tableauTargets = t)
            }
        }
    }

    // ----------------------------
    // Draw / recycle
    // ----------------------------

    private fun drawFromStock(state: GameState): GameState {
        if (state.stock.isEmpty()) {
            // When stock is empty, a "draw" typically recycles.
            return recycleWasteToStock(state).copy(moves = state.moves + 1)
        }

        val drawCount = if (state.drawMode == DrawMode.Draw1) 1 else 3
        val actual = minOf(drawCount, state.stock.size)

        val newStock = state.stock.dropLast(actual)
        val drawn = state.stock.takeLast(actual).map { it.flipUp() }

        val newWaste = state.waste + drawn
        return state.copy(stock = newStock, waste = newWaste, moves = state.moves + 1)
    }

    private fun recycleWasteToStock(state: GameState): GameState {
        if (state.stock.isNotEmpty() || state.waste.isEmpty()) return state

        // Typical Klondike: flip waste into stock, keeping order so the earliest waste becomes bottom.
        // waste top is last -> when recycling, stock top should become the first card to draw next.
        val recycled = state.waste
            .asReversed()
            .map { it.flipDown() }

        return state.copy(stock = recycled, waste = emptyList())
    }

    // ----------------------------
    // Waste moves
    // ----------------------------

    private fun canMoveWasteToFoundation(state: GameState, foundationIndex: Int): Boolean {
        val card = state.waste.lastOrNull() ?: return false
        val pile = state.foundations.getOrNull(foundationIndex) ?: return false
        return canPlaceOnFoundation(card, pile.lastOrNull())
    }

    private fun wasteToFoundation(state: GameState, foundationIndex: Int): GameState {
        val card = state.waste.last()
        val newWaste = state.waste.dropLast(1)

        val foundations = state.foundations.toMutableList()
        foundations[foundationIndex] = foundations[foundationIndex] + card

        return state.copy(
            waste = newWaste,
            foundations = foundations,
            moves = state.moves + 1
        )
    }

    private fun canMoveWasteToTableau(state: GameState, toColumn: Int): Boolean {
        val card = state.waste.lastOrNull() ?: return false
        val dest = state.tableau.getOrNull(toColumn) ?: return false
        return canPlaceOnTableau(card, dest.lastOrNull())
    }

    private fun wasteToTableau(state: GameState, toColumn: Int): GameState {
        val card = state.waste.last()
        val newWaste = state.waste.dropLast(1)

        val tableau = state.tableau.toMutableList()
        tableau[toColumn] = tableau[toColumn] + card.copy(faceUp = true)

        return state.copy(
            waste = newWaste,
            tableau = tableau,
            moves = state.moves + 1
        )
    }

    // ----------------------------
    // Tableau -> Foundation
    // ----------------------------

    private fun canMoveTableauTopToFoundation(state: GameState, fromColumn: Int, foundationIndex: Int): Boolean {
        val col = state.tableau.getOrNull(fromColumn) ?: return false
        val card = col.lastOrNull() ?: return false
        if (!card.faceUp) return false

        val foundation = state.foundations.getOrNull(foundationIndex) ?: return false
        return canPlaceOnFoundation(card, foundation.lastOrNull())
    }

    private fun tableauTopToFoundation(state: GameState, fromColumn: Int, foundationIndex: Int): GameState {
        val tableau = state.tableau.toMutableList()
        val from = tableau[fromColumn].toMutableList()

        val card = from.removeLast()

        // Flip new top if needed
        if (from.isNotEmpty() && !from.last().faceUp) {
            from[from.lastIndex] = from.last().flipUp()
        }

        tableau[fromColumn] = from.toList()

        val foundations = state.foundations.toMutableList()
        foundations[foundationIndex] = foundations[foundationIndex] + card

        return state.copy(
            tableau = tableau,
            foundations = foundations,
            moves = state.moves + 1
        )
    }

    // ----------------------------
    // Tableau -> Tableau (run)
    // ----------------------------

    private fun canMoveTableauRunToTableau(state: GameState, fromColumn: Int, fromIndex: Int, toColumn: Int): Boolean {
        if (fromColumn == toColumn) return false
        val from = state.tableau.getOrNull(fromColumn) ?: return false
        val to = state.tableau.getOrNull(toColumn) ?: return false
        if (fromIndex !in from.indices) return false

        val run = from.subList(fromIndex, from.size)
        if (run.isEmpty()) return false
        if (run.any { !it.faceUp }) return false
        if (!isValidRun(run)) return false

        val bottomCard = run.first()
        return canPlaceOnTableau(bottomCard, to.lastOrNull())
    }

    private fun tableauRunToTableau(state: GameState, fromColumn: Int, fromIndex: Int, toColumn: Int): GameState {
        val tableau = state.tableau.toMutableList()
        val from = tableau[fromColumn].toMutableList()
        val to = tableau[toColumn].toMutableList()

        val run = from.subList(fromIndex, from.size).toList()

        // Remove run from source
        repeat(run.size) { from.removeAt(fromIndex) }

        // Flip new top if needed
        if (from.isNotEmpty() && !from.last().faceUp) {
            from[from.lastIndex] = from.last().flipUp()
        }

        // Add to destination (all faceUp)
        run.forEach { c -> to.add(c.copy(faceUp = true)) }

        tableau[fromColumn] = from.toList()
        tableau[toColumn] = to.toList()

        return state.copy(tableau = tableau, moves = state.moves + 1)
    }

    // ----------------------------
    // Rule helpers
    // ----------------------------

    /**
     * Foundation: same suit, ascending Ace -> King.
     */
    private fun canPlaceOnFoundation(card: Card, top: Card?): Boolean {
        if (top == null) return card.rank == Rank.Ace
        return card.suit == top.suit && card.rank.value == top.rank.value + 1
    }

    /**
     * Tableau: descending and alternating colors.
     * Empty tableau: only King can be placed.
     */
    private fun canPlaceOnTableau(card: Card, top: Card?): Boolean {
        if (top == null) return card.rank == Rank.King
        if (!top.faceUp) return false
        val rankOk = card.rank.value == top.rank.value - 1
        val colorOk = card.color != top.color
        return rankOk && colorOk
    }

    /**
     * Valid run means:
     * - all faceUp (checked elsewhere too)
     * - each card descends by 1
     * - alternating colors
     *
     * Example: [7♠, 6♥, 5♣] (bottom -> top)
     */
    private fun isValidRun(run: List<Card>): Boolean {
        if (run.isEmpty()) return false
        for (i in 0 until run.lastIndex) {
            val a = run[i]
            val b = run[i + 1]
            if (a.rank.value != b.rank.value + 1) return false
            if (a.color == b.color) return false
        }
        return true
    }

    private fun recomputeWin(state: GameState): GameState {
        val total = state.foundations.sumOf { it.size }
        return state.copy(isWin = total == 52)
    }
}