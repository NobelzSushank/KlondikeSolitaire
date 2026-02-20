package com.example.klondikesolitaire.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.klondikesolitaire.ads.AdsInitializer
import com.example.klondikesolitaire.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    appViewModel: AppViewModel,
    onDone: () -> Unit
) {
    val context = LocalContext.current.applicationContext
    val initialized by appViewModel.isInitialized.collectAsState()

    LaunchedEffect(Unit) {
        // 1) Initialize DataStore / read theme settings
        appViewModel.initialize()

        // 2) Initialize AdMob safely (won’t crash if unavailable)
        AdsInitializer.initializeSafely(context)

        // 3) Show splash for ~1.2s or until initialization ends (whichever is later)
        val start = System.currentTimeMillis()
        while (!initialized) {
            delay(50)
        }
        val elapsed = System.currentTimeMillis() - start
        val remaining = 1200L - elapsed
        if (remaining > 0) delay(remaining)

        onDone()
    }

    // Premium-ish gradient background
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary
        )
    )

    Surface {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Klondike Solitaire",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.height(18.dp))

                // Subtle loading indicator on a soft chip
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}