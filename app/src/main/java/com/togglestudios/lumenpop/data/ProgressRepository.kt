package com.togglestudios.lumenpop.data

import android.content.Context
import android.content.SharedPreferences

class ProgressRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lumen-pop-progress", Context.MODE_PRIVATE)

    fun loadProgress(): SavedProgress {
        val highest = prefs.getInt("highestUnlocked", 1)
        val coins = prefs.getInt("coins", 1000)
        val sound = prefs.getBoolean("sound", true)
        val music = prefs.getBoolean("music", true)
        val haptics = prefs.getBoolean("haptics", true)
        val highContrast = prefs.getBoolean("highContrast", false)
        val username = prefs.getString("username", "Player") ?: "Player"
        val tutorialSeen = prefs.getBoolean("tutorialSeen", false)
        val dailyGiftClaimedOn = prefs.getString("dailyGiftClaimedOn", "") ?: ""

        val completedMap = mutableMapOf<Int, LevelStats>()
        for (i in 1..100) {
            if (prefs.contains("level_${i}_stars")) {
                val stars = prefs.getInt("level_${i}_stars", 0)
                val bestScore = prefs.getInt("level_${i}_bestScore", 0)
                completedMap[i] = LevelStats(stars, bestScore)
            }
        }

        return SavedProgress(
            highestUnlocked = highest,
            completed = completedMap,
            coins = coins,
            sound = sound,
            music = music,
            haptics = haptics,
            highContrast = highContrast,
            username = username,
            tutorialSeen = tutorialSeen,
            dailyGiftClaimedOn = dailyGiftClaimedOn
        )
    }

    fun saveProgress(progress: SavedProgress) {
        val editor = prefs.edit()
        editor.putInt("highestUnlocked", progress.highestUnlocked)
        editor.putInt("coins", progress.coins)
        editor.putBoolean("sound", progress.sound)
        editor.putBoolean("music", progress.music)
        editor.putBoolean("haptics", progress.haptics)
        editor.putBoolean("highContrast", progress.highContrast)
        editor.putString("username", progress.username)
        editor.putBoolean("tutorialSeen", progress.tutorialSeen)
        editor.putString("dailyGiftClaimedOn", progress.dailyGiftClaimedOn)

        progress.completed.forEach { (level, stats) ->
            editor.putInt("level_${level}_stars", stats.stars)
            editor.putInt("level_${level}_bestScore", stats.bestScore)
        }
        editor.apply()
    }

    fun resetProgress(): SavedProgress {
        prefs.edit().clear().apply()
        return SavedProgress()
    }
}
