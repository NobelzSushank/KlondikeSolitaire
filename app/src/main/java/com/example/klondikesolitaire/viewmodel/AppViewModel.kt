package com.example.klondikesolitaire.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.klondikesolitaire.data.AppDataStore
import com.example.klondikesolitaire.data.AppSettings
import com.example.klondikesolitaire.data.AppStats
import com.example.klondikesolitaire.data.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppViewModel(private val appContext: Context) : ViewModel() {

    private val store = AppDataStore(appContext)

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings

    private val _stats = MutableStateFlow(AppStats())
    val stats: StateFlow<AppStats> = _stats

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    fun initialize() {
        if (_isInitialized.value) return

        viewModelScope.launch {
            val initial = store.settingsFlow.first()
            _settings.value = initial
            _isInitialized.value = true

            viewModelScope.launch {
                store.settingsFlow.collect { newSettings ->
                    _settings.value = newSettings
                }
            }

            viewModelScope.launch {
                store.statsFlow.collect { value ->
                    _stats.value = value
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

    fun setDrawMode(isDrawThree: Boolean) {
        viewModelScope.launch { store.setDrawMode(isDrawThree) }
    }

    fun setSoundEnabled(value: Boolean) {
        viewModelScope.launch { store.setSoundEnabled(value) }
    }

    fun setHapticsEnabled(value: Boolean) {
        viewModelScope.launch { store.setHapticsEnabled(value) }
    }

    fun setNonPersonalizedAds(value: Boolean) {
        viewModelScope.launch { store.setNonPersonalizedAds(value) }
    }

    fun setLeftHandedMode(value: Boolean) {
        viewModelScope.launch { store.setLeftHandedMode(value) }
    }

    fun setAutocompleteEnabled(value: Boolean) {
        viewModelScope.launch { store.setAutocompleteEnabled(value) }
    }

    fun setBackgroundStyle(styleId: String) {
        viewModelScope.launch { store.setBackgroundStyle(styleId) }
    }

    fun setCardBackStyle(styleId: String) {
        viewModelScope.launch { store.setCardBackStyle(styleId) }
    }

    fun setFaceStyle(styleId: String) {
        viewModelScope.launch { store.setFaceStyle(styleId) }
    }

    fun startPremiumSession(durationMillis: Long = 20 * 60 * 1000L) {
        viewModelScope.launch { store.startPremiumSession(durationMillis) }
    }

    fun resetStats() {
        viewModelScope.launch { store.resetStats() }
    }

    fun clearSavedGame() {
        viewModelScope.launch { store.clearSavedGame() }
    }
}
