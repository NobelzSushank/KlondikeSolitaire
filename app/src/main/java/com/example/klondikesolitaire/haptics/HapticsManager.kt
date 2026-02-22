package com.example.klondikesolitaire.haptics

import android.view.HapticFeedbackConstants
import android.view.View

class HapticsManager(private val view: View) {

    fun performLight(enabled: Boolean) {
        if (!enabled) return
        runCatching {
            view.performHapticFeedback(
                HapticFeedbackConstants.KEYBOARD_TAP,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }

    fun performHeavy(enabled: Boolean) {
        if (!enabled) return
        runCatching {
            view.performHapticFeedback(
                HapticFeedbackConstants.LONG_PRESS,
                HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
            )
        }
    }
}
