package com.togglestudios.lumenpop

import com.togglestudios.lumenpop.data.BoosterKind
import com.togglestudios.lumenpop.data.LevelConfig
import com.togglestudios.lumenpop.data.LumenColor
import com.togglestudios.lumenpop.data.Tile
import com.togglestudios.lumenpop.engine.GameEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    private val engine = GameEngine()

    @Test
    fun testBoardCreation() {
        val board = engine.createBoard(1)
        assertEquals(49, board.size)
        // Verify every tile is populated
        board.forEach { tile ->
            assertNotNull(tile)
            assertNotNull(tile.color)
        }
    }

    @Test
    fun testAdjacencyRules() {
        // (row 1, col 1) -> index 8
        // Adjacent indices: 0, 1, 2, 7, 9, 14, 15, 16
        val center = 8
        assertTrue(GameEngine.areAdjacent(center, 0))
        assertTrue(GameEngine.areAdjacent(center, 1))
        assertTrue(GameEngine.areAdjacent(center, 2))
        assertTrue(GameEngine.areAdjacent(center, 7))
        assertTrue(GameEngine.areAdjacent(center, 9))
        assertTrue(GameEngine.areAdjacent(center, 14))
        assertTrue(GameEngine.areAdjacent(center, 15))
        assertTrue(GameEngine.areAdjacent(center, 16))

        // Non-adjacent indices
        assertFalse(GameEngine.areAdjacent(center, 3))
        assertFalse(GameEngine.areAdjacent(center, 21))
        assertFalse(GameEngine.areAdjacent(center, 48))
    }

    @Test
    fun testDirectionCalculation() {
        // Center: 16 (row 2, col 2)
        val center = 16
        assertEquals(Pair(0, 1), GameEngine.directionBetween(center, 17)) // Right
        assertEquals(Pair(0, -1), GameEngine.directionBetween(center, 15)) // Left
        assertEquals(Pair(1, 0), GameEngine.directionBetween(center, 23)) // Down
        assertEquals(Pair(-1, 0), GameEngine.directionBetween(center, 9)) // Up
        assertEquals(Pair(1, 1), GameEngine.directionBetween(center, 24)) // Down-Right
        assertEquals(Pair(-1, -1), GameEngine.directionBetween(center, 8)) // Up-Left

        // Non-adjacent returns null
        assertNull(GameEngine.directionBetween(center, 48))
    }

    @Test
    fun testBombBoosterTargeting() {
        val board = engine.createBoard(1)
        val centerIndex = 24 // Row 3, Col 3 (exact center of 7x7)
        val (newBoard, score) = engine.executeBombBooster(board, centerIndex)
        assertEquals(49, newBoard.size)
        // 9 tiles affected in 3x3 pocket: 9 * 50 = 450
        assertEquals(450, score)
    }

    @Test
    fun testBurstBoosterTargeting() {
        val board = engine.createBoard(1)
        val centerIndex = 24 // Row 3, Col 3
        val (newBoard, score) = engine.executeBurstBooster(board, centerIndex)
        assertEquals(49, newBoard.size)
        // Distance <= 2.0 covers 13 tiles: 13 * 60 = 780
        assertEquals(780, score)
    }

    @Test
    fun testLevelConfigProgression() {
        val lvl1 = LevelConfig.forLevel(1)
        assertEquals(1, lvl1.level)
        assertEquals(700, lvl1.targetScore)
        assertEquals(20, lvl1.moves)
        assertEquals("Starlight Meadows", lvl1.world)

        val lvl10 = LevelConfig.forLevel(10)
        assertEquals(10, lvl10.level)
        assertEquals(700 + 9 * 180, lvl10.targetScore)
        assertEquals(24, lvl10.moves)

        val lvl30 = LevelConfig.forLevel(30)
        assertEquals(30, lvl30.level)
        assertEquals("Twilight Grove", lvl30.world)
    }

    @Test
    fun testScoringRules() {
        // Chains < 3 give 0 points
        assertEquals(0, GameEngine.calculateScore(0))
        assertEquals(0, GameEngine.calculateScore(1))
        assertEquals(0, GameEngine.calculateScore(2))

        // Web native exact scoring
        assertEquals(30, GameEngine.calculateScore(3))
        assertEquals(55, GameEngine.calculateScore(4))
        assertEquals(85, GameEngine.calculateScore(5))
        assertEquals(120, GameEngine.calculateScore(6))
        assertEquals(155, GameEngine.calculateScore(7)) // 120 + 1 * 35
        assertEquals(190, GameEngine.calculateScore(8)) // 120 + 2 * 35
        assertEquals(225, GameEngine.calculateScore(9)) // 120 + 3 * 35

        // Cascaded / extra cleared tiles (+10 pts each)
        // 4-link chain that clears 7 extra tiles via Beam: 55 + 7 * 10 = 125
        assertEquals(125, GameEngine.calculateScore(4, 11))
        // 5-link chain that clears 8 extra tiles via Nova: 85 + 8 * 10 = 165
        assertEquals(165, GameEngine.calculateScore(5, 13))
    }
}
