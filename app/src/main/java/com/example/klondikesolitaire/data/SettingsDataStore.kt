package com.example.klondikesolitaire.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "settings")

data class SettingsState(
    val soundEnabled: Boolean = true,
    val showHints: Boolean = true,
    val coins: Int = 0
)

class SettingsDataStore(private val context: Context) {

    val settingsFlow: Flow<SettingsState> =
        context.dataStore.data
            .catch { e ->
                // If DataStore is corrupted, emit empty preferences so app still runs.
                if (e is IOException) emit(emptyPreferences()) else throw e
            }
            .map { prefs ->
                SettingsState(
                    soundEnabled = prefs[PreferencesKeys.SOUND_ENABLED] ?: true,
                    showHints = prefs[PreferencesKeys.SHOW_HINTS] ?: true,
                    coins = prefs[PreferencesKeys.COINS] ?: 0
                )
            }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_ENABLED] = enabled }
    }

    suspend fun setShowHints(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SHOW_HINTS] = enabled }
    }

    suspend fun addCoins(delta: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.COINS] ?: 0
            prefs[PreferencesKeys.COINS] = (current + delta).coerceAtLeast(0)
        }
    }
}