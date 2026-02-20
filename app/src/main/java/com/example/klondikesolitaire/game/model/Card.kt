package com.example.klondikesolitaire.game.model

/**
 * Canonical card models for the project (used by the new KlondikeEngine).
 *
 * NOTE:
 * Keeping everything here avoids duplicate declarations and keeps package names consistent.
 */

enum class Color { Red, Black }

enum class Suit(val color: Color) {
    Spades(Color.Black),
    Clubs(Color.Black),
    Hearts(Color.Red),
    Diamonds(Color.Red)
}

enum class Rank(val value: Int) {
    Ace(1),
    Two(2),
    Three(3),
    Four(4),
    Five(5),
    Six(6),
    Seven(7),
    Eight(8),
    Nine(9),
    Ten(10),
    Jack(11),
    Queen(12),
    King(13);

    companion object {
        fun fromValue(v: Int): Rank = entries.firstOrNull { it.value == v } ?: Ace
    }
}

/**
 * A single card. faceUp is part of the model (important for Klondike rules).
 */
data class Card(
    val suit: Suit,
    val rank: Rank,
    val faceUp: Boolean
) {
    val color: Color get() = suit.color

    fun flipUp(): Card = if (faceUp) this else copy(faceUp = true)
    fun flipDown(): Card = if (!faceUp) this else copy(faceUp = false)

    fun shortLabel(): String {
        val r = when (rank) {
            Rank.Ace -> "A"
            Rank.Jack -> "J"
            Rank.Queen -> "Q"
            Rank.King -> "K"
            else -> rank.value.toString()
        }
        val s = when (suit) {
            Suit.Spades -> "♠"
            Suit.Clubs -> "♣"
            Suit.Hearts -> "♥"
            Suit.Diamonds -> "♦"
        }
        return "$r$s"
    }
}