package com.example.klondikesolitaire.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.klondikesolitaire.audio.SoundManager
import com.example.klondikesolitaire.game.engine.KlondikeEngine
import com.example.klondikesolitaire.data.AppDataStore
import com.example.klondikesolitaire.data.AppSettings
import com.example.klondikesolitaire.game.model.Card
import com.example.klondikesolitaire.game.model.DrawMode
import com.example.klondikesolitaire.game.model.GameState
import com.example.klondikesolitaire.game.model.Move
import com.example.klondikesolitaire.ui.components.CardView
import com.example.klondikesolitaire.ui.components.DecorativeSquareSlot
import com.example.klondikesolitaire.ui.components.SlotView
import com.example.klondikesolitaire.haptics.HapticsManager
import com.example.klondikesolitaire.ui.game.layout.GameDimensions
import com.example.klondikesolitaire.ui.game.layout.LayoutDebugOverlay
import com.example.klondikesolitaire.viewmodel.CardDestination
import com.example.klondikesolitaire.viewmodel.CardSource
import com.example.klondikesolitaire.viewmodel.GameViewModel
import com.example.klondikesolitaire.viewmodel.GameViewModelFactory
import kotlinx.coroutines.delay
import android.os.SystemClock
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class GameEntryMode {
    ForceNew,
    ContinueOrNew
}

private sealed class DropTarget {
    data class Foundation(val index: Int) : DropTarget()
    data class Tableau(val index: Int) : DropTarget()
}

private data class ActiveDrag(
    val source: CardSource,
    val card: Card,
    val startTopLeft: Offset,
    val size: IntSize,
    val legalFoundationTargets: Set<Int>,
    val legalTableauTargets: Set<Int>
)

@Composable
fun GameScreen(
    entryMode: GameEntryMode,
    onGoHome: () -> Unit
) {
    var showPauseDialog by remember { mutableStateOf(false) }
    var showLayoutDebug by remember { mutableStateOf(false) }
    var alwaysShowAutoComplete by rememberSaveable { mutableStateOf(false) }

    val appContext = LocalContext.current.applicationContext
    val hostView = LocalView.current
    val vm: GameViewModel = viewModel(factory = GameViewModelFactory(appContext))
    val ui by vm.ui.collectAsState()
    val appStore = remember(appContext) { AppDataStore(appContext) }
    val appSettings by appStore.settingsFlow.collectAsState(initial = AppSettings())
    val soundManager = remember(appContext) { SoundManager(appContext) }
    val hapticsManager = remember(hostView) { HapticsManager(hostView) }

    DisposableEffect(soundManager) {
        onDispose { soundManager.release() }
    }

    LaunchedEffect(ui.showWinSheet) {
        if (ui.showWinSheet) {
            soundManager.playWin(appSettings.soundEnabled)
            hapticsManager.performHeavy(appSettings.hapticsEnabled)
        }
    }

    val scope = rememberCoroutineScope()
    val targetRects = remember { mutableStateMapOf<DropTarget, Rect>() }

    var activeDrag by remember { mutableStateOf<ActiveDrag?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    var hintFoundations by remember { mutableStateOf(emptySet<Int>()) }
    var hintTableau by remember { mutableStateOf(emptySet<Int>()) }
    var selectedSource by remember { mutableStateOf<CardSource?>(null) }
    var lastTapSource by remember { mutableStateOf<CardSource?>(null) }
    var lastTapUptimeMs by remember { mutableLongStateOf(0L) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showThemesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(entryMode) {
        when (entryMode) {
            GameEntryMode.ForceNew -> vm.startNewGame(if (appSettings.drawModeIsThree) DrawMode.Draw3 else DrawMode.Draw1)
            GameEntryMode.ContinueOrNew -> vm.continueGame()
        }
    }

    BackHandler { showPauseDialog = true }

    Surface {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val dims = remember(maxWidth, maxHeight) {
                GameDimensions.calculate(maxWidth = maxWidth, maxHeight = maxHeight)
            }
            val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

            val bgBrush = when (ui.selectedTheme.backgroundStyle) {
                "bg_ocean" -> Brush.verticalGradient(listOf(Color(0xFF0D47A1), Color(0xFF1976D2)))
                "bg_sunset" -> Brush.verticalGradient(listOf(Color(0xFFBF360C), Color(0xFFF57C00)))
                "bg_royal" -> Brush.verticalGradient(listOf(Color(0xFF4A148C), Color(0xFF6A1B9A)))
                "bg_aurora" -> Brush.verticalGradient(listOf(Color(0xFF004D40), Color(0xFF26A69A)))
                "bg_ruby" -> Brush.verticalGradient(listOf(Color(0xFF880E4F), Color(0xFFC2185B)))
                "bg_night" -> Brush.verticalGradient(listOf(Color(0xFF212121), Color(0xFF424242)))
                "bg_gold" -> Brush.verticalGradient(listOf(Color(0xFF8D6E63), Color(0xFFD4AF37)))
                else -> Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.96f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.84f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.78f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgBrush)
                    .padding(
                        PaddingValues(
                            start = dims.sidePadding,
                            end = dims.sidePadding,
                            top = dims.hudPaddingTop + statusBarTop,
                            bottom = dims.bottomBarHeight + 10.dp + navBarBottom
                        )
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TopHud(
                        score = ui.score,
                        elapsedTimeMs = ui.elapsedTimeMs,
                        moves = ui.moves
                    )

                    TopRowArea(
                        dims = dims,
                        ui = ui,
                        hintFoundations = hintFoundations,
                        activeDrag = activeDrag,
                        selectedSource = selectedSource,
                        leftHandedMode = appSettings.leftHandedMode,
                        onTargetRect = { target, rect -> targetRects[target] = rect },
                        onStockTap = {
                            vm.drawFromStock()
                            soundManager.playDeal(appSettings.soundEnabled)
                            hapticsManager.performLight(appSettings.hapticsEnabled)
                        },
                        onWasteTap = {
                            vm.tapCardAssist(CardSource.WasteTop)
                            soundManager.playMove(appSettings.soundEnabled)
                            hapticsManager.performLight(appSettings.hapticsEnabled)
                        },
                        onStartWasteDrag = { card, startTopLeft, size ->
                            val legal = KlondikeEngine.legalTargetsForSelection(
                                ui.game,
                                KlondikeEngine.Selection.WasteTop
                            )
                            activeDrag = ActiveDrag(
                                source = CardSource.WasteTop,
                                card = card,
                                startTopLeft = startTopLeft,
                                size = size,
                                legalFoundationTargets = legal.foundationTargets.toSet(),
                                legalTableauTargets = legal.tableauTargets.toSet()
                            )
                            dragOffset = Offset.Zero
                        },
                        onWasteDrag = { amount ->
                            dragOffset += amount
                        },
                        onEndDrag = onEndDrag@{
                            val drag = activeDrag ?: return@onEndDrag
                            val destination = findDropTarget(
                                targetRects = targetRects,
                                point = drag.startTopLeft + dragOffset + Offset(
                                    drag.size.width / 2f,
                                    drag.size.height / 2f
                                )
                            )
                            handleDrop(
                                vm = vm,
                                uiState = ui.game,
                                drag = drag,
                                destination = destination,
                                onSuccess = {
                                    activeDrag = null
                                    dragOffset = Offset.Zero
                                    soundManager.playMove(appSettings.soundEnabled)
                                    hapticsManager.performLight(appSettings.hapticsEnabled)
                                },
                                onIllegal = {
                                    soundManager.playInvalid(appSettings.soundEnabled)
                                    scope.launch {
                                        val startX = dragOffset.x
                                        val startY = dragOffset.y
                                        val steps = 8
                                        repeat(steps) { idx ->
                                            val t = (idx + 1) / steps.toFloat()
                                            dragOffset = Offset(
                                                x = startX * (1f - t),
                                                y = startY * (1f - t)
                                            )
                                            delay(16)
                                        }
                                        activeDrag = null
                                        dragOffset = Offset.Zero
                                    }
                                }
                            )
                        }
                    )

                    TableauArea(
                        dims = dims,
                        ui = ui,
                        hintTableau = hintTableau,
                        activeDrag = activeDrag,
                        selectedSource = selectedSource,
                        leftHandedMode = appSettings.leftHandedMode,
                        onTargetRect = { target, rect -> targetRects[target] = rect },
                        onCardTap = { source ->
                            val now = SystemClock.uptimeMillis()
                            val isDoubleTap = lastTapSource == source && (now - lastTapUptimeMs) <= 260L
                            lastTapSource = source
                            lastTapUptimeMs = now

                            if (isDoubleTap) {
                                vm.tapCardAssist(source)
                                soundManager.playMove(appSettings.soundEnabled)
                                hapticsManager.performLight(appSettings.hapticsEnabled)
                                selectedSource = null
                            } else {
                                val current = selectedSource
                                if (current is CardSource.Tableau && source is CardSource.Tableau && current.column != source.column) {
                                    val move = Move.TableauToTableau(
                                        fromColumn = current.column,
                                        fromIndex = current.index,
                                        toColumn = source.column
                                    )
                                    if (KlondikeEngine.validateMove(ui.game, move)) {
                                        vm.dragMove(current, CardDestination.Tableau(source.column))
                                        soundManager.playMove(appSettings.soundEnabled)
                                        hapticsManager.performLight(appSettings.hapticsEnabled)
                                    } else {
                                        soundManager.playInvalid(appSettings.soundEnabled)
                                    }
                                    selectedSource = null
                                } else {
                                    selectedSource = source
                                }
                            }
                        },
                        onStartCardDrag = { source, card, startTopLeft, size ->
                            val legal = when (source) {
                                is CardSource.Tableau -> KlondikeEngine.legalTargetsForSelection(
                                    ui.game,
                                    KlondikeEngine.Selection.TableauRun(source.column, source.index)
                                )
                                else -> KlondikeEngine.LegalTargets(emptyList(), emptyList())
                            }
                            selectedSource = source
                            activeDrag = ActiveDrag(
                                source = source,
                                card = card,
                                startTopLeft = startTopLeft,
                                size = size,
                                legalFoundationTargets = legal.foundationTargets.toSet(),
                                legalTableauTargets = legal.tableauTargets.toSet()
                            )
                            dragOffset = Offset.Zero
                        },
                        onCardDrag = { amount -> dragOffset += amount },
                        onEndDrag = onEndDrag@{
                            val drag = activeDrag ?: return@onEndDrag
                            val destination = findDropTarget(
                                targetRects = targetRects,
                                point = drag.startTopLeft + dragOffset + Offset(
                                    drag.size.width / 2f,
                                    drag.size.height / 2f
                                )
                            )

                            handleDrop(
                                vm = vm,
                                uiState = ui.game,
                                drag = drag,
                                destination = destination,
                                onSuccess = {
                                    activeDrag = null
                                    dragOffset = Offset.Zero
                                    soundManager.playMove(appSettings.soundEnabled)
                                    hapticsManager.performLight(appSettings.hapticsEnabled)
                                },
                                onIllegal = {
                                    soundManager.playInvalid(appSettings.soundEnabled)
                                    scope.launch {
                                        val startX = dragOffset.x
                                        val startY = dragOffset.y
                                        val steps = 8
                                        repeat(steps) { idx ->
                                            val t = (idx + 1) / steps.toFloat()
                                            dragOffset = Offset(
                                                x = startX * (1f - t),
                                                y = startY * (1f - t)
                                            )
                                            delay(16)
                                        }
                                        activeDrag = null
                                        dragOffset = Offset.Zero
                                    }
                                }
                            )
                        }
                    )
                }

                if (com.example.klondikesolitaire.BuildConfig.DEBUG && showLayoutDebug) {
                    LayoutDebugOverlay(
                        dims = dims,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 40.dp)
                    )
                }
            }

            if (activeDrag != null) {
                val drag = activeDrag ?: return@BoxWithConstraints
                val density = LocalDensity.current
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (drag.startTopLeft.x + dragOffset.x).roundToInt(),
                                y = (drag.startTopLeft.y + dragOffset.y).roundToInt()
                            )
                        }
                        .size(
                            width = with(density) { drag.size.width.toDp() },
                            height = with(density) { drag.size.height.toDp() }
                        )
                        .shadow(12.dp, MaterialTheme.shapes.small)
                ) {
                    CardView(
                        card = drag.card,
                        faceStyle = ui.selectedTheme.faceStyle,
                        backStyle = ui.selectedTheme.cardBackStyle,
                        isSelected = true,
                        onClick = {},
                        onDoubleClick = {},
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            val endgameLikely = remember(ui.game, alwaysShowAutoComplete, appSettings.autocompleteEnabled) {
                appSettings.autocompleteEnabled && (
                    alwaysShowAutoComplete ||
                        (ui.stock.isEmpty() && ui.waste.isEmpty() && ui.foundations.sumOf { it.size } >= 20)
                )
            }

            if (endgameLikely) {
                FilledTonalButton(
                    onClick = { vm.autocomplete() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = dims.bottomBarHeight + 10.dp + navBarBottom)
                        .combinedClickable(
                            onClick = { vm.autocomplete() },
                            onLongClick = { alwaysShowAutoComplete = !alwaysShowAutoComplete }
                        )
                ) {
                    Text("Auto Complete")
                }
            }

            BottomBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navBarBottom),
                onSettings = { showSettingsDialog = true },
                onThemes = { showThemesDialog = true },
                onPlay = {
                    vm.startNewGame(if (appSettings.drawModeIsThree) DrawMode.Draw3 else DrawMode.Draw1)
                    soundManager.playDeal(appSettings.soundEnabled)
                    hapticsManager.performLight(appSettings.hapticsEnabled)
                },
                onHints = {
                    vm.hint()
                    val hintTargets = findHintTargets(ui.game)
                    hintFoundations = hintTargets.first
                    hintTableau = hintTargets.second
                    scope.launch {
                        delay(750)
                        hintFoundations = emptySet()
                        hintTableau = emptySet()
                    }
                },
                onUndo = { vm.undo() }
            )

            if (showSettingsDialog) {
                AlertDialog(
                    onDismissRequest = { showSettingsDialog = false },
                    title = { Text("Settings") },
                    text = {
                        Text(
                            "Settings panel will be connected here.\n\n" +
                                "Tip: Use Play for a fresh deal, Hints for legal-target glow, and Undo to roll back."
                        )
                    },
                    confirmButton = {
                        Button(onClick = { showSettingsDialog = false }) { Text("Close") }
                    }
                )
            }

            if (showThemesDialog) {
                AlertDialog(
                    onDismissRequest = { showThemesDialog = false },
                    title = { Text("Themes") },
                    text = { Text("Card face/back themes are active from saved theme selection. Full picker UI can be wired to this action.") },
                    confirmButton = {
                        Button(onClick = { showThemesDialog = false }) { Text("Close") }
                    }
                )
            }

            if (showPauseDialog) {
                PauseDialog(
                    onResume = {
                        showPauseDialog = false
                        vm.resume()
                    },
                    onNewGame = {
                        showPauseDialog = false
                        vm.startNewGame(if (appSettings.drawModeIsThree) DrawMode.Draw3 else DrawMode.Draw1)
                    },
                    onHome = {
                        showPauseDialog = false
                        vm.saveGameOnBackground()
                        onGoHome()
                    },
                    onDismiss = {
                        showPauseDialog = false
                        vm.resume()
                    }
                )
                vm.pause()
            }

            if (ui.showWinSheet) {
                WinDialog(
                    onPlayAgain = {
                    vm.startNewGame(ui.drawMode)
                    soundManager.playDeal(appSettings.soundEnabled)
                    hapticsManager.performLight(appSettings.hapticsEnabled)
                },
                    onHome = {
                        vm.saveGameOnBackground()
                        onGoHome()
                    }
                )
            }
        }
    }
}

@Composable
private fun TopHud(score: Int, elapsedTimeMs: Long, moves: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HudLabel("Score: $score")
        HudLabel("Time: ${formatElapsed(elapsedTimeMs)}")
        HudLabel("Moves: $moves")
    }
}

@Composable
private fun HudLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.45f),
                offset = Offset(1f, 1f),
                blurRadius = 3f
            )
        ),
        color = Color.White,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun TopRowArea(
    dims: GameDimensions,
    ui: com.example.klondikesolitaire.viewmodel.GameUiState,
    hintFoundations: Set<Int>,
    activeDrag: ActiveDrag?,
    selectedSource: CardSource?,
    leftHandedMode: Boolean,
    onTargetRect: (DropTarget, Rect) -> Unit,
    onStockTap: () -> Unit,
    onWasteTap: () -> Unit,
    onStartWasteDrag: (Card, Offset, IntSize) -> Unit,
    onWasteDrag: (Offset) -> Unit,
    onEndDrag: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(dims.topRowGap)) {
            val stockSlot: @Composable () -> Unit = {
                SlotView(
                    highlighted = false,
                    cornerRadius = dims.slotCornerRadius,
                    borderWidth = dims.slotBorderWidth,
                    modifier = Modifier
                        .width(dims.cardWidth)
                        .height(dims.cardHeight)
                        .combinedClickable(onClick = onStockTap)
                ) {
                    val stockTop = ui.stock.lastOrNull()
                    if (stockTop != null) {
                        CardView(
                            card = stockTop,
                            faceStyle = ui.selectedTheme.faceStyle,
                            backStyle = ui.selectedTheme.cardBackStyle,
                            isSelected = false,
                            onClick = onStockTap,
                            onDoubleClick = onStockTap,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            val wasteSlot: @Composable () -> Unit = {
                SlotView(
                    highlighted = false,
                    cornerRadius = dims.slotCornerRadius,
                    borderWidth = dims.slotBorderWidth,
                    modifier = Modifier
                        .width(dims.cardWidth)
                        .height(dims.cardHeight)
                ) {
                    val topWaste = ui.waste.lastOrNull()
                    if (topWaste != null) {
                        var wasteRect by remember { mutableStateOf(Rect.Zero) }
                        CardView(
                            card = topWaste,
                            faceStyle = ui.selectedTheme.faceStyle,
                            backStyle = ui.selectedTheme.cardBackStyle,
                            isSelected = activeDrag?.source == CardSource.WasteTop || selectedSource == CardSource.WasteTop,
                            onClick = onWasteTap,
                            onDoubleClick = onWasteTap,
                            modifier = Modifier
                                .fillMaxSize()
                                .onGloballyPositioned { wasteRect = it.boundsInRoot() }
                                .pointerInput(topWaste, activeDrag) {
                                    detectDragGestures(
                                        onDragStart = {
                                            onStartWasteDrag(topWaste, wasteRect.topLeft, IntSize(wasteRect.width.roundToInt(), wasteRect.height.roundToInt()))
                                        },
                                        onDragEnd = onEndDrag,
                                        onDragCancel = onEndDrag,
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            onWasteDrag(dragAmount)
                                        }
                                    )
                                }
                        )
                    }
                }
            }

            if (leftHandedMode) {
                wasteSlot()
                stockSlot()
            } else {
                stockSlot()
                wasteSlot()
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            repeat(4) { foundationIndex ->
                val highlighted = foundationIndex in hintFoundations ||
                    (activeDrag?.legalFoundationTargets?.contains(foundationIndex) == true)

                SlotView(
                    highlighted = highlighted,
                    cornerRadius = dims.slotCornerRadius,
                    borderWidth = dims.slotBorderWidth,
                    modifier = Modifier
                        .width(dims.cardWidth)
                        .height(dims.cardHeight)
                        .onGloballyPositioned {
                            onTargetRect(DropTarget.Foundation(foundationIndex), it.boundsInRoot())
                        }
                ) {
                    ui.foundations[foundationIndex].lastOrNull()?.let { card ->
                        CardView(
                            card = card,
                            faceStyle = ui.selectedTheme.faceStyle,
                            backStyle = ui.selectedTheme.cardBackStyle,
                            isSelected = false,
                            onClick = {},
                            onDoubleClick = {},
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            DecorativeSquareSlot(
                highlighted = false,
                modifier = Modifier
                    .width(dims.cardWidth * 0.6f)
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun TableauArea(
    dims: GameDimensions,
    ui: com.example.klondikesolitaire.viewmodel.GameUiState,
    hintTableau: Set<Int>,
    activeDrag: ActiveDrag?,
    selectedSource: CardSource?,
    leftHandedMode: Boolean,
    onTargetRect: (DropTarget, Rect) -> Unit,
    onCardTap: (CardSource) -> Unit,
    onStartCardDrag: (CardSource, Card, Offset, IntSize) -> Unit,
    onCardDrag: (Offset) -> Unit,
    onEndDrag: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(dims.columnGap)
    ) {
        repeat(7) { columnIndex ->
            val pile = ui.tableau[columnIndex]
            val highlighted = columnIndex in hintTableau ||
                (activeDrag?.legalTableauTargets?.contains(columnIndex) == true)

            Box(
                modifier = Modifier
                    .width(dims.cardWidth)
                    .fillMaxHeight()
                    .onGloballyPositioned {
                        onTargetRect(DropTarget.Tableau(columnIndex), it.boundsInRoot())
                    }
                    .border(
                        width = if (highlighted) 2.dp else 0.dp,
                        color = if (highlighted) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                if (pile.isEmpty()) {
                    SlotView(
                        highlighted = highlighted,
                        cornerRadius = dims.slotCornerRadius,
                        borderWidth = dims.slotBorderWidth,
                        modifier = Modifier
                            .width(dims.cardWidth)
                            .height(dims.cardHeight)
                    )
                }

                var runningY = 0.dp
                pile.forEachIndexed { cardIndex, card ->
                    val source = CardSource.Tableau(column = columnIndex, index = cardIndex)
                    val overlap = if (card.faceUp) dims.overlapFaceUp else dims.overlapFaceDown
                    var cardRect by remember(cardIndex, columnIndex) { mutableStateOf(Rect.Zero) }

                    CardView(
                        card = card,
                        faceStyle = ui.selectedTheme.faceStyle,
                        backStyle = ui.selectedTheme.cardBackStyle,
                        isSelected = activeDrag?.source == source || selectedSource == source,
                        onClick = { onCardTap(source) },
                        onDoubleClick = null,
                        enableClicks = false,
                        modifier = Modifier
                            .offset(y = runningY)
                            .width(dims.cardWidth)
                            .height(dims.cardHeight)
                            .onGloballyPositioned { cardRect = it.boundsInRoot() }
                            .pointerInput(card, source) {
                                if (!card.faceUp) return@pointerInput
                                detectTapGestures(onTap = { onCardTap(source) })
                            }
                            .pointerInput(card, source, activeDrag) {
                                if (!card.faceUp) return@pointerInput
                                detectDragGestures(
                                    onDragStart = {
                                        onStartCardDrag(
                                            source,
                                            card,
                                            cardRect.topLeft,
                                            IntSize(cardRect.width.roundToInt(), cardRect.height.roundToInt())
                                        )
                                    },
                                    onDragEnd = onEndDrag,
                                    onDragCancel = onEndDrag,
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        onCardDrag(dragAmount)
                                    }
                                )
                            }
                    )

                    runningY += overlap
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    modifier: Modifier = Modifier,
    onSettings: () -> Unit,
    onThemes: () -> Unit,
    onPlay: () -> Unit,
    onHints: () -> Unit,
    onUndo: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 10.dp,
        shadowElevation = 14.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(icon = "⚙", label = "Settings", onClick = onSettings)
            BottomBarItem(icon = "🎨", label = "Themes", onClick = onThemes)
            BottomBarPlayItem(onClick = onPlay)
            BottomBarItem(icon = "💡", label = "Hints", onClick = onHints)
            BottomBarItem(icon = "↶", label = "Undo", onClick = onUndo)
        }
    }
}

@Composable
private fun BottomBarItem(
    icon: String,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp)
    ) {
        Text(icon, fontSize = 22.sp)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun BottomBarPlayItem(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                )
                .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("▶", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        }
        Text("Play", style = MaterialTheme.typography.labelLarge)
    }
}

private fun findDropTarget(targetRects: Map<DropTarget, Rect>, point: Offset): DropTarget? {
    return targetRects.entries.firstOrNull { (_, rect) -> rect.contains(point) }?.key
}

private fun handleDrop(
    vm: GameViewModel,
    uiState: GameState,
    drag: ActiveDrag,
    destination: DropTarget?,
    onSuccess: () -> Unit,
    onIllegal: () -> Unit
) {
    val move = toMove(drag.source, destination) ?: return onIllegal()
    val isValid = KlondikeEngine.validateMove(uiState, move)
    if (!isValid) return onIllegal()

    val cardDestination = when (destination) {
        is DropTarget.Foundation -> CardDestination.Foundation(destination.index)
        is DropTarget.Tableau -> CardDestination.Tableau(destination.index)
        null -> null
    } ?: return onIllegal()

    vm.dragMove(drag.source, cardDestination)
    onSuccess()
}

private fun toMove(source: CardSource, destination: DropTarget?): Move? {
    destination ?: return null
    return when (source) {
        CardSource.Stock -> Move.DrawFromStock
        CardSource.WasteTop -> when (destination) {
            is DropTarget.Foundation -> Move.WasteToFoundation(destination.index)
            is DropTarget.Tableau -> Move.WasteToTableau(destination.index)
        }

        is CardSource.Tableau -> when (destination) {
            is DropTarget.Foundation -> Move.TableauToFoundation(source.column, destination.index)
            is DropTarget.Tableau -> Move.TableauToTableau(source.column, source.index, destination.index)
        }
    }
}

private fun findHintTargets(state: GameState): Pair<Set<Int>, Set<Int>> {
    val fromWaste = KlondikeEngine.legalTargetsForSelection(state, KlondikeEngine.Selection.WasteTop)
    if (fromWaste.foundationTargets.isNotEmpty() || fromWaste.tableauTargets.isNotEmpty()) {
        return fromWaste.foundationTargets.toSet() to fromWaste.tableauTargets.toSet()
    }

    for (col in 0..6) {
        val pile = state.tableau[col]
        val firstFaceUp = pile.indexOfFirst { it.faceUp }
        if (firstFaceUp == -1) continue

        val targets = KlondikeEngine.legalTargetsForSelection(
            state,
            KlondikeEngine.Selection.TableauRun(col, firstFaceUp)
        )
        if (targets.foundationTargets.isNotEmpty() || targets.tableauTargets.isNotEmpty()) {
            return targets.foundationTargets.toSet() to targets.tableauTargets.toSet()
        }
    }

    return emptySet<Int>() to emptySet()
}

private fun formatElapsed(ms: Long): String {
    val totalSeconds = (ms / 1000L).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}


@Composable
private fun PauseDialog(
    onResume: () -> Unit,
    onNewGame: () -> Unit,
    onHome: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paused") },
        text = { Text("What would you like to do?") },
        confirmButton = { Button(onClick = onResume) { Text("Resume") } },
        dismissButton = {
            Column {
                Button(onClick = onNewGame) { Text("New Game") }
                Button(onClick = onHome) { Text("Home") }
            }
        }
    )
}

@Composable
private fun WinDialog(
    onPlayAgain: () -> Unit,
    onHome: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onPlayAgain,
        title = { Text("You Win!") },
        text = { Text("Great streak! Want another deal?") },
        confirmButton = { Button(onClick = onPlayAgain) { Text("Play Again") } },
        dismissButton = { Button(onClick = onHome) { Text("Home") } }
    )
}
