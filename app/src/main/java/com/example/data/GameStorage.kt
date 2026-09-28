package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

class GameStorage(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("battle_clash_prefs", Context.MODE_PRIVATE)

    fun loadSettings(): GameSettings {
        val turns = prefs.getInt("turns_per_player", 10)
        val delay = prefs.getInt("auto_delay_sec", 5)
        val autoEnabled = prefs.getBoolean("auto_enabled", true)
        val soundEffects = prefs.getBoolean("sound_effects_enabled", true)
        val soundHaptics = prefs.getBoolean("sound_haptics", true)
        val p1Name = prefs.getString("p1_name", "Hero Crimson") ?: "Hero Crimson"
        val p2Name = prefs.getString("p2_name", "Cyber Frost") ?: "Cyber Frost"
        val p1Uri = prefs.getString("p1_avatar_path", null)
        val p2Uri = prefs.getString("p2_avatar_path", null)

        return GameSettings(
            turnsPerPlayer = turns,
            autoClickDelaySeconds = delay,
            isAutoClickEnabled = autoEnabled,
            soundEffectsEnabled = soundEffects,
            soundHapticsEnabled = soundHaptics,
            player1Name = p1Name,
            player2Name = p2Name,
            player1CustomUri = p1Uri,
            player2CustomUri = p2Uri
        )
    }

    fun saveSettings(settings: GameSettings) {
        prefs.edit()
            .putInt("turns_per_player", settings.turnsPerPlayer)
            .putInt("auto_delay_sec", settings.autoClickDelaySeconds)
            .putBoolean("auto_enabled", settings.isAutoClickEnabled)
            .putBoolean("sound_effects_enabled", settings.soundEffectsEnabled)
            .putBoolean("sound_haptics", settings.soundHapticsEnabled)
            .putString("p1_name", settings.player1Name)
            .putString("p2_name", settings.player2Name)
            .putString("p1_avatar_path", settings.player1CustomUri)
            .putString("p2_avatar_path", settings.player2CustomUri)
            .apply()
    }

    /**
     * Copies image content from user's gallery picker Uri into local app internal storage
     * so that it persists forever without URI permission issues.
     */
    fun saveAvatarFromUri(player: Player, uri: Uri): String? {
        return try {
            val fileName = "avatar_${player.name.lowercase()}.jpg"
            val destFile = File(context.filesDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearCustomAvatar(player: Player) {
        try {
            val fileName = "avatar_${player.name.lowercase()}.jpg"
            val destFile = File(context.filesDir, fileName)
            if (destFile.exists()) {
                destFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
