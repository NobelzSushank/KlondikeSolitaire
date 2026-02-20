package com.example.klondikesolitaire.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.klondikesolitaire.data.ThemeMode
import com.example.klondikesolitaire.ui.navigation.AppNavGraph
import com.example.klondikesolitaire.ui.theme.KlondikeSolitaireTheme
import com.example.klondikesolitaire.viewmodel.AppViewModel
import com.example.klondikesolitaire.viewmodel.AppViewModelFactory
import androidx.compose.ui.platform.LocalContext

@Composable
fun App() {
    val context = LocalContext.current.applicationContext
    val vm: AppViewModel = viewModel(factory = AppViewModelFactory(context))

    val settings by vm.settings.collectAsState()

    // Apply light/dark based on stored preference.
    val darkTheme = when (settings.themeMode) {
        ThemeMode.System -> false // (Beginner default; you can hook to system later)
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    KlondikeSolitaireTheme(darkTheme = darkTheme) {
        AppNavGraph(appViewModel = vm)
    }
}