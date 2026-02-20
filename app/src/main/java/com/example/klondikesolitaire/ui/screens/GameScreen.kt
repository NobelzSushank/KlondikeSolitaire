package com.example.klondikesolitaire.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.klondikesolitaire.ui.game.layout.GameDimensions
import com.example.klondikesolitaire.ui.game.layout.LayoutDebugOverlay
import com.example.klondikesolitaire.viewmodel.GameViewModel
import com.example.klondikesolitaire.viewmodel.GameViewModelFactory

enum class GameEntryMode {
    ForceNew,
    ContinueOrNew
}

@Composable
fun GameScreen(
    entryMode: GameEntryMode,
    onGoHome: () -> Unit
) {
    var showPauseDialog by remember { mutableStateOf(false) }
    var showLayoutDebug by remember { mutableStateOf(false) }

    val appContext = LocalContext.current.applicationContext
    val vm: GameViewModel = viewModel(factory = GameViewModelFactory(appContext))
    val ui by vm.ui.collectAsState()

    LaunchedEffect(entryMode) {
        when (entryMode) {
            GameEntryMode.ForceNew -> vm.startNewGame()
            GameEntryMode.ContinueOrNew -> vm.continueGame()
        }
    }

    BackHandler { showPauseDialog = true }

    Surface {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val dims = remember(maxWidth, maxHeight) {
                GameDimensions.calculate(maxWidth = maxWidth, maxHeight = maxHeight)
            }

            val bgBrush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.80f)
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgBrush)
                    .padding(
                        PaddingValues(
                            start = dims.sidePadding,
                            end = dims.sidePadding,
                            top = dims.hudPaddingTop
                        )
                    )
            ) {
                Text(
                    text = "Game Coming Soon",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.Center)
                )

                Text(
                    text = "Moves: ${ui.moves} • Score: ${ui.score}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 6.dp)
                )

                if (com.example.klondikesolitaire.BuildConfig.DEBUG) {
                    FilledTonalButton(
                        onClick = { showLayoutDebug = !showLayoutDebug },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp)
                            .size(width = 120.dp, height = 40.dp)
                    ) {
                        Text(if (showLayoutDebug) "Hide Layout" else "Show Layout")
                    }
                }

                if (com.example.klondikesolitaire.BuildConfig.DEBUG && showLayoutDebug) {
                    LayoutDebugOverlay(
                        dims = dims,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 46.dp)
                    )
                }
            }

            if (showPauseDialog) {
                PauseDialog(
                    onResume = {
                        showPauseDialog = false
                        vm.resume()
                    },
                    onNewGame = {
                        showPauseDialog = false
                        vm.startNewGame()
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
private fun PauseDialog(
    onResume: () -> Unit,
    onNewGame: () -> Unit,
    onHome: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paused") },
        text = {
            Column(Modifier.padding(top = 6.dp)) {
                Text("What would you like to do?")
            }
        },
        confirmButton = {
            Button(onClick = onResume) { Text("Resume") }
        },
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
        confirmButton = {
            Button(onClick = onPlayAgain) { Text("Play Again") }
        },
        dismissButton = {
            Button(onClick = onHome) { Text("Home") }
        }
    )
}
