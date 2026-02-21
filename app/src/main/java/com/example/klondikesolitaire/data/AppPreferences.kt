package com.example.klondikesolitaire.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

/**
 * Keys stored in DataStore (Preferences).
 *
 * App-wide:
 * - themeMode: 0 System, 1 Light, 2 Dark
 * - hasSavedGame: used to show/hide Continue button on Home
 *
 * Game persistence:
 * - gameSaveJson: serialized game + meta (JSON string)
 *
 * Lightweight stats:
 * - gamesPlayed, wins, streak, bestWinTimeMs
 */
object AppPreferences {
    val THEME_MODE = intPreferencesKey("theme_mode")
    val HAS_SAVED_GAME = booleanPreferencesKey("has_saved_game")

    val GAME_SAVE_JSON = stringPreferencesKey("game_save_json")

    val STATS_GAMES_PLAYED = intPreferencesKey("stats_games_played")
    val STATS_WINS = intPreferencesKey("stats_wins")
    val STATS_STREAK = intPreferencesKey("stats_streak")
    val STATS_BEST_WIN_TIME_MS = longPreferencesKey("stats_best_win_time_ms")

    val COSMETIC_BACKGROUND_STYLE = stringPreferencesKey("cosmetic_background_style")
    val COSMETIC_CARD_BACK_STYLE = stringPreferencesKey("cosmetic_card_back_style")
    val COSMETIC_FACE_STYLE = stringPreferencesKey("cosmetic_face_style")
    val PREMIUM_SESSION_ENDS_AT_MS = longPreferencesKey("premium_session_ends_at_ms")
}