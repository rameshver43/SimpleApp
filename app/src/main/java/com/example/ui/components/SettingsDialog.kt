package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.GameSettings
import com.example.data.Player
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Player1Color
import com.example.ui.theme.Player2Color

@Composable
fun SettingsDialog(
    currentSettings: GameSettings,
    onSaveSettings: (GameSettings) -> Unit,
    onResetAvatars: () -> Unit,
    onDismiss: () -> Unit
) {
    var turnsCount by remember { mutableIntStateOf(currentSettings.turnsPerPlayer) }
    var delaySec by remember { mutableIntStateOf(currentSettings.autoClickDelaySeconds) }
    var autoEnabled by remember { mutableStateOf(currentSettings.isAutoClickEnabled) }
    var soundEnabled by remember { mutableStateOf(currentSettings.soundEffectsEnabled) }
    var hapticsEnabled by remember { mutableStateOf(currentSettings.soundHapticsEnabled) }
    var p1Name by remember { mutableStateOf(currentSettings.player1Name) }
    var p2Name by remember { mutableStateOf(currentSettings.player2Name) }

    val turnOptions = listOf(3, 5, 10, 15, 20)
    val delayOptions = listOf(2, 3, 5, 8, 10)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(520.dp)
                .height(340.dp)
                .padding(12.dp)
                .testTag("settings_dialog_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF14172B)),
            border = BorderStroke(1.5.dp, Color(0xFF2C3258))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Game Settings",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Game Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Setting 1: Number of Turns per Player
                Text(
                    text = "Number of Turns for Each Player: $turnsCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    turnOptions.forEach { count ->
                        FilterChip(
                            selected = turnsCount == count,
                            onClick = { turnsCount = count },
                            label = { Text("$count turns", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldAccent,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1E2242),
                                labelColor = Color.White
                            ),
                            modifier = Modifier.testTag("turn_chip_$count")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Setting 2: Time Delay for Automatic Button Clicks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Auto-Click Time Delay: ${delaySec}s",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (autoEnabled) "Auto-Roll ON" else "Auto-Roll OFF",
                            fontSize = 11.sp,
                            color = if (autoEnabled) Color(0xFF10B981) else Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = autoEnabled,
                            onCheckedChange = { autoEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF10B981)
                            ),
                            modifier = Modifier.testTag("toggle_auto_enabled")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    delayOptions.forEach { sec ->
                        FilterChip(
                            selected = delaySec == sec,
                            onClick = { delaySec = sec },
                            label = { Text("${sec}s delay", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Player2Color,
                                selectedLabelColor = Color.Black,
                                containerColor = Color(0xFF1E2242),
                                labelColor = Color.White
                            ),
                            modifier = Modifier.testTag("delay_chip_$sec")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Setting 3: Player Names
                Text(
                    text = "Player Names",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = p1Name,
                        onValueChange = { p1Name = it },
                        label = { Text("Player 1", fontSize = 10.sp, color = Player1Color) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("player_1_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Player1Color,
                            unfocusedBorderColor = Player1Color.copy(alpha = 0.5f)
                        )
                    )

                    OutlinedTextField(
                        value = p2Name,
                        onValueChange = { p2Name = it },
                        label = { Text("Player 2", fontSize = 10.sp, color = Player2Color) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("player_2_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Player2Color,
                            unfocusedBorderColor = Player2Color.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Setting 4: Sound Effects & Haptics Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sound Effects Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("sound_effects_toggle_row")
                    ) {
                        Icon(
                            imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound Effects",
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sound FX",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = { soundEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GoldAccent
                            ),
                            modifier = Modifier.testTag("toggle_sound_effects")
                        )
                    }

                    // Haptics Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("haptics_toggle_row")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Haptics",
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Vibrate",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = { hapticsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = GoldAccent
                            ),
                            modifier = Modifier.testTag("toggle_haptics")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Setting 5: Reset Default Avatars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onResetAvatars,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("reset_avatars_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Avatars",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reset Character Avatars to Default",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Save Button
                Button(
                    onClick = {
                        val newSettings = currentSettings.copy(
                            turnsPerPlayer = turnsCount,
                            autoClickDelaySeconds = delaySec,
                            isAutoClickEnabled = autoEnabled,
                            soundEffectsEnabled = soundEnabled,
                            soundHapticsEnabled = hapticsEnabled,
                            player1Name = p1Name.ifBlank { "Player 1" },
                            player2Name = p2Name.ifBlank { "Player 2" }
                        )
                        onSaveSettings(newSettings)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("save_settings_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Icon(
                        imageVector = Icons.Default.Done,
                        contentDescription = "Apply",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "APPLY & SAVE",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
