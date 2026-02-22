package com.example.klondikesolitaire.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.klondikesolitaire.data.AppDataStore
import com.example.klondikesolitaire.data.AppStats
import com.example.klondikesolitaire.game.engine.KlondikeEngine
import com.example.klondikesolitaire.game.model.DrawMode
import com.example.klondikesolitaire.game.model.GameState
import com.example.klondikesolitaire.game.model.Move
import com.example.klondikesolitaire.game.persistence.GameSaveDto
import com.example.klondikesolitaire.game.persistence.GameSnapshot
import com.example.klondikesolitaire.game.persistence.GameThemeSelection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * GameViewModel
 * - Single source of truth (StateFlow<GameUiState>)
 * - Persists/loads via DataStore (JSON)
 * - Simple "addictive loop": streak + best time tracking
 *
 * UI integration can happen later; for now this compiles and supports Home "Continue".
 */
data class GameUiState(
    val game: GameState = KlondikeEngine.newGame(drawMode = DrawMode.Draw1),

    val score: Int = 0,
    val moves: Int = 0,
    val elapsedTimeMs: Long = 0L,

    val selectedTheme: GameThemeSelection = GameThemeSelection(),

    val hintCountThisGame: Int = 0,
    val undoCountThisGame: Int = 0,

    val premiumSessionRemainingMillis: Long = 0L,

    // Stats
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val streak: Int = 0,
    val bestTimeMs: Long = 0L,

    val savedGameExists: Boolean = false,

    // UX flags for a future UI
    val isPaused: Boolean = false,
    val showWinSheet: Boolean = false,

    // Internal meta
    val dealSeed: Long = 0L,
    val drawMode: DrawMode = DrawMode.Draw1
) {
    val stock: List<com.example.klondikesolitaire.game.model.Card> get() = game.stock
    val waste: List<com.example.klondikesolitaire.game.model.Card> get() = game.waste
    val foundations: List<List<com.example.klondikesolitaire.game.model.Card>> get() = game.foundations
    val tableau: List<List<com.example.klondikesolitaire.game.model.Card>> get() = game.tableau
}

sealed class CardSource {
    data object Stock : CardSource()
    data object WasteTop : CardSource()
    data class Tableau(val column: Int, val index: Int) : CardSource() // run start
}

sealed class CardDestination {
    data class Foundation(val index: Int) : CardDestination()
    data class Tableau(val column: Int) : CardDestination()
}

class GameViewModel(appContext: Context) : ViewModel() {

    private val store = AppDataStore(appContext)

    private val _ui = MutableStateFlow(
        GameUiState(
            // initial placeholder dealSeed; will be replaced by continueGame() or startNewGame()
            dealSeed = 0L,
            drawMode = DrawMode.Draw1
        )
    )
    val ui: StateFlow<GameUiState> = _ui

    private val history = ArrayDeque<GameUiState>() // simple undo stack

    private var timerJob: Job? = null
    private var autosaveJob: Job? = null

    init {
        // Keep stats + savedGameExists in sync with DataStore.
        viewModelScope.launch {
            combine(
                store.statsFlow,
                store.savedGameFlow.map { it != null }.distinctUntilChanged(),
                store.settingsFlow
            ) { stats, hasSave, settings -> Triple(stats, hasSave, settings) }
                .collect { (stats, hasSave, settings) ->
                    _ui.value = _ui.value.copy(
                        gamesPlayed = stats.gamesPlayed,
                        wins = stats.wins,
                        streak = stats.streak,
                        bestTimeMs = stats.bestWinTimeMs,
                        savedGameExists = hasSave,
                        selectedTheme = settings.toThemeSelection(),
                        premiumSessionRemainingMillis =
                            (settings.premiumSessionEndsAtMs - System.currentTimeMillis()).coerceAtLeast(0L)
                    )
                }
        }

        // Autosave: whenever the game changes, persist after a short delay.
        // This ensures leaving Game and coming back restores state.
        startAutoSave()
    }

    // -------------------------
    // Actions requested
    // -------------------------

    fun startNewGame(drawMode: DrawMode = _ui.value.drawMode) {
        // Rule: starting a new game counts as "abandon" if previous game wasn't a win.
        // We reset streak when abandoning an unfinished game.
        val previousWasWon = _ui.value.game.isWin
        if (!previousWasWon && _ui.value.savedGameExists) {
            resetStreakOnLossOrAbandon()
        }

        val seed = System.currentTimeMillis() // good enough for now
        val game = KlondikeEngine.newGame(drawMode = drawMode, random = Random(seed))

        // Increment games played.
        viewModelScope.launch {
            store.updateStats { s -> s.copy(gamesPlayed = s.gamesPlayed + 1) }
        }

        history.clear()
        _ui.value = _ui.value.copy(
            game = game,
            drawMode = drawMode,
            dealSeed = seed,
            score = 0,
            moves = 0,
            elapsedTimeMs = 0L,
            hintCountThisGame = 0,
            undoCountThisGame = 0,
            isPaused = false,
            showWinSheet = false
        )

        resume()
    }

    fun continueGame() {
        viewModelScope.launch {
            val dto = store.savedGameFlow.map { it }.distinctUntilChanged().firstOrNull { it != null }
            if (dto == null) {
                // No saved game: start a new one
                startNewGame(_ui.value.drawMode)
                return@launch
            }

            val snap = GameSnapshot.fromJson(dto.snapshotJson)
            val game = snap?.let { GameSnapshot.toState(it) }
            if (game == null) {
                // Corrupt save: clear and start fresh
                store.clearSavedGame()
                startNewGame(dto.drawMode)
                return@launch
            }

            history.clear()
            _ui.value = _ui.value.copy(
                game = game,
                drawMode = dto.drawMode,
                dealSeed = dto.dealSeed,
                score = dto.score,
                moves = game.moves,
                elapsedTimeMs = dto.elapsedTimeMs,
                hintCountThisGame = dto.hintCountThisGame,
                undoCountThisGame = dto.undoCountThisGame,
                premiumSessionRemainingMillis = dto.premiumSessionRemainingMillis,
                selectedTheme = dto.theme,
                isPaused = false,
                showWinSheet = game.isWin
            )

            // Continue implies we're playing; timer should run.
            resume()
        }
    }

    /**
     * Restart the same deal (same shuffle) using the stored dealSeed.
     * This is great for "one more try" behavior.
     */
    fun restartSameDeal() {
        val seed = _ui.value.dealSeed
        val drawMode = _ui.value.drawMode
        if (seed == 0L) {
            startNewGame(drawMode)
            return
        }

        // Restart counts as a new game but NOT as an abandon (optional design).
        // We do NOT reset streak here (it’s the "same deal retry").
        viewModelScope.launch {
            store.updateStats { s -> s.copy(gamesPlayed = s.gamesPlayed + 1) }
        }

        val game = KlondikeEngine.newGame(drawMode = drawMode, random = Random(seed))
        history.clear()
        _ui.value = _ui.value.copy(
            game = game,
            score = 0,
            moves = 0,
            elapsedTimeMs = 0L,
            hintCountThisGame = 0,
            undoCountThisGame = 0,
            isPaused = false,
            showWinSheet = false
        )

        resume()
    }

    fun drawFromStock() {
        applyMove(Move.DrawFromStock, scoreDelta = 0)
    }

    /**
     * Assist tap: if source is stock -> draw
     * if waste/top tableau -> attempt auto-move to foundation, else do nothing (for now).
     * This is intentionally lightweight.
     */
    fun tapCardAssist(source: CardSource) {
        when (source) {
            CardSource.Stock -> drawFromStock()

            CardSource.WasteTop -> {
                val targets = KlondikeEngine.legalTargetsForSelection(_ui.value.game, KlondikeEngine.Selection.WasteTop)

                // Prefer foundation (classic assist behavior).
                val foundation = targets.foundationTargets.firstOrNull()
                if (foundation != null) {
                    applyMove(Move.WasteToFoundation(foundation), scoreDelta = 10)
                    return
                }

                // Fallback to tableau so tap feels responsive when no foundation move exists.
                val tableau = targets.tableauTargets.firstOrNull() ?: return
                applyMove(Move.WasteToTableau(tableau), scoreDelta = 0)
            }

            is CardSource.Tableau -> {
                val sel = KlondikeEngine.Selection.TableauRun(source.column, source.index)
                val targets = KlondikeEngine.legalTargetsForSelection(_ui.value.game, sel)
                val f = targets.foundationTargets.firstOrNull()
                if (f != null) {
                    applyMove(Move.TableauToFoundation(source.column, f), scoreDelta = 10)
                }
            }
        }
    }

    /**
     * Drag move maps to engine moves.
     * UI will eventually provide accurate indices; for now this is ready.
     */
    fun dragMove(source: CardSource, destination: CardDestination) {
        val move = when (source) {
            CardSource.Stock -> Move.DrawFromStock
            CardSource.WasteTop -> when (destination) {
                is CardDestination.Foundation -> Move.WasteToFoundation(destination.index)
                is CardDestination.Tableau -> Move.WasteToTableau(destination.column)
            }
            is CardSource.Tableau -> when (destination) {
                is CardDestination.Foundation -> Move.TableauToFoundation(source.column, destination.index)
                is CardDestination.Tableau -> Move.TableauToTableau(
                    fromColumn = source.column,
                    fromIndex = source.index,
                    toColumn = destination.column
                )
            }
        }

        val scoreDelta = when (move) {
            is Move.WasteToFoundation, is Move.TableauToFoundation -> 10
            else -> 0
        }

        applyMove(move, scoreDelta)
    }

    fun undo() {
        val previous = history.removeLastOrNull() ?: return
        _ui.value = previous.copy(isPaused = _ui.value.isPaused)
        _ui.value = _ui.value.copy(undoCountThisGame = _ui.value.undoCountThisGame + 1)
    }

    fun hint() {
        // Lightweight: count hint usage; real hinting comes later.
        _ui.value = _ui.value.copy(hintCountThisGame = _ui.value.hintCountThisGame + 1)
    }

    fun autocomplete() {
        // Lightweight stub: real autoplay can be implemented later.
        // Keeping action so UI can wire a button without missing method.
    }

    fun pause() {
        if (_ui.value.isPaused) return
        _ui.value = _ui.value.copy(isPaused = true)
        timerJob?.cancel()
        timerJob = null
        // Save when pausing (useful if app backgrounded)
        saveGameOnBackground()
    }

    fun resume() {
        if (!_ui.value.isPaused && timerJob != null) return
        _ui.value = _ui.value.copy(isPaused = false)
        startTimerIfNeeded()
    }

    fun saveGameOnBackground() {
        viewModelScope.launch {
            val dto = buildSaveDtoOrNull(_ui.value) ?: run {
                store.clearSavedGame()
                return@launch
            }
            store.saveGame(dto)
        }
    }

    // -------------------------
    // Internal helpers
    // -------------------------

    private fun applyMove(move: Move, scoreDelta: Int) {
        val current = _ui.value
        val nextGame = KlondikeEngine.applyMove(current.game, move)

        // If nothing changed (invalid move), do nothing.
        if (nextGame == current.game) return

        // Push history for undo.
        history.addLast(current.copy()) // snapshot
        // Keep history small (beginner friendly / memory safe)
        while (history.size > 50) history.removeFirst()

        val next = current.copy(
            game = nextGame,
            moves = nextGame.moves,
            score = (current.score + scoreDelta).coerceAtLeast(0),
            showWinSheet = nextGame.isWin
        )
        _ui.value = next

        // Win handling: update stats once, encourage "Play Again" later via showWinSheet flag.
        if (!current.game.isWin && nextGame.isWin) {
            onWin()
        }
    }

    private fun onWin() {
        pause() // stop timer (and autosave will keep final)

        val elapsed = _ui.value.elapsedTimeMs
        viewModelScope.launch {
            store.updateStats { s ->
                val newWins = s.wins + 1
                val newStreak = s.streak + 1
                val best = if (s.bestWinTimeMs == 0L) elapsed else minOf(s.bestWinTimeMs, elapsed)
                s.copy(wins = newWins, streak = newStreak, bestWinTimeMs = best)
            }
        }

        // Optionally: on win, you might clear "saved game" (since game finished),
        // but keeping it is okay. We'll keep it saved so returning shows win state.
        saveGameOnBackground()
    }

    private fun resetStreakOnLossOrAbandon() {
        viewModelScope.launch {
            store.updateStats { s -> s.copy(streak = 0) }
        }
    }

    private fun buildSaveDtoOrNull(ui: GameUiState): GameSaveDto? {
        // If there is no meaningful game, don't save. (Here we always have a game.)
        val snapshotJson = GameSnapshot.fromState(ui.game).toJson()

        return GameSaveDto(
            dealSeed = ui.dealSeed,
            drawMode = ui.drawMode,
            snapshotJson = snapshotJson,
            score = ui.score,
            elapsedTimeMs = ui.elapsedTimeMs,
            hintCountThisGame = ui.hintCountThisGame,
            undoCountThisGame = ui.undoCountThisGame,
            premiumSessionRemainingMillis = ui.premiumSessionRemainingMillis,
            theme = ui.selectedTheme
        )
    }

    private fun startTimerIfNeeded() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000)
                if (_ui.value.isPaused) continue
                _ui.value = _ui.value.copy(elapsedTimeMs = _ui.value.elapsedTimeMs + 1_000)
            }
        }
    }

    private fun startAutoSave() {
        autosaveJob?.cancel()
        autosaveJob = viewModelScope.launch {
            var lastSavedMoves = -1
            while (true) {
                delay(800)
                val ui = _ui.value

                // Save only when game has started (dealSeed != 0) OR when a saved game already exists.
                if (ui.dealSeed == 0L && !ui.savedGameExists) continue

                // Avoid spamming writes: only save if moves changed or if paused/win.
                val shouldSave = ui.moves != lastSavedMoves || ui.isPaused || ui.game.isWin
                if (!shouldSave) continue

                val dto = buildSaveDtoOrNull(ui) ?: continue
                store.saveGame(dto)
                lastSavedMoves = ui.moves
            }
        }
    }
}
