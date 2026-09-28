package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffectsManager
import com.example.data.FloatingScore
import com.example.data.GameSettings
import com.example.data.GameState
import com.example.data.GameStorage
import com.example.data.Player
import com.example.data.RollRecord
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = GameStorage(application.applicationContext)

    private val _settings = MutableStateFlow(storage.loadSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    private val _gameState = MutableStateFlow(
        GameState(
            timerRemainingMillis = _settings.value.autoClickDelaySeconds * 1000L,
            timerTotalMillis = _settings.value.autoClickDelaySeconds * 1000L
        )
    )
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    val soundManager = SoundEffectsManager(application.applicationContext).apply {
        isSoundEnabled = _settings.value.soundEffectsEnabled
    }

    private var timerJob: Job? = null
    private var rollingJob: Job? = null

    private val vibrator: Vibrator? by lazy {
        val ctx = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        startTimerLoop()
    }

    private fun startTimerLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val tickInterval = 50L
            while (true) {
                delay(tickInterval)
                val current = _gameState.value
                val cfg = _settings.value

                if (current.isGameOver || current.isTimerPaused || current.isRollingAnimation || !cfg.isAutoClickEnabled) {
                    continue
                }

                val newRemaining = current.timerRemainingMillis - tickInterval
                if (newRemaining <= 0L) {
                    // Auto-click triggered!
                    soundManager.playButtonClick()
                    _gameState.update {
                        it.copy(
                            timerRemainingMillis = cfg.autoClickDelaySeconds * 1000L,
                            timerTotalMillis = cfg.autoClickDelaySeconds * 1000L
                        )
                    }
                    performRoll()
                } else {
                    _gameState.update {
                        it.copy(timerRemainingMillis = newRemaining)
                    }
                }
            }
        }
    }

    fun onRollButtonClicked() {
        val current = _gameState.value
        if (current.isGameOver || current.isRollingAnimation) return

        soundManager.playButtonClick()

        // Reset timer when manually clicked
        val totalMs = _settings.value.autoClickDelaySeconds * 1000L
        _gameState.update {
            it.copy(
                timerRemainingMillis = totalMs,
                timerTotalMillis = totalMs
            )
        }
        performRoll()
    }

    private fun performRoll() {
        if (_gameState.value.isGameOver || _gameState.value.isRollingAnimation) return

        rollingJob?.cancel()
        rollingJob = viewModelScope.launch {
            _gameState.update { it.copy(isRollingAnimation = true) }

            // Dynamic roll scramble animation
            vibrate(50)
            val scrambleSteps = 7
            for (i in 0 until scrambleSteps) {
                val tempNumber = Random.nextInt(1, 51)
                soundManager.playRollTick()
                _gameState.update { it.copy(displayedRollNumber = tempNumber) }
                delay(40L + (i * 12L))
            }

            // Final random number between 1 and 50
            val finalRoll = Random.nextInt(1, 51)
            soundManager.playScoreAdd()
            vibrate(90)

            val state = _gameState.value
            val activePlayer = state.currentPlayer
            val isP1 = activePlayer == Player.PLAYER_1

            val newP1Score = if (isP1) state.player1Score + finalRoll else state.player1Score
            val newP2Score = if (!isP1) state.player2Score + finalRoll else state.player2Score

            val newP1Turns = if (isP1) state.player1TurnsCompleted + 1 else state.player1TurnsCompleted
            val newP2Turns = if (!isP1) state.player2TurnsCompleted + 1 else state.player2TurnsCompleted

            val turnsTarget = _settings.value.turnsPerPlayer
            val gameEnded = newP1Turns >= turnsTarget && newP2Turns >= turnsTarget

            val winner = when {
                !gameEnded -> null
                newP1Score > newP2Score -> Player.PLAYER_1
                newP2Score > newP1Score -> Player.PLAYER_2
                else -> null
            }
            val isTie = gameEnded && (newP1Score == newP2Score)

            val record = RollRecord(
                player = activePlayer,
                score = finalRoll,
                roundNumber = if (isP1) newP1Turns else newP2Turns,
                turnNumber = state.history.size + 1
            )

            val floatItem = FloatingScore(
                player = activePlayer,
                points = finalRoll
            )

            val nextPlayer = if (gameEnded) activePlayer else activePlayer.opponent()
            val totalTimerMs = _settings.value.autoClickDelaySeconds * 1000L

            _gameState.update {
                it.copy(
                    player1Score = newP1Score,
                    player2Score = newP2Score,
                    player1TurnsCompleted = newP1Turns,
                    player2TurnsCompleted = newP2Turns,
                    currentPlayer = nextPlayer,
                    lastRoll = finalRoll,
                    displayedRollNumber = finalRoll,
                    isRollingAnimation = false,
                    isGameOver = gameEnded,
                    winner = winner,
                    isTie = isTie,
                    timerRemainingMillis = totalTimerMs,
                    timerTotalMillis = totalTimerMs,
                    history = listOf(record) + it.history,
                    floatingScores = listOf(floatItem) + it.floatingScores.take(3)
                )
            }

            if (gameEnded) {
                vibrateVictory()
                if (isTie) {
                    soundManager.playTieSound()
                } else {
                    soundManager.playVictoryFanfare()
                }
            } else {
                soundManager.playTurnChange(nextPlayer)
            }
        }
    }

    fun playButtonClick() {
        soundManager.playButtonClick()
    }

    fun togglePauseTimer() {
        soundManager.playButtonClick()
        _gameState.update { it.copy(isTimerPaused = !it.isTimerPaused) }
    }

    fun restartGame() {
        soundManager.playButtonClick()
        val totalMs = _settings.value.autoClickDelaySeconds * 1000L
        _gameState.update {
            GameState(
                timerRemainingMillis = totalMs,
                timerTotalMillis = totalMs,
                isTimerPaused = false
            )
        }
    }

    fun updateSettings(newSettings: GameSettings) {
        _settings.value = newSettings
        soundManager.isSoundEnabled = newSettings.soundEffectsEnabled
        storage.saveSettings(newSettings)
        val totalMs = newSettings.autoClickDelaySeconds * 1000L
        _gameState.update {
            it.copy(
                timerTotalMillis = totalMs,
                timerRemainingMillis = minOf(it.timerRemainingMillis, totalMs)
            )
        }
    }

    fun setPlayerAvatar(player: Player, uri: Uri) {
        viewModelScope.launch {
            soundManager.playButtonClick()
            val savedPath = storage.saveAvatarFromUri(player, uri)
            if (savedPath != null) {
                val updated = if (player == Player.PLAYER_1) {
                    _settings.value.copy(player1CustomUri = savedPath)
                } else {
                    _settings.value.copy(player2CustomUri = savedPath)
                }
                updateSettings(updated)
            }
        }
    }

    fun resetPlayerAvatar(player: Player) {
        soundManager.playButtonClick()
        storage.clearCustomAvatar(player)
        val updated = if (player == Player.PLAYER_1) {
            _settings.value.copy(player1CustomUri = null)
        } else {
            _settings.value.copy(player2CustomUri = null)
        }
        updateSettings(updated)
    }

    private fun vibrate(millis: Long) {
        if (!_settings.value.soundHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(millis)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateVictory() {
        if (!_settings.value.soundHapticsEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 100, 80, 150, 80, 300), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(400)
            }
        } catch (_: Exception) {}
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
