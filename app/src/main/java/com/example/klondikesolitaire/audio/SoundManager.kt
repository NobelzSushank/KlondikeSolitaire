package com.example.klondikesolitaire.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Lightweight in-game sound manager with graceful fallback.
 *
 * If /res/raw sounds are missing, play calls simply no-op.
 */
class SoundManager(context: Context) {

    private val appContext = context.applicationContext

    private val soundPool: SoundPool = SoundPool.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .setMaxStreams(4)
        .build()

    private val dealId = loadRawByName("deal")
    private val moveId = loadRawByName("move")
    private val invalidId = loadRawByName("invalid")
    private val winId = loadRawByName("win")

    fun playDeal(enabled: Boolean) = play(dealId, enabled)
    fun playMove(enabled: Boolean) = play(moveId, enabled)
    fun playInvalid(enabled: Boolean) = play(invalidId, enabled)
    fun playWin(enabled: Boolean) = play(winId, enabled)

    fun release() {
        runCatching { soundPool.release() }
    }

    private fun loadRawByName(name: String): Int? {
        return runCatching {
            val resId = appContext.resources.getIdentifier(name, "raw", appContext.packageName)
            if (resId == 0) null else soundPool.load(appContext, resId, 1)
        }.getOrNull()
    }

    private fun play(soundId: Int?, enabled: Boolean) {
        if (!enabled) return
        val id = soundId ?: return
        runCatching {
            soundPool.play(id, 1f, 1f, 1, 0, 1f)
        }
    }
}
