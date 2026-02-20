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
import androidx.compose.runtime.*

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

@Composable
fun GameScreen(
    onGoHome: () -> Unit
) {
    var showPauseDialog by remember { mutableStateOf(false) }

    // Debug-only: toggle overlay to verify scaling.
    var showLayoutDebug by remember { mutableStateOf(false) }

    // Create GameViewModel scoped to this destination.
    val appContext = LocalContext.current.applicationContext
    val vm: GameViewModel = viewModel(factory = GameViewModelFactory(appContext))

    // Ensure entering the screen restores a saved game if present.
    LaunchedEffect(Unit) {
        vm.continueGame()
    }

    // Back from Game shows PauseDialog.
    BackHandler {
        showPauseDialog = true
    }

    Surface {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val dims = remember(maxWidth, maxHeight) {
                GameDimensions.calculate(maxWidth = maxWidth, maxHeight = maxHeight)
            }

            // Placeholder background that follows the selected theme (for now: gradient).
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
                // Placeholder game content
                Text(
                    text = "Game Coming Soon",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.align(Alignment.Center)
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
                            .padding(top = 4.dp)
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
            // Two secondary actions as buttons inside the dismiss area:
            Column {
                Button(onClick = onNewGame) { Text("New Game") }
                Button(onClick = onHome) { Text("Home") }
            }
        }
    )
}