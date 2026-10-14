package com.togglestudios.lumenpop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.togglestudios.lumenpop.data.ProgressRepository
import com.togglestudios.lumenpop.data.Screen
import com.togglestudios.lumenpop.engine.SoundManager
import com.togglestudios.lumenpop.engine.VibrationManager
import com.togglestudios.lumenpop.ui.components.TutorialCarousel
import com.togglestudios.lumenpop.ui.screens.GameScreen
import com.togglestudios.lumenpop.ui.screens.HomeScreen
import com.togglestudios.lumenpop.ui.screens.LoadingScreen
import com.togglestudios.lumenpop.ui.screens.SettingsScreen
import com.togglestudios.lumenpop.ui.screens.StartScreen
import com.togglestudios.lumenpop.ui.theme.LumenPopTheme

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager
    private lateinit var vibrationManager: VibrationManager
    private lateinit var progressRepository: ProgressRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        progressRepository = ProgressRepository(this)
        soundManager = SoundManager(this)
        vibrationManager = VibrationManager(this)

        setContent {
            var currentScreen by remember { mutableStateOf(Screen.LOADING) }
            var currentProgress by remember { mutableStateOf(progressRepository.loadProgress()) }
            var activeLevel by remember { mutableIntStateOf(currentProgress.highestUnlocked) }
            var showTutorial by remember { mutableStateOf(false) }

            // Sync settings
            soundManager.isMusicEnabled = currentProgress.music
            soundManager.isSoundEnabled = currentProgress.sound
            vibrationManager.isHapticsEnabled = currentProgress.haptics

            LumenPopTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        Screen.LOADING -> {
                            LoadingScreen(
                                onLoaded = {
                                    currentScreen = Screen.START
                                    soundManager.playMusic("homepage_music.mp3")
                                }
                            )
                        }

                        Screen.START -> {
                            StartScreen(
                                progress = currentProgress,
                                onPlayClick = {
                                    currentScreen = Screen.HOME
                                },
                                onSettingsClick = {
                                    currentScreen = Screen.SETTINGS
                                }
                            )
                        }

                        Screen.HOME -> {
                            HomeScreen(
                                progress = currentProgress,
                                onSelectLevel = { lvl ->
                                    activeLevel = lvl
                                    if (lvl == 1 && !currentProgress.tutorialSeen) {
                                        showTutorial = true
                                    } else {
                                        currentScreen = Screen.GAME
                                    }
                                },
                                onSettings = {
                                    currentScreen = Screen.SETTINGS
                                },
                                onSaveProgress = { updated ->
                                    currentProgress = updated
                                    progressRepository.saveProgress(updated)
                                },
                                onBack = {
                                    currentScreen = Screen.START
                                }
                            )
                        }

                        Screen.GAME -> {
                            GameScreen(
                                level = activeLevel,
                                progress = currentProgress,
                                soundManager = soundManager,
                                vibrationManager = vibrationManager,
                                onSaveProgress = { updated ->
                                    currentProgress = updated
                                    progressRepository.saveProgress(updated)
                                },
                                onNextLevel = { nextLvl ->
                                    activeLevel = nextLvl
                                },
                                onHome = {
                                    currentScreen = Screen.HOME
                                    soundManager.playMusic("homepage_music.mp3")
                                }
                            )
                        }

                        Screen.SETTINGS -> {
                            SettingsScreen(
                                progress = currentProgress,
                                onSaveProgress = { updated ->
                                    currentProgress = updated
                                    progressRepository.saveProgress(updated)
                                    soundManager.isMusicEnabled = updated.music
                                    soundManager.isSoundEnabled = updated.sound
                                    vibrationManager.isHapticsEnabled = updated.haptics
                                },
                                onResetProgress = {
                                    val reset = progressRepository.resetProgress()
                                    currentProgress = reset
                                    activeLevel = 1
                                },
                                onBack = {
                                    currentScreen = Screen.START
                                }
                            )
                        }

                        else -> {
                            currentScreen = Screen.START
                        }
                    }

                    if (showTutorial) {
                        TutorialCarousel(
                            onComplete = {
                                showTutorial = false
                                currentProgress.tutorialSeen = true
                                progressRepository.saveProgress(currentProgress)
                                currentScreen = Screen.GAME
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::soundManager.isInitialized && soundManager.isMusicEnabled) {
            soundManager.playMusic("homepage_music.mp3")
        }
    }

    override fun onPause() {
        super.onPause()
        if (::soundManager.isInitialized) {
            soundManager.stopMusic()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::soundManager.isInitialized) {
            soundManager.release()
        }
    }
}
