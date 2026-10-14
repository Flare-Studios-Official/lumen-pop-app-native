package com.togglestudios.lumenpop.engine

import com.togglestudios.lumenpop.data.LumenColor
import com.togglestudios.lumenpop.data.SpecialType
import com.togglestudios.lumenpop.data.Tile
import kotlin.random.Random

class GameEngine {
    companion object {
        const val BOARD_SIZE = 7
        const val TOTAL_TILES = BOARD_SIZE * BOARD_SIZE

        val LINE_DIRECTIONS = listOf(
            Pair(-1, -1), Pair(-1, 0), Pair(-1, 1),
            Pair(0, -1), Pair(0, 1),
            Pair(1, -1), Pair(1, 0), Pair(1, 1)
        )

        fun rowOf(index: Int): Int = index / BOARD_SIZE
        fun colOf(index: Int): Int = index % BOARD_SIZE

        fun directionBetween(from: Int, to: Int): Pair<Int, Int>? {
            val dRow = rowOf(to) - rowOf(from)
            val dCol = colOf(to) - colOf(from)
            if (kotlin.math.abs(dRow) > 1 || kotlin.math.abs(dCol) > 1 || (dRow == 0 && dCol == 0)) {
                return null
            }
            return Pair(dRow.coerceIn(-1, 1), dCol.coerceIn(-1, 1))
        }

        fun areAdjacent(a: Int, b: Int): Boolean {
            val dRow = kotlin.math.abs(rowOf(a) - rowOf(b))
            val dCol = kotlin.math.abs(colOf(a) - colOf(b))
            return dRow <= 1 && dCol <= 1 && !(dRow == 0 && dCol == 0)
        }
    }

    private var nextTileId = 1000L
    private fun freshId(): Long = nextTileId++

    fun createBoard(level: Int = 1): MutableList<Tile> {
        val board = MutableList(TOTAL_TILES) {
            Tile(id = freshId(), color = LumenColor.SOLAR)
        }
        val colorBag = mutableListOf<LumenColor>()
        val colors = LumenColor.entries.toTypedArray()

        for (i in 0 until TOTAL_TILES) {
            if (colorBag.isEmpty()) {
                val len = if (Random.nextFloat() < 0.85f) 3 else 4
                val randomColor = colors.random()
                repeat(len) { colorBag.add(randomColor) }
            }
            board[i] = Tile(id = freshId(), color = colorBag.removeAt(0))
        }

        var attempts = 0
        while (countPlayableChains(board) < 3 && attempts < 10) {
            attempts++
            installGuaranteedLine(board, colors.random())
        }

        // Guarantee early 4-link opportunity on intro levels
        if (level <= 2) {
            val beamColor = colors.random()
            val row = Random.nextInt(BOARD_SIZE)
            val colStart = Random.nextInt(BOARD_SIZE - 3)
            for (i in 0 until 4) {
                board[row * BOARD_SIZE + colStart + i] = Tile(id = freshId(), color = beamColor)
            }
        }

        return board
    }

    fun hasPlayableChain(board: List<Tile?>): Boolean {
        for (start in 0 until TOTAL_TILES) {
            val tile = board[start] ?: continue
            for (dir in LINE_DIRECTIONS) {
                var length = 1
                var r = rowOf(start) + dir.first
                var c = colOf(start) + dir.second
                while (r in 0 until BOARD_SIZE && c in 0 until BOARD_SIZE) {
                    val nextIdx = r * BOARD_SIZE + c
                    if (board[nextIdx]?.color != tile.color) break
                    length++
                    if (length >= 3) return true
                    r += dir.first
                    c += dir.second
                }
            }
        }
        return false
    }

    fun countPlayableChains(board: List<Tile?>): Int {
        var count = 0
        for (start in 0 until TOTAL_TILES) {
            val tile = board[start] ?: continue
            for (dir in LINE_DIRECTIONS) {
                var length = 1
                var r = rowOf(start) + dir.first
                var c = colOf(start) + dir.second
                while (r in 0 until BOARD_SIZE && c in 0 until BOARD_SIZE) {
                    val nextIdx = r * BOARD_SIZE + c
                    if (board[nextIdx]?.color != tile.color) break
                    length++
                    if (length == 3) {
                        count++
                        break
                    }
                    r += dir.first
                    c += dir.second
                }
            }
        }
        return count
    }

    fun installGuaranteedLine(board: MutableList<Tile>, color: LumenColor) {
        val isRow = Random.nextBoolean()
        if (isRow) {
            val row = Random.nextInt(BOARD_SIZE)
            val colStart = Random.nextInt(BOARD_SIZE - 2)
            for (i in 0 until 3) {
                val idx = row * BOARD_SIZE + colStart + i
                if (!board[idx].isFusion) board[idx].color = color
            }
        } else {
            val col = Random.nextInt(BOARD_SIZE)
            val rowStart = Random.nextInt(BOARD_SIZE - 2)
            for (i in 0 until 3) {
                val idx = (rowStart + i) * BOARD_SIZE + col
                if (!board[idx].isFusion) board[idx].color = color
            }
        }
    }

    /**
     * Resolves matching tiles to remove, including special beam and nova explosions.
     */
    fun calculateExplodedIndices(selectedIndices: List<Int>, board: List<Tile>): Set<Int> {
        val result = mutableSetOf<Int>()
        result.addAll(selectedIndices)

        // Process special tiles in the chain
        selectedIndices.forEach { idx ->
            val tile = board.getOrNull(idx) ?: return@forEach
            when (tile.special) {
                SpecialType.BEAM_H -> {
                    val r = rowOf(idx)
                    for (c in 0 until BOARD_SIZE) result.add(r * BOARD_SIZE + c)
                }
                SpecialType.BEAM_V -> {
                    val c = colOf(idx)
                    for (r in 0 until BOARD_SIZE) result.add(r * BOARD_SIZE + c)
                }
                SpecialType.NOVA -> {
                    val r = rowOf(idx)
                    val c = colOf(idx)
                    for (dr in -1..1) {
                        for (dc in -1..1) {
                            val nr = r + dr
                            val nc = c + dc
                            if (nr in 0 until BOARD_SIZE && nc in 0 until BOARD_SIZE) {
                                result.add(nr * BOARD_SIZE + nc)
                            }
                        }
                    }
                }
                SpecialType.CROSS -> {
                    val r = rowOf(idx)
                    val c = colOf(idx)
                    for (i in 0 until BOARD_SIZE) {
                        result.add(r * BOARD_SIZE + i)
                        result.add(i * BOARD_SIZE + c)
                    }
                }
                SpecialType.NONE -> Unit
            }
        }
        return result
    }

    /**
     * Collapses board downwards with gravity and refills new random tiles from the top.
     */
    fun collapseAndRefill(
        currentBoard: List<Tile>,
        explodedIndices: Set<Int>,
        level: Int = 1
    ): Pair<MutableList<Tile>, List<Int>> {
        val working: MutableList<Tile?> = currentBoard.toMutableList()
        explodedIndices.forEach { idx ->
            working[idx] = null
        }

        // If chain was 4 or 5+, spawn a special tile at the last linked position
        // This is handled by caller before or after refill
        val nextBoard: MutableList<Tile?> = MutableList(TOTAL_TILES) { null }
        val refilled = mutableListOf<Int>()
        val colorBag = mutableListOf<LumenColor>()
        val colors = LumenColor.entries.toTypedArray()
        var vortexSpawned = false

        for (col in 0 until BOARD_SIZE) {
            val survivors = mutableListOf<Tile>()
            for (row in (BOARD_SIZE - 1) downTo 0) {
                val tile = working[row * BOARD_SIZE + col]
                if (tile != null) survivors.add(tile)
            }

            var survivorIdx = 0
            for (row in (BOARD_SIZE - 1) downTo 0) {
                if (survivorIdx < survivors.size) {
                    nextBoard[row * BOARD_SIZE + col] = survivors[survivorIdx]
                    survivorIdx++
                } else {
                    if (colorBag.isEmpty()) {
                        val len = if (Random.nextFloat() < 0.85f) 3 else 4
                        val color = colors.random()
                        repeat(len) { colorBag.add(color) }
                    }
                    val isFusion = !vortexSpawned && Random.nextFloat() < 0.03f
                    val newTile = if (isFusion) {
                        vortexSpawned = true
                        Tile(id = freshId(), color = LumenColor.COSMIC, isFusion = true)
                    } else {
                        Tile(id = freshId(), color = colorBag.removeAt(0))
                    }
                    nextBoard[row * BOARD_SIZE + col] = newTile
                    refilled.add(row * BOARD_SIZE + col)
                }
            }
        }

        val finalBoard = nextBoard.filterNotNull().toMutableList()
        if (!hasPlayableChain(finalBoard)) {
            installGuaranteedLine(finalBoard, colors.random())
        }

        return Pair(finalBoard, refilled)
    }

    /**
     * Executes Shuffle Booster.
     */
    fun shuffleBoard(board: MutableList<Tile>): MutableList<Tile> {
        val colors = board.map { it.color }.shuffled()
        for (i in board.indices) {
            board[i] = board[i].copy(color = colors[i])
        }
        var attempts = 0
        while (countPlayableChains(board) < 3 && attempts < 10) {
            attempts++
            installGuaranteedLine(board, LumenColor.entries.toTypedArray().random())
        }
        return board
    }

    /**
     * Executes Bomb Booster: clears 3x3 at target cell.
     */
    fun executeBombBooster(board: List<Tile>, centerIndex: Int = (BOARD_SIZE / 2) * BOARD_SIZE + (BOARD_SIZE / 2)): Pair<MutableList<Tile>, Int> {
        val centerRow = rowOf(centerIndex)
        val centerCol = colOf(centerIndex)
        val cleared = mutableSetOf<Int>()
        for (dr in -1..1) {
            for (dc in -1..1) {
                val r = centerRow + dr
                val c = centerCol + dc
                if (r in 0 until BOARD_SIZE && c in 0 until BOARD_SIZE) {
                    cleared.add(r * BOARD_SIZE + c)
                }
            }
        }
        val (newBoard, _) = collapseAndRefill(board, cleared)
        return Pair(newBoard, cleared.size * 50)
    }

    /**
     * Executes Burst Wave Booster: clears all tiles within Euclidean radius <= 2.
     */
    fun executeBurstBooster(board: List<Tile>, centerIndex: Int = (BOARD_SIZE / 2) * BOARD_SIZE + (BOARD_SIZE / 2)): Pair<MutableList<Tile>, Int> {
        val centerRow = rowOf(centerIndex)
        val centerCol = colOf(centerIndex)
        val cleared = mutableSetOf<Int>()
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                val dist = kotlin.math.hypot((r - centerRow).toDouble(), (c - centerCol).toDouble())
                if (dist <= 2.0) {
                    cleared.add(r * BOARD_SIZE + c)
                }
            }
        }
        val (newBoard, _) = collapseAndRefill(board, cleared)
        return Pair(newBoard, cleared.size * 60)
    }
}
