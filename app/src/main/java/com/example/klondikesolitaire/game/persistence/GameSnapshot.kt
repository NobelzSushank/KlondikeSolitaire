package com.example.klondikesolitaire.game.persistence

import com.example.klondikesolitaire.game.model.Card
import com.example.klondikesolitaire.game.model.DrawMode
import com.example.klondikesolitaire.game.model.GameState
import com.example.klondikesolitaire.game.model.Rank
import com.example.klondikesolitaire.game.model.Suit
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serializable DTO so the game can be persisted safely into DataStore as a JSON String.
 *
 * Uses org.json (built into Android) so we don't need extra dependencies.
 *
 * Parsing is tolerant to missing/unknown fields so older saves won't crash.
 */
data class GameSnapshot(
    val drawMode: DrawMode,
    val stock: List<Card>,
    val waste: List<Card>,
    val foundations: List<List<Card>>,
    val tableau: List<List<Card>>,
    val moves: Int
) {
    companion object {
        fun fromState(state: GameState): GameSnapshot = GameSnapshot(
            drawMode = state.drawMode,
            stock = state.stock,
            waste = state.waste,
            foundations = state.foundations,
            tableau = state.tableau,
            moves = state.moves
        )

        fun toState(snapshot: GameSnapshot): GameState {
            val win = snapshot.foundations.sumOf { it.size } == 52
            return GameState(
                drawMode = snapshot.drawMode,
                stock = snapshot.stock,
                waste = snapshot.waste,
                foundations = snapshot.foundations,
                tableau = snapshot.tableau,
                moves = snapshot.moves,
                isWin = win
            )
        }

        /**
         * Parse from JSON string. Returns null if parsing fails.
         */
        fun fromJson(json: String): GameSnapshot? {
            return try {
                val obj = JSONObject(json)

                val drawModeStr = obj.optString("drawMode", DrawMode.Draw1.name)
                val drawMode = runCatching { DrawMode.valueOf(drawModeStr) }.getOrElse { DrawMode.Draw1 }

                val stock = readCardList(obj.optJSONArray("stock"))
                val waste = readCardList(obj.optJSONArray("waste"))

                val foundations = readPileList(obj.optJSONArray("foundations"), expected = 4)
                val tableau = readPileList(obj.optJSONArray("tableau"), expected = 7)

                val moves = obj.optInt("moves", 0)

                GameSnapshot(
                    drawMode = drawMode,
                    stock = stock,
                    waste = waste,
                    foundations = foundations,
                    tableau = tableau,
                    moves = moves
                )
            } catch (_: Throwable) {
                null
            }
        }

        private fun readPileList(array: JSONArray?, expected: Int): List<List<Card>> {
            if (array == null) return List(expected) { emptyList() }
            val out = MutableList(expected) { emptyList<Card>() }
            val count = minOf(array.length(), expected)
            for (i in 0 until count) {
                val pileArr = array.optJSONArray(i)
                out[i] = readCardList(pileArr)
            }
            return out
        }

        private fun readCardList(array: JSONArray?): List<Card> {
            if (array == null) return emptyList()
            val list = ArrayList<Card>(array.length())
            for (i in 0 until array.length()) {
                val o = array.optJSONObject(i) ?: continue
                val suitStr = o.optString("suit", Suit.Spades.name)
                val rankVal = o.optInt("rank", 1)
                val faceUp = o.optBoolean("faceUp", false)

                val suit = runCatching { Suit.valueOf(suitStr) }.getOrElse { Suit.Spades }
                val rank = Rank.fromValue(rankVal)

                list.add(Card(suit = suit, rank = rank, faceUp = faceUp))
            }
            return list
        }
    }

    fun toJson(): String {
        val obj = JSONObject()
        obj.put("drawMode", drawMode.name)
        obj.put("stock", stock.toJsonArray())
        obj.put("waste", waste.toJsonArray())
        obj.put("foundations", foundations.toPileArray())
        obj.put("tableau", tableau.toPileArray())
        obj.put("moves", moves)
        return obj.toString()
    }
}

private fun List<Card>.toJsonArray(): JSONArray {
    val arr = JSONArray()
    for (c in this) {
        val o = JSONObject()
        o.put("suit", c.suit.name)
        o.put("rank", c.rank.value)
        o.put("faceUp", c.faceUp)
        arr.put(o)
    }
    return arr
}

private fun List<List<Card>>.toPileArray(): JSONArray {
    val arr = JSONArray()
    for (pile in this) {
        arr.put(pile.toJsonArray())
    }
    return arr
}