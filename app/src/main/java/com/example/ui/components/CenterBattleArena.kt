package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GameSettings
import com.example.data.GameState
import com.example.data.Player
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Player1Color
import com.example.ui.theme.Player2Color
import com.example.ui.theme.RollButtonGradientEnd
import com.example.ui.theme.RollButtonGradientStart

@Composable
fun CenterBattleArena(
    gameState: GameState,
    settings: GameSettings,
    onRollClick: () -> Unit,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePlayer = gameState.currentPlayer
    val isP1Turn = activePlayer == Player.PLAYER_1
    val activeColor = if (isP1Turn) Player1Color else Player2Color
    val activePlayerName = if (isP1Turn) settings.player1Name else settings.player2Name

    // Infinite pulse for active banner
    val infiniteTransition = rememberInfiniteTransition(label = "arenaBanner")
    val bannerGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bannerGlow"
    )

    // Spin animation when rolling
    val rollRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "diceSpin"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP: Turn Indicator Banner
        Surface(
            modifier = Modifier
                .scale(if (gameState.isRollingAnimation) 1.05f else bannerGlow)
                .shadow(8.dp, shape = RoundedCornerShape(16.dp), spotColor = activeColor),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF14172B),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, activeColor)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isP1Turn) "🔥" else "❄️",
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (gameState.isRollingAnimation) "ROLLING 1-50..." else "$activePlayerName's TURN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = activeColor,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Round ${gameState.currentRound} of ${settings.turnsPerPlayer}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isP1Turn) "🔥" else "❄️",
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // CENTER: Dynamic Number Orb (1 - 50)
        Box(
            modifier = Modifier
                .size(92.dp)
                .testTag("center_dice_orb"),
            contentAlignment = Alignment.Center
        ) {
            // Background glow halo
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                activeColor.copy(alpha = if (gameState.isRollingAnimation) 0.5f else 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Outer ring with rotation if rolling
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .then(if (gameState.isRollingAnimation) Modifier.rotate(rollRotation) else Modifier)
                    .border(
                        width = 2.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                activeColor,
                                GoldAccent,
                                activeColor
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Inner Orb Surface
            Surface(
                modifier = Modifier.size(68.dp),
                shape = CircleShape,
                color = Color(0xFF0D0F1F),
                shadowElevation = 10.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = gameState.displayedRollNumber,
                        transitionSpec = {
                            (scaleIn(tween(120)) + fadeIn(tween(100))).togetherWith(
                                scaleOut(tween(100)) + fadeOut(tween(80))
                            )
                        },
                        label = "rollNumberAnim"
                    ) { rollNum ->
                        if (rollNum > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$rollNum",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (gameState.isRollingAnimation) GoldAccent else Color.White
                                )
                                Text(
                                    text = "1-50",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeColor.copy(alpha = 0.8f)
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = "Dice Ready",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "VS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // FOOTER: Circular Roll Button with Countdown Ring and Auto-Roll Subtext
        FooterCircleButton(
            gameState = gameState,
            settings = settings,
            onRollClick = onRollClick,
            onTogglePause = onTogglePause
        )
    }
}

@Composable
private fun FooterCircleButton(
    gameState: GameState,
    settings: GameSettings,
    onRollClick: () -> Unit,
    onTogglePause: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed || gameState.isRollingAnimation) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 400f),
        label = "btnScale"
    )

    // Countdown progress fraction (1.0 down to 0.0)
    val progressFraction = if (settings.isAutoClickEnabled && gameState.timerTotalMillis > 0) {
        (gameState.timerRemainingMillis.toFloat() / gameState.timerTotalMillis.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    // Countdown color transition
    val countdownColor = when {
        progressFraction > 0.5f -> Color(0xFF10B981) // Green
        progressFraction > 0.2f -> GoldAccent        // Amber
        else -> Color(0xFFFF3366)                    // Red
    }

    val secondsRemaining = (gameState.timerRemainingMillis / 1000f).coerceAtLeast(0f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("footer_circle_button_container")
    ) {
        Box(
            modifier = Modifier.size(86.dp),
            contentAlignment = Alignment.Center
        ) {
            // Countdown Progress Arc Ring
            Canvas(modifier = Modifier.size(84.dp)) {
                // Background Track
                drawArc(
                    color = Color.White.copy(alpha = 0.12f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )

                if (settings.isAutoClickEnabled && !gameState.isGameOver) {
                    drawArc(
                        color = countdownColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progressFraction,
                        useCenter = false,
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // Main Interactive Circle Button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(buttonScale)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                RollButtonGradientStart,
                                RollButtonGradientEnd
                            )
                        )
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = !gameState.isGameOver && !gameState.isRollingAnimation
                    ) {
                        onRollClick()
                    }
                    .testTag("circle_roll_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Roll Random 1 to 50",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "ROLL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Subtext with Countdown & Pause / Play Control
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(top = 1.dp)
        ) {
            if (settings.isAutoClickEnabled && !gameState.isGameOver) {
                Text(
                    text = if (gameState.isTimerPaused) {
                        "Paused"
                    } else {
                        "Auto in ${String.format("%.1fs", secondsRemaining)}"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (gameState.isTimerPaused) GoldAccent else Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onTogglePause,
                    modifier = Modifier
                        .size(22.dp)
                        .testTag("toggle_auto_pause_button")
                ) {
                    Icon(
                        imageVector = if (gameState.isTimerPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (gameState.isTimerPaused) "Resume Auto Roll" else "Pause Auto Roll",
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else if (!settings.isAutoClickEnabled && !gameState.isGameOver) {
                Text(
                    text = "Manual Mode",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}
