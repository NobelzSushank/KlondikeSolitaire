package com.example.klondikesolitaire.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.klondikesolitaire.game.persistence.GameSaveDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "klondike_prefs")

enum class ThemeMode(val id: Int) {
    System(0),
    Light(1),
    Dark(2);

    companion object {
        fun fromId(id: Int): ThemeMode = entries.firstOrNull { it.id == id } ?: System
    }
}

data class AppStats(
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val streak: Int = 0,
    val bestWinTimeMs: Long = 0L // 0 = none yet
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val hasSavedGame: Boolean = false
)

/**
 * DataStore manager (Preferences) — beginner-friendly wrapper.
 *
 * Note: This is safe against IO corruption (emits defaults so app still runs).
 */
class AppDataStore(private val context: Context) {

    val settingsFlow: Flow<AppSettings> =
        context.dataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences()) else throw e
            }
            .map { prefs ->
                val themeId = prefs[AppPreferences.THEME_MODE] ?: ThemeMode.System.id
                AppSettings(
                    themeMode = ThemeMode.fromId(themeId),
                    hasSavedGame = prefs[AppPreferences.HAS_SAVED_GAME] ?: false
                )
            }

    val statsFlow: Flow<AppStats> =
        context.dataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences()) else throw e
            }
            .map { prefs ->
                AppStats(
                    gamesPlayed = prefs[AppPreferences.STATS_GAMES_PLAYED] ?: 0,
                    wins = prefs[AppPreferences.STATS_WINS] ?: 0,
                    streak = prefs[AppPreferences.STATS_STREAK] ?: 0,
                    bestWinTimeMs = prefs[AppPreferences.STATS_BEST_WIN_TIME_MS] ?: 0L
                )
            }

    val savedGameFlow: Flow<GameSaveDto?> =
        context.dataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences()) else throw e
            }
            .map { prefs ->
                val json = prefs[AppPreferences.GAME_SAVE_JSON]
                if (json.isNullOrBlank()) null else GameSaveDto.fromJson(json)
            }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[AppPreferences.THEME_MODE] = mode.id }
    }

    suspend fun setHasSavedGame(value: Boolean) {
        context.dataStore.edit { it[AppPreferences.HAS_SAVED_GAME] = value }
    }

    suspend fun saveGame(dto: GameSaveDto) {
        context.dataStore.edit { prefs ->
            prefs[AppPreferences.GAME_SAVE_JSON] = dto.toJson()
            prefs[AppPreferences.HAS_SAVED_GAME] = true
        }
    }

    suspend fun clearSavedGame() {
        context.dataStore.edit { prefs ->
            prefs.remove(AppPreferences.GAME_SAVE_JSON)
            prefs[AppPreferences.HAS_SAVED_GAME] = false
        }
    }

    suspend fun updateStats(transform: (AppStats) -> AppStats) {
        context.dataStore.edit { prefs ->
            val current = AppStats(
                gamesPlayed = prefs[AppPreferences.STATS_GAMES_PLAYED] ?: 0,
                wins = prefs[AppPreferences.STATS_WINS] ?: 0,
                streak = prefs[AppPreferences.STATS_STREAK] ?: 0,
                bestWinTimeMs = prefs[AppPreferences.STATS_BEST_WIN_TIME_MS] ?: 0L
            )
            val updated = transform(current)
            prefs[AppPreferences.STATS_GAMES_PLAYED] = updated.gamesPlayed
            prefs[AppPreferences.STATS_WINS] = updated.wins
            prefs[AppPreferences.STATS_STREAK] = updated.streak
            prefs[AppPreferences.STATS_BEST_WIN_TIME_MS] = updated.bestWinTimeMs
        }
    }
}