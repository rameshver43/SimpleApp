package com.example.data

enum class Player(val id: Int, val defaultName: String) {
    PLAYER_1(1, "Hero Crimson"),
    PLAYER_2(2, "Cyber Frost");

    fun opponent(): Player = if (this == PLAYER_1) PLAYER_2 else PLAYER_1
}

data class GameSettings(
    val turnsPerPlayer: Int = 10,
    val autoClickDelaySeconds: Int = 5,
    val isAutoClickEnabled: Boolean = true,
    val soundEffectsEnabled: Boolean = true,
    val soundHapticsEnabled: Boolean = true,
    val player1Name: String = "Hero Crimson",
    val player2Name: String = "Cyber Frost",
    val player1CustomUri: String? = null,
    val player2CustomUri: String? = null
)

data class RollRecord(
    val id: Long = System.currentTimeMillis(),
    val player: Player,
    val score: Int,
    val roundNumber: Int,
    val turnNumber: Int
)

data class FloatingScore(
    val id: Long = System.currentTimeMillis(),
    val player: Player,
    val points: Int
)

data class GameState(
    val player1Score: Int = 0,
    val player2Score: Int = 0,
    val player1TurnsCompleted: Int = 0,
    val player2TurnsCompleted: Int = 0,
    val currentPlayer: Player = Player.PLAYER_1,
    val lastRoll: Int? = null,
    val displayedRollNumber: Int = 0,
    val isRollingAnimation: Boolean = false,
    val isGameOver: Boolean = false,
    val winner: Player? = null,
    val isTie: Boolean = false,
    val timerRemainingMillis: Long = 5000L,
    val timerTotalMillis: Long = 5000L,
    val isTimerPaused: Boolean = false,
    val history: List<RollRecord> = emptyList(),
    val floatingScores: List<FloatingScore> = emptyList()
) {
    val currentRound: Int
        get() = minOf(player1TurnsCompleted, player2TurnsCompleted) + 1
}
