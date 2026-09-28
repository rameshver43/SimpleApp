package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundEffectsManager
import com.example.data.Player
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Battle Clash", appName)
  }

  @Test
  fun `sound effects manager plays sounds without error`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val soundManager = SoundEffectsManager(context)
    assertNotNull(soundManager)

    // Verify all sound functions execute safely
    soundManager.playButtonClick()
    soundManager.playRollTick()
    soundManager.playTurnChange(Player.PLAYER_1)
    soundManager.playTurnChange(Player.PLAYER_2)
    soundManager.playScoreAdd()
    soundManager.playVictoryFanfare()
    soundManager.playTieSound()

    soundManager.release()
  }

  @Test
  fun `game viewmodel initializes with sound enabled`() {
    val application = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = GameViewModel(application)
    assertNotNull(viewModel)
    assertTrue(viewModel.settings.value.soundEffectsEnabled)
    assertEquals(10, viewModel.settings.value.turnsPerPlayer)
    assertEquals(5, viewModel.settings.value.autoClickDelaySeconds)
  }
}
