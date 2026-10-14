package com.togglestudios.lumenpop

import com.togglestudios.lumenpop.data.LevelStats
import com.togglestudios.lumenpop.data.SavedProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.max

class SupabaseAuthValidationTest {

    private fun validateUsername(username: String): Boolean {
        val clean = username.trim().lowercase()
        if (clean.length < 3 || clean.length > 12) return false
        return clean.matches(Regex("^[a-z0-9_-]+$"))
    }

    private fun validatePassword(password: String): Boolean {
        return password.length in 8..128
    }

    private fun mergeProgress(local: SavedProgress, cloud: SavedProgress): SavedProgress {
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

        return local.copy(
            highestUnlocked = mergedHighest,
            coins = mergedCoins,
            completed = mergedCompleted,
            tutorialSeen = local.tutorialSeen || cloud.tutorialSeen
        )
    }

    @Test
    fun testUsernameConstraints() {
        // Valid usernames (3-12 lowercase letters, digits, _, -)
        assertTrue(validateUsername("lumen_star"))
        assertTrue(validateUsername("nova-7"))
        assertTrue(validateUsername("star_player"))
        assertTrue(validateUsername("abc"))
        assertTrue(validateUsername("player123456"))

        // Invalid usernames
        assertFalse(validateUsername("ab")) // Too short
        assertFalse(validateUsername("thisusernameiswaytoolong")) // Too long (>12)
        assertFalse(validateUsername("User Name")) // Has spaces
        assertFalse(validateUsername("player@123")) // Has invalid character @
        assertFalse(validateUsername("stargazer!")) // Has exclamation
    }

    @Test
    fun testPasswordConstraints() {
        assertTrue(validatePassword("SecurePass123!"))
        assertTrue(validatePassword("12345678"))
        assertFalse(validatePassword("short")) // < 8 characters
        assertFalse(validatePassword("")) // empty
    }

    @Test
    fun testCloudMergeNeverOverwritesValidProgress() {
        // Local user has played up to level 5 with 1500 coins
        val local = SavedProgress(
            highestUnlocked = 5,
            coins = 1500,
            completed = mutableMapOf(
                1 to LevelStats(3, 1200),
                2 to LevelStats(2, 900)
            ),
            tutorialSeen = true
        )

        // Cloud has level 3 with 2000 coins and higher score on level 2
        val cloud = SavedProgress(
            highestUnlocked = 3,
            coins = 2000,
            completed = mutableMapOf(
                1 to LevelStats(2, 800),
                2 to LevelStats(3, 1500),
                3 to LevelStats(1, 750)
            ),
            tutorialSeen = false
        )

        val merged = mergeProgress(local, cloud)

        // Should retain highest level (5)
        assertEquals(5, merged.highestUnlocked)
        // Should retain highest coins (2000)
        assertEquals(2000, merged.coins)
        // Level 1 should retain max stars (3) and max score (1200)
        assertEquals(3, merged.completed[1]?.stars)
        assertEquals(1200, merged.completed[1]?.bestScore)
        // Level 2 should take cloud's better stars (3) and score (1500)
        assertEquals(3, merged.completed[2]?.stars)
        assertEquals(1500, merged.completed[2]?.bestScore)
        // Level 3 should be merged from cloud
        assertEquals(1, merged.completed[3]?.stars)
        // Tutorial seen should remain true
        assertTrue(merged.tutorialSeen)
    }
}
