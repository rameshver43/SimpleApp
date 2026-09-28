package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.GameSettings
import com.example.data.GameState
import com.example.data.Player
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Player1Color
import com.example.ui.theme.Player2Color
import kotlinx.coroutines.delay
import java.io.File
import kotlin.random.Random

private data class ConfettiParticle(
    val x: Float,
    val initialY: Float,
    val speed: Float,
    val size: Float,
    val rotationSpeed: Float,
    val color: Color
)

@Composable
fun WinnerPopupDialog(
    gameState: GameState,
    settings: GameSettings,
    onRematch: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!gameState.isGameOver) return

    val context = LocalContext.current
    val winner = gameState.winner
    val isTie = gameState.isTie
    val winnerColor = when (winner) {
        Player.PLAYER_1 -> Player1Color
        Player.PLAYER_2 -> Player2Color
        null -> GoldAccent
    }
    val winnerName = when (winner) {
        Player.PLAYER_1 -> settings.player1Name
        Player.PLAYER_2 -> settings.player2Name
        null -> "Both Players"
    }

    // Scale pop animation for dialog
    val trophyScale = remember { Animatable(0.2f) }
    LaunchedEffect(Unit) {
        trophyScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 300f)
        )
    }

    // Confetti animation progress
    val confettiAnim = rememberInfiniteTransition(label = "confetti")
    val confettiProgress by confettiAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confettiFall"
    )

    // Generate random confetti particles
    val particles = remember {
        val colors = listOf(
            GoldAccent,
            Player1Color,
            Player2Color,
            Color(0xFF10B981),
            Color(0xFFEC4899),
            Color(0xFF8B5CF6)
        )
        List(40) {
            ConfettiParticle(
                x = Random.nextFloat(),
                initialY = Random.nextFloat() * -0.5f,
                speed = 0.8f + Random.nextFloat() * 0.7f,
                size = 6f + Random.nextFloat() * 8f,
                rotationSpeed = Random.nextFloat() * 360f,
                color = colors[Random.nextInt(colors.size)]
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center
        ) {
            // Confetti Canvas Overlay
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                particles.forEach { p ->
                    val curY = ((p.initialY + (confettiProgress * p.speed)) % 1.2f) * canvasH
                    val curX = p.x * canvasW
                    val rot = confettiProgress * p.rotationSpeed

                    rotate(rot, pivot = Offset(curX, curY)) {
                        drawRect(
                            color = p.color,
                            topLeft = Offset(curX, curY),
                            size = Size(p.size.dp.toPx(), (p.size * 0.6f).dp.toPx())
                        )
                    }
                }
            }

            // Winner Card Container (Landscape-optimized)
            Card(
                modifier = Modifier
                    .scale(trophyScale.value)
                    .width(480.dp)
                    .padding(16.dp)
                    .shadow(24.dp, shape = RoundedCornerShape(24.dp), spotColor = winnerColor)
                    .testTag("winner_popup_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13162A)),
                border = BorderStroke(2.5.dp, winnerColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = winnerColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, winnerColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = "Trophy",
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTie) "STALEMATE TIE!" else "VICTORY CHAMPION!",
                                color = GoldAccent,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Winner Avatar Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (winner != null) {
                            val isP1 = winner == Player.PLAYER_1
                            val customPath = if (isP1) settings.player1CustomUri else settings.player2CustomUri
                            val defaultRes = if (isP1) R.drawable.char_player1_1790606554154 else R.drawable.char_player2_1790606571101

                            val imageModel: Any = remember(customPath) {
                                if (!customPath.isNullOrEmpty() && File(customPath).exists()) {
                                    File(customPath)
                                } else {
                                    defaultRes
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .border(3.dp, winnerColor, CircleShape)
                                    .shadow(12.dp, CircleShape, spotColor = winnerColor)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(imageModel)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "$winnerName Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                        }

                        Column {
                            Text(
                                text = if (isTie) "It's an Epic Tie!" else "$winnerName WINS!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Finished ${settings.turnsPerPlayer} turns each",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Final Scoreboard Comparison
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Player 1 Final
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = settings.player1Name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Player1Color
                                )
                                Text(
                                    text = "${gameState.player1Score}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (winner == Player.PLAYER_1) GoldAccent else Color.White
                                )
                                Text(
                                    text = "pts",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }

                            Text(
                                text = "VS",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White.copy(alpha = 0.4f)
                            )

                            // Player 2 Final
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = settings.player2Name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Player2Color
                                )
                                Text(
                                    text = "${gameState.player2Score}",
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (winner == Player.PLAYER_2) GoldAccent else Color.White
                                )
                                Text(
                                    text = "pts",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons: Play Again & Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("winner_settings_button"),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Settings",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onRematch,
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("winner_rematch_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = winnerColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Rematch",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PLAY AGAIN",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }
    }
}
