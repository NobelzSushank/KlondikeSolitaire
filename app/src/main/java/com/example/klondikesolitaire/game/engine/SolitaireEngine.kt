package com.example.klondikesolitaire.game.engine

/**
 * FIX:
 * Your project now uses the newer Klondike engine:
 * - com.example.klondikesolitaire.game.engine.KlondikeEngine
 * - models in com.example.klondikesolitaire.game.model (Card has faceUp inside)
 *
 * The old file "SolitaireEngine.kt" referenced older types (Selection, CardSlot, etc.)
 * causing many compile errors after the model upgrade.
 *
 * We keep this file (so nothing breaks if something still imports it),
 * but implement it as a thin wrapper that delegates to the new engine.
 */

import com.example.klondikesolitaire.game.model.DrawMode
import com.example.klondikesolitaire.game.model.GameState
import com.example.klondikesolitaire.game.model.Move
import kotlin.random.Random

@Deprecated(
    message = "Use KlondikeEngine directly. This wrapper exists for backward compatibility.",
    replaceWith = ReplaceWith("KlondikeEngine")
)
object SolitaireEngine {

    fun newGame(
        drawMode: DrawMode = DrawMode.Draw1,
        random: Random = Random.Default
    ): GameState = KlondikeEngine.newGame(drawMode = drawMode, random = random)

    fun validateMove(state: GameState, move: Move): Boolean =
        KlondikeEngine.validateMove(state, move)

    fun applyMove(state: GameState, move: Move): GameState =
        KlondikeEngine.applyMove(state, move)

    fun legalTargetsForSelection(
        state: GameState,
        selection: KlondikeEngine.Selection
    ): KlondikeEngine.LegalTargets =
        KlondikeEngine.legalTargetsForSelection(state, selection)
}