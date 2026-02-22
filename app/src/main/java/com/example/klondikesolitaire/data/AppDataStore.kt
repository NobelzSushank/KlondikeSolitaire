package com.example.klondikesolitaire.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.klondikesolitaire.game.persistence.GameSaveDto
import com.example.klondikesolitaire.game.persistence.GameThemeSelection
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
    val bestWinTimeMs: Long = 0L
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.System,
    val hasSavedGame: Boolean = false,
    val backgroundStyle: String = "default_gradient",
    val cardBackStyle: String = "default_back",
    val faceStyle: String = "classic_face",
    val premiumSessionEndsAtMs: Long = 0L,
    val drawModeIsThree: Boolean = false,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val nonPersonalizedAds: Boolean = false,
    val leftHandedMode: Boolean = false,
    val autocompleteEnabled: Boolean = true
) {
    val isPremiumSessionActive: Boolean
        get() = premiumSessionEndsAtMs > System.currentTimeMillis()

    fun toThemeSelection(): GameThemeSelection = GameThemeSelection(
        backgroundStyle = backgroundStyle,
        cardBackStyle = cardBackStyle,
        faceStyle = faceStyle
    )
}

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
                    hasSavedGame = prefs[AppPreferences.HAS_SAVED_GAME] ?: false,
                    backgroundStyle = prefs[AppPreferences.COSMETIC_BACKGROUND_STYLE] ?: "default_gradient",
                    cardBackStyle = prefs[AppPreferences.COSMETIC_CARD_BACK_STYLE] ?: "default_back",
                    faceStyle = prefs[AppPreferences.COSMETIC_FACE_STYLE] ?: "classic_face",
                    premiumSessionEndsAtMs = prefs[AppPreferences.PREMIUM_SESSION_ENDS_AT_MS] ?: 0L,
                    drawModeIsThree = (prefs[AppPreferences.SETTING_DRAW_MODE] ?: 1) == 3,
                    soundEnabled = prefs[AppPreferences.SETTING_SOUND_ENABLED] ?: true,
                    hapticsEnabled = prefs[AppPreferences.SETTING_HAPTICS_ENABLED] ?: true,
                    nonPersonalizedAds = prefs[AppPreferences.SETTING_NON_PERSONALIZED_ADS] ?: false,
                    leftHandedMode = prefs[AppPreferences.SETTING_LEFT_HANDED_MODE] ?: false,
                    autocompleteEnabled = prefs[AppPreferences.SETTING_AUTOCOMPLETE_ENABLED] ?: true
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

    suspend fun setDrawMode(isDrawThree: Boolean) {
        context.dataStore.edit { it[AppPreferences.SETTING_DRAW_MODE] = if (isDrawThree) 3 else 1 }
    }

    suspend fun setSoundEnabled(value: Boolean) {
        context.dataStore.edit { it[AppPreferences.SETTING_SOUND_ENABLED] = value }
    }

    suspend fun setHapticsEnabled(value: Boolean) {
        context.dataStore.edit { it[AppPreferences.SETTING_HAPTICS_ENABLED] = value }
    }

    suspend fun setNonPersonalizedAds(value: Boolean) {
        context.dataStore.edit { it[AppPreferences.SETTING_NON_PERSONALIZED_ADS] = value }
    }

    suspend fun setLeftHandedMode(value: Boolean) {
        context.dataStore.edit { it[AppPreferences.SETTING_LEFT_HANDED_MODE] = value }
    }

    suspend fun setAutocompleteEnabled(value: Boolean) {
        context.dataStore.edit { it[AppPreferences.SETTING_AUTOCOMPLETE_ENABLED] = value }
    }

    suspend fun setBackgroundStyle(styleId: String) {
        context.dataStore.edit { it[AppPreferences.COSMETIC_BACKGROUND_STYLE] = styleId }
    }

    suspend fun setCardBackStyle(styleId: String) {
        context.dataStore.edit { it[AppPreferences.COSMETIC_CARD_BACK_STYLE] = styleId }
    }

    suspend fun setFaceStyle(styleId: String) {
        context.dataStore.edit { it[AppPreferences.COSMETIC_FACE_STYLE] = styleId }
    }

    suspend fun startPremiumSession(durationMillis: Long) {
        val endsAt = System.currentTimeMillis() + durationMillis.coerceAtLeast(0L)
        context.dataStore.edit { it[AppPreferences.PREMIUM_SESSION_ENDS_AT_MS] = endsAt }
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

    suspend fun resetStats() {
        context.dataStore.edit { prefs ->
            prefs[AppPreferences.STATS_GAMES_PLAYED] = 0
            prefs[AppPreferences.STATS_WINS] = 0
            prefs[AppPreferences.STATS_STREAK] = 0
            prefs[AppPreferences.STATS_BEST_WIN_TIME_MS] = 0L
        }
    }
}
