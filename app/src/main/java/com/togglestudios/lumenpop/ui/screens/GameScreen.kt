package com.togglestudios.lumenpop.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.togglestudios.lumenpop.data.BoosterKind
import com.togglestudios.lumenpop.data.LevelConfig
import com.togglestudios.lumenpop.data.LevelStats
import com.togglestudios.lumenpop.data.SavedProgress
import com.togglestudios.lumenpop.data.SpecialType
import com.togglestudios.lumenpop.data.Tile
import com.togglestudios.lumenpop.engine.GameEngine
import com.togglestudios.lumenpop.engine.SoundManager
import com.togglestudios.lumenpop.engine.VibrationManager
import com.togglestudios.lumenpop.ui.components.BoosterBar
import com.togglestudios.lumenpop.ui.components.GameOverDialog
import com.togglestudios.lumenpop.ui.components.LumenTileView
import com.togglestudios.lumenpop.ui.components.PauseDialog
import com.togglestudios.lumenpop.ui.components.ScoreBoardView
import com.togglestudios.lumenpop.ui.components.WinDialog
import com.togglestudios.lumenpop.ui.components.rememberAssetImage
import kotlin.random.Random

@Composable
fun GameScreen(
    level: Int,
    progress: SavedProgress,
    soundManager: SoundManager,
    vibrationManager: VibrationManager,
    onSaveProgress: (SavedProgress) -> Unit,
    onNextLevel: (Int) -> Unit,
    onHome: () -> Unit
) {
    val levelConfig = remember(level) { LevelConfig.forLevel(level) }
    val engine = remember { GameEngine() }

    LaunchedEffect(level) {
        soundManager.playMusic("gameplay_music.mp3")
    }

    var score by remember { mutableIntStateOf(0) }
    var movesLeft by remember { mutableIntStateOf(levelConfig.moves) }
    val board = remember { mutableStateListOf<Tile>().apply { addAll(engine.createBoard(level)) } }
    val selectedIndices = remember { mutableStateListOf<Int>() }

    var isPaused by remember { mutableStateOf(false) }
    var isWin by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var starsEarned by remember { mutableIntStateOf(0) }
    var coinsAwarded by remember { mutableIntStateOf(0) }

    val bgBitmap = rememberAssetImage(levelConfig.bgAsset)
    var boardPixelSize by remember { mutableStateOf(IntSize.Zero) }

    fun processChainEnd() {
        if (selectedIndices.size >= 3) {
            val chainLength = selectedIndices.size
            val pointsEarned = (chainLength * 100 * (1.0f + (chainLength - 3) * 0.25f)).toInt()
            score += pointsEarned

            val lastIdx = selectedIndices.last()
            val specialToSpawn = when {
                chainLength >= 5 -> if (Random.nextBoolean()) SpecialType.NOVA else SpecialType.CROSS
                chainLength == 4 -> if (Random.nextBoolean()) SpecialType.BEAM_H else SpecialType.BEAM_V
                else -> SpecialType.NONE
            }

            val exploded = engine.calculateExplodedIndices(selectedIndices, board).toMutableSet()
            if (specialToSpawn != SpecialType.NONE) {
                exploded.remove(lastIdx)
            }

            soundManager.playSound(if (specialToSpawn != SpecialType.NONE) "special" else "pop", chainLength)
            vibrationManager.pop(chainLength)

            val (newBoard, _) = engine.collapseAndRefill(board, exploded, level)

            if (specialToSpawn != SpecialType.NONE && lastIdx in newBoard.indices) {
                newBoard[lastIdx] = newBoard[lastIdx].copy(special = specialToSpawn)
            }

            board.clear()
            board.addAll(newBoard)

            movesLeft = (movesLeft - 1).coerceAtLeast(0)

            if (score >= levelConfig.targetScore) {
                val finalStars = when {
                    score >= levelConfig.targetScore * 1.5 -> 3
                    score >= levelConfig.targetScore -> 2
                    else -> 1
                }
                starsEarned = finalStars
                val reward = 50 * finalStars
                coinsAwarded = reward
                progress.coins += reward
                if (level == progress.highestUnlocked) {
                    progress.highestUnlocked = (level + 1).coerceAtMost(30)
                }
                val prev = progress.completed[level]
                val best = maxOf(prev?.bestScore ?: 0, score)
                val st = maxOf(prev?.stars ?: 0, finalStars)
                progress.completed[level] = LevelStats(st, best)
                onSaveProgress(progress)

                soundManager.playSound("win")
                vibrationManager.heavy()
                isWin = true
            } else if (movesLeft <= 0) {
                soundManager.playSound("lose")
                isGameOver = true
            }
        }
        selectedIndices.clear()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (bgBitmap != null) {
            Image(
                bitmap = bgBitmap,
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D0422)))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.45f), Color.Black.copy(alpha = 0.75f))
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScoreBoardView(
                levelConfig = levelConfig,
                currentScore = score,
                movesLeft = movesLeft,
                coins = progress.coins,
                onPauseClick = { isPaused = true }
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1B0B3B).copy(alpha = 0.8f))
                        .onGloballyPositioned { coordinates ->
                            boardPixelSize = coordinates.size
                        }
                        .pointerInput(board) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (isWin || isGameOver || isPaused) return@detectDragGestures
                                    val cell = getCellIndex(offset, boardPixelSize)
                                    if (cell != null && cell in board.indices) {
                                        selectedIndices.clear()
                                        selectedIndices.add(cell)
                                        soundManager.playSound("wake")
                                        vibrationManager.tick()
                                    }
                                },
                                onDrag = { change, _ ->
                                    if (isWin || isGameOver || isPaused) return@detectDragGestures
                                    val cell = getCellIndex(change.position, boardPixelSize)
                                    if (cell != null && cell in board.indices && selectedIndices.isNotEmpty()) {
                                        val lastCell = selectedIndices.last()
                                        if (cell == lastCell) return@detectDragGestures

                                        if (selectedIndices.size > 1 && cell == selectedIndices[selectedIndices.size - 2]) {
                                            selectedIndices.removeAt(selectedIndices.size - 1)
                                            soundManager.playSound("backtrack")
                                            vibrationManager.tick()
                                            return@detectDragGestures
                                        }

                                        val firstColor = board[selectedIndices.first()].color
                                        val targetTile = board[cell]
                                        val isColorMatch = targetTile.color == firstColor || targetTile.isFusion || board[selectedIndices.first()].isFusion
                                        val isAdjacent = GameEngine.areAdjacent(lastCell, cell)

                                        if (isAdjacent && isColorMatch && !selectedIndices.contains(cell)) {
                                            selectedIndices.add(cell)
                                            soundManager.playSound("link", selectedIndices.size)
                                            vibrationManager.tick()
                                        }
                                    }
                                },
                                onDragEnd = {
                                    processChainEnd()
                                },
                                onDragCancel = {
                                    selectedIndices.clear()
                                }
                            )
                        }
                        .padding(6.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(GameEngine.BOARD_SIZE),
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = false
                    ) {
                        itemsIndexed(board) { index, tile ->
                            LumenTileView(
                                tile = tile,
                                isSelected = selectedIndices.contains(index),
                                isRefilled = false
                            )
                        }
                    }
                }
            }

            BoosterBar(
                coins = progress.coins,
                onBoosterClick = { booster ->
                    when (booster) {
                        BoosterKind.SHUFFLE -> {
                            if (progress.coins >= BoosterKind.SHUFFLE.price) {
                                progress.coins -= BoosterKind.SHUFFLE.price
                                val shuffled = engine.shuffleBoard(board)
                                board.clear()
                                board.addAll(shuffled)
                                soundManager.playSound("booster")
                                vibrationManager.pop()
                                onSaveProgress(progress)
                            }
                        }
                        BoosterKind.BOMB -> {
                            if (progress.coins >= BoosterKind.BOMB.price) {
                                progress.coins -= BoosterKind.BOMB.price
                                val (newB, pts) = engine.executeBombBooster(board)
                                score += pts
                                board.clear()
                                board.addAll(newB)
                                soundManager.playSound("special")
                                vibrationManager.heavy()
                                onSaveProgress(progress)
                            }
                        }
                        BoosterKind.BURST -> {
                            if (progress.coins >= BoosterKind.BURST.price) {
                                progress.coins -= BoosterKind.BURST.price
                                val (newB, pts) = engine.executeBurstBooster(board)
                                score += pts
                                board.clear()
                                board.addAll(newB)
                                soundManager.playSound("special")
                                vibrationManager.heavy()
                                onSaveProgress(progress)
                            }
                        }
                    }
                }
            )
        }

        if (isWin) {
            WinDialog(
                score = score,
                stars = starsEarned,
                coinsEarned = coinsAwarded,
                onNextLevel = {
                    isWin = false
                    onNextLevel((level + 1).coerceAtMost(30))
                },
                onReplay = {
                    isWin = false
                    score = 0
                    movesLeft = levelConfig.moves
                    board.clear()
                    board.addAll(engine.createBoard(level))
                }
            )
        }

        if (isGameOver) {
            GameOverDialog(
                score = score,
                targetScore = levelConfig.targetScore,
                onRetry = {
                    isGameOver = false
                    score = 0
                    movesLeft = levelConfig.moves
                    board.clear()
                    board.addAll(engine.createBoard(level))
                },
                onHome = onHome
            )
        }

        if (isPaused) {
            PauseDialog(
                onResume = { isPaused = false },
                onRestart = {
                    isPaused = false
                    score = 0
                    movesLeft = levelConfig.moves
                    board.clear()
                    board.addAll(engine.createBoard(level))
                },
                onHome = onHome
            )
        }
    }
}

private fun getCellIndex(offset: Offset, size: IntSize): Int? {
    if (size.width <= 0 || size.height <= 0) return null
    val cellW = size.width / GameEngine.BOARD_SIZE.toFloat()
    val cellH = size.height / GameEngine.BOARD_SIZE.toFloat()
    val col = (offset.x / cellW).toInt()
    val row = (offset.y / cellH).toInt()
    if (col in 0 until GameEngine.BOARD_SIZE && row in 0 until GameEngine.BOARD_SIZE) {
        return row * GameEngine.BOARD_SIZE + col
    }
    return null
}
