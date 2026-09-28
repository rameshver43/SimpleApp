package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.FloatingScore
import com.example.data.Player
import com.example.ui.theme.Player1Accent
import com.example.ui.theme.Player1Color
import com.example.ui.theme.Player1Glow
import com.example.ui.theme.Player2Accent
import com.example.ui.theme.Player2Color
import com.example.ui.theme.Player2Glow
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun PlayerCard(
    player: Player,
    name: String,
    score: Int,
    turnsCompleted: Int,
    maxTurns: Int,
    isCurrentTurn: Boolean,
    customAvatarPath: String?,
    floatingScores: List<FloatingScore>,
    onAvatarSelected: (android.net.Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onAvatarSelected(uri)
            }
        }
    )

    val isP1 = player == Player.PLAYER_1
    val playerThemeColor = if (isP1) Player1Color else Player2Color
    val playerAccentColor = if (isP1) Player1Accent else Player2Accent
    val playerGlowColor = if (isP1) Player1Glow else Player2Glow
    val defaultDrawableRes = if (isP1) R.drawable.char_player1_1790606554154 else R.drawable.char_player2_1790606571101

    // Animated score transition
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "scoreAnimation"
    )

    // Pulsing active turn ring
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTurn")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val cardBorderColor by animateColorAsState(
        targetValue = if (isCurrentTurn) playerThemeColor else playerThemeColor.copy(alpha = 0.25f),
        animationSpec = tween(300),
        label = "borderGlow"
    )

    val cardScale by animateFloatAsState(
        targetValue = if (isCurrentTurn) 1.03f else 1.0f,
        animationSpec = tween(300),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .scale(cardScale)
            .testTag(if (isP1) "player_1_card" else "player_2_card")
            .shadow(
                elevation = if (isCurrentTurn) 16.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isCurrentTurn) playerGlowColor else Color.Black
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTurn) {
                Color(if (isP1) 0xFF2A1424 else 0xFF12243C)
            } else {
                Color(if (isP1) 0xFF1C131D else 0xFF101B2A)
            }
        ),
        border = BorderStroke(
            width = if (isCurrentTurn) 2.5.dp else 1.dp,
            color = cardBorderColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row: Name & Active Turn Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = playerThemeColor,
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                    }

                    if (isCurrentTurn) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = playerThemeColor,
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlashOn,
                                    contentDescription = "Active Turn",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "TURN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "WAITING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Avatar Box with Tap to Upload from Gallery
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(86.dp)
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag(if (isP1) "player_1_avatar" else "player_2_avatar")
                ) {
                    // Pulsing Outer Halo if active turn
                    if (isCurrentTurn) {
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .scale(1.0f + (pulseGlow * 0.08f))
                                .border(
                                    width = 3.dp,
                                    brush = Brush.radialGradient(
                                        colors = listOf(playerGlowColor, playerAccentColor, Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }

                    // Avatar Image
                    val imageModel: Any = remember(customAvatarPath) {
                        if (!customAvatarPath.isNullOrEmpty() && File(customAvatarPath).exists()) {
                            File(customAvatarPath)
                        } else {
                            defaultDrawableRes
                        }
                    }

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageModel)
                            .crossfade(true)
                            .build(),
                        contentDescription = "$name Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(
                                width = 2.dp,
                                color = if (isCurrentTurn) playerThemeColor else Color.White.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    )

                    // Edit / Upload Icon Overlay on Bottom Right of Avatar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 2.dp, y = 2.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(playerAccentColor)
                            .border(1.5.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Change photo from gallery",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Counter & Dynamic Total Score Card Below Image
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(if (isP1) "player_1_score_counter" else "player_2_score_counter"),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Black.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, playerThemeColor.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "TOTAL SCORE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = playerAccentColor
                        )
                        Text(
                            text = "$animatedScore",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        // Turns count progress
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Turns: $turnsCompleted/$maxTurns",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                            val progress = if (maxTurns > 0) turnsCompleted.toFloat() / maxTurns.toFloat() else 0f
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .width(55.dp)
                                    .height(4.dp)
                                    .clip(CircleShape),
                                color = playerThemeColor,
                                trackColor = Color.White.copy(alpha = 0.15f),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            // Floating Score Particle Effect (`+XX`) on Score Addition
            floatingScores.filter { it.player == player }.forEach { floatScore ->
                FloatingScorePopup(points = floatScore.points, color = playerThemeColor)
            }
        }
    }
}

@Composable
private fun FloatingScorePopup(points: Int, color: Color) {
    var visible by remember { mutableStateOf(true) }
    val offsetY by animateFloatAsState(
        targetValue = if (visible) -45f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "floatingY"
    )

    LaunchedEffect(Unit) {
        delay(700)
        visible = false
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)) + slideInVertically { it / 2 },
        exit = fadeOut(tween(300)) + slideOutVertically { -it / 2 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.toInt()) },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color,
                shadowElevation = 8.dp
            ) {
                Text(
                    text = "+$points",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

