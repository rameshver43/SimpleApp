package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Player
import com.example.ui.components.CenterBattleArena
import com.example.ui.components.PlayerCard
import com.example.ui.components.SettingsDialog
import com.example.ui.components.WinnerPopupDialog
import com.example.ui.theme.ArenaDarkBg
import com.example.ui.theme.ArenaSurfaceBg
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Player1Color
import com.example.ui.theme.Player2Color
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattleClashScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = modifier
            .fillMaxSize()
            .testTag("battle_clash_screen"),
        containerColor = ArenaDarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GoldAccent,
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = "Dice Icon",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = "BATTLE CLASH",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )

                        // Round Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "ROUND ${gameState.currentRound}/${settings.turnsPerPlayer}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldAccent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Quick Auto-roll Pause / Resume Toggle
                    if (settings.isAutoClickEnabled && !gameState.isGameOver) {
                        IconButton(
                            onClick = { viewModel.togglePauseTimer() },
                            modifier = Modifier.testTag("top_bar_pause_toggle")
                        ) {
                            Icon(
                                imageVector = if (gameState.isTimerPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = if (gameState.isTimerPaused) "Resume Auto Roll" else "Pause Auto Roll",
                                tint = if (gameState.isTimerPaused) GoldAccent else Color.White
                            )
                        }
                    }

                    // Restart / Rematch
                    IconButton(
                        onClick = { viewModel.restartGame() },
                        modifier = Modifier.testTag("restart_game_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Restart Battle",
                            tint = Color.White
                        )
                    }

                    // Settings
                    IconButton(
                        onClick = {
                            viewModel.playButtonClick()
                            showSettingsDialog = true
                        },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Open Settings",
                            tint = GoldAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ArenaSurfaceBg
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ArenaSurfaceBg,
                            ArenaDarkBg,
                            Color(0xFF090A14)
                        )
                    )
                )
        ) {
            // Main Horizontal Gaming Arena Row
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT TOP CHARACTER: Player 1
                PlayerCard(
                    player = Player.PLAYER_1,
                    name = settings.player1Name,
                    score = gameState.player1Score,
                    turnsCompleted = gameState.player1TurnsCompleted,
                    maxTurns = settings.turnsPerPlayer,
                    isCurrentTurn = gameState.currentPlayer == Player.PLAYER_1 && !gameState.isGameOver,
                    customAvatarPath = settings.player1CustomUri,
                    floatingScores = gameState.floatingScores,
                    onAvatarSelected = { uri ->
                        viewModel.setPlayerAvatar(Player.PLAYER_1, uri)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )

                // CENTER FOOTER & ARENA: Turn Banner, 1-50 Rolling Orb, Footer Circle Button
                CenterBattleArena(
                    gameState = gameState,
                    settings = settings,
                    onRollClick = { viewModel.onRollButtonClicked() },
                    onTogglePause = { viewModel.togglePauseTimer() },
                    modifier = Modifier
                        .weight(1.3f)
                        .padding(horizontal = 4.dp)
                )

                // RIGHT TOP CHARACTER: Player 2
                PlayerCard(
                    player = Player.PLAYER_2,
                    name = settings.player2Name,
                    score = gameState.player2Score,
                    turnsCompleted = gameState.player2TurnsCompleted,
                    maxTurns = settings.turnsPerPlayer,
                    isCurrentTurn = gameState.currentPlayer == Player.PLAYER_2 && !gameState.isGameOver,
                    customAvatarPath = settings.player2CustomUri,
                    floatingScores = gameState.floatingScores,
                    onAvatarSelected = { uri ->
                        viewModel.setPlayerAvatar(Player.PLAYER_2, uri)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                )
            }
        }
    }

    // Popup Winner Celebration Dialog
    if (gameState.isGameOver) {
        WinnerPopupDialog(
            gameState = gameState,
            settings = settings,
            onRematch = { viewModel.restartGame() },
            onOpenSettings = {
                viewModel.restartGame()
                showSettingsDialog = true
            },
            onDismiss = { viewModel.restartGame() }
        )
    }

    // Settings Configuration Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            currentSettings = settings,
            onSaveSettings = { updated ->
                viewModel.updateSettings(updated)
            },
            onResetAvatars = {
                viewModel.resetPlayerAvatar(Player.PLAYER_1)
                viewModel.resetPlayerAvatar(Player.PLAYER_2)
            },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
