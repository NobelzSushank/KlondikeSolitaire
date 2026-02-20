package com.example.klondikesolitaire.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object PreferencesKeys {
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    val SHOW_HINTS = booleanPreferencesKey("show_hints")
    val COINS = intPreferencesKey("coins")
}