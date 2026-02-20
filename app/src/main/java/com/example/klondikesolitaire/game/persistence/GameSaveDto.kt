package com.example.klondikesolitaire.game.persistence

import com.example.klondikesolitaire.game.model.DrawMode
import org.json.JSONObject

/**
 * Lightweight persisted payload for continuing games.
 *
 * We store:
 * - dealSeed: allows "restart same deal"
 * - drawMode: Draw1/Draw3
 * - snapshotJson: GameSnapshot JSON (stock/waste/foundations/tableau/moves)
 * - score, elapsedTime, hint/undo counts
 * - theme selection placeholders
 * - premiumSessionRemainingMillis (placeholder)
 */
data class GameThemeSelection(
    val backgroundStyle: String = "default_gradient",
    val cardBackStyle: String = "default_back",
    val faceStyle: String = "default_face"
)

data class GameSaveDto(
    val dealSeed: Long,
    val drawMode: DrawMode,

    val snapshotJson: String, // GameSnapshot JSON

    val score: Int,
    val elapsedTimeMs: Long,

    val hintCountThisGame: Int,
    val undoCountThisGame: Int,

    val premiumSessionRemainingMillis: Long,

    val theme: GameThemeSelection
) {
    fun toJson(): String {
        val o = JSONObject()
        o.put("dealSeed", dealSeed)
        o.put("drawMode", drawMode.name)
        o.put("snapshotJson", snapshotJson)
        o.put("score", score)
        o.put("elapsedTimeMs", elapsedTimeMs)
        o.put("hintCountThisGame", hintCountThisGame)
        o.put("undoCountThisGame", undoCountThisGame)
        o.put("premiumSessionRemainingMillis", premiumSessionRemainingMillis)

        val t = JSONObject()
        t.put("backgroundStyle", theme.backgroundStyle)
        t.put("cardBackStyle", theme.cardBackStyle)
        t.put("faceStyle", theme.faceStyle)
        o.put("theme", t)

        return o.toString()
    }

    companion object {
        fun fromJson(json: String): GameSaveDto? {
            return try {
                val o = JSONObject(json)

                val dealSeed = o.optLong("dealSeed", 0L)
                val drawModeStr = o.optString("drawMode", DrawMode.Draw1.name)
                val drawMode = runCatching { DrawMode.valueOf(drawModeStr) }.getOrElse { DrawMode.Draw1 }

                val snapshotJson = o.optString("snapshotJson", "")
                if (snapshotJson.isBlank()) return null

                val score = o.optInt("score", 0)
                val elapsed = o.optLong("elapsedTimeMs", 0L)

                val hint = o.optInt("hintCountThisGame", 0)
                val undo = o.optInt("undoCountThisGame", 0)

                val premium = o.optLong("premiumSessionRemainingMillis", 0L)

                val t = o.optJSONObject("theme")
                val theme = GameThemeSelection(
                    backgroundStyle = t?.optString("backgroundStyle", "default_gradient") ?: "default_gradient",
                    cardBackStyle = t?.optString("cardBackStyle", "default_back") ?: "default_back",
                    faceStyle = t?.optString("faceStyle", "default_face") ?: "default_face"
                )

                GameSaveDto(
                    dealSeed = dealSeed,
                    drawMode = drawMode,
                    snapshotJson = snapshotJson,
                    score = score,
                    elapsedTimeMs = elapsed,
                    hintCountThisGame = hint,
                    undoCountThisGame = undo,
                    premiumSessionRemainingMillis = premium,
                    theme = theme
                )
            } catch (_: Throwable) {
                null
            }
        }
    }
}