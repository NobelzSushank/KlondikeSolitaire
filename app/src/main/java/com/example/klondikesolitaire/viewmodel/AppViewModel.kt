package com.example.klondikesolitaire.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.klondikesolitaire.data.AppDataStore
import com.example.klondikesolitaire.data.AppSettings
import com.example.klondikesolitaire.data.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Single source of truth for app-wide settings needed early (Splash -> Theme -> Navigation).
 */
class AppViewModel(private val appContext: Context) : ViewModel() {

    private val store = AppDataStore(appContext)

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    /**
     * Called from SplashScreen.
     *
     * Loads DataStore once and exposes it via StateFlow.
     */
    fun initialize() {
        if (_isInitialized.value) return

        viewModelScope.launch {
            // Read once for initialization, then continue collecting changes.
            val initial = store.settingsFlow.first()
            _settings.value = initial
            _isInitialized.value = true

            // Keep collecting changes (Settings screen can update later).
            viewModelScope.launch {
                store.settingsFlow.collect { newSettings ->
                    _settings.value = newSettings
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { store.setThemeMode(mode) }
    }

    fun setHasSavedGame(value: Boolean) {
        viewModelScope.launch { store.setHasSavedGame(value) }
    }

    // Simple stubs to simulate game persistence for now:
    fun createFakeSavedGame() = setHasSavedGame(true)
    fun clearSavedGame() = setHasSavedGame(false)
}