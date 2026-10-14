package com.togglestudios.lumenpop.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.max

class ProgressRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("lumen-pop-progress", Context.MODE_PRIVATE)

    val supabaseManager: SupabaseManager = SupabaseManager(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    fun loadProgress(): SavedProgress {
        val highest = prefs.getInt("highestUnlocked", 1)
        val coins = prefs.getInt("coins", 1000)
        val sound = prefs.getBoolean("sound", true)
        val music = prefs.getBoolean("music", true)
        val haptics = prefs.getBoolean("haptics", true)
        val highContrast = prefs.getBoolean("highContrast", false)
        val username = prefs.getString("username", supabaseManager.getActiveUsername()) ?: supabaseManager.getActiveUsername()
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

    fun saveProgress(progress: SavedProgress, syncToCloud: Boolean = true) {
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

        if (syncToCloud && supabaseManager.isLoggedIn()) {
            val uid = supabaseManager.currentSession?.userId
            if (!uid.isNullOrEmpty()) {
                scope.launch {
                    supabaseManager.saveCloudProgress(uid, progress)
                }
            }
        }
    }

    suspend fun syncWithCloud(): SavedProgress {
        val local = loadProgress()
        val session = supabaseManager.currentSession ?: return local

        val cloudRes = supabaseManager.loadCloudProgress(session.userId)
        val cloud = cloudRes.getOrNull() ?: return local

        // Safe Merge: Never discard progress
        val mergedHighest = max(local.highestUnlocked, cloud.highestUnlocked)
        val mergedCoins = max(local.coins, cloud.coins)
        val mergedCompleted = HashMap(local.completed)

        cloud.completed.forEach { (lvl, cStats) ->
            val lStats = mergedCompleted[lvl]
            if (lStats == null) {
                mergedCompleted[lvl] = cStats
            } else {
                mergedCompleted[lvl] = LevelStats(
                    stars = max(lStats.stars, cStats.stars),
                    bestScore = max(lStats.bestScore, cStats.bestScore)
                )
            }
        }

        val mergedProgress = local.copy(
            highestUnlocked = mergedHighest,
            coins = mergedCoins,
            completed = mergedCompleted,
            username = session.username,
            tutorialSeen = local.tutorialSeen || cloud.tutorialSeen,
            dailyGiftClaimedOn = if (local.dailyGiftClaimedOn.isNotEmpty()) local.dailyGiftClaimedOn else cloud.dailyGiftClaimedOn
        )

        saveProgress(mergedProgress, syncToCloud = true)
        return mergedProgress
    }

    fun resetProgress(): SavedProgress {
        prefs.edit().clear().apply()
        return SavedProgress(username = supabaseManager.getActiveUsername())
    }
}
