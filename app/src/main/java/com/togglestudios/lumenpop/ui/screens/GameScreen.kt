package com.togglestudios.lumenpop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.delay
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
    var dragDirection by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var activeBooster by remember { mutableStateOf<BoosterKind?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    var isPaused by remember { mutableStateOf(false) }
    var isWin by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var starsEarned by remember { mutableIntStateOf(0) }
    var coinsAwarded by remember { mutableIntStateOf(0) }

    val bgBitmap = rememberAssetImage(levelConfig.bgAsset)
    var boardPixelSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2200)
            toastMessage = null
        }
    }

    fun applyTargetedBooster(booster: BoosterKind, cellIndex: Int) {
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
                    toastMessage = "Board rearranged · a path remains"
                } else {
                    toastMessage = "Not enough shards yet"
                }
            }
            BoosterKind.BOMB -> {
                if (progress.coins >= BoosterKind.BOMB.price) {
                    progress.coins -= BoosterKind.BOMB.price
                    val (newB, pts) = engine.executeBombBooster(board, cellIndex)
                    score += pts
                    board.clear()
                    board.addAll(newB)
                    soundManager.playSound("special")
                    vibrationManager.heavy()
                    onSaveProgress(progress)
                    toastMessage = "Pulse Bomb cleared a pocket"
                } else {
                    toastMessage = "Not enough shards yet"
                }
            }
            BoosterKind.BURST -> {
                if (progress.coins >= BoosterKind.BURST.price) {
                    progress.coins -= BoosterKind.BURST.price
                    val (newB, pts) = engine.executeBurstBooster(board, cellIndex)
                    score += pts
                    board.clear()
                    board.addAll(newB)
                    soundManager.playSound("special")
                    vibrationManager.heavy()
                    onSaveProgress(progress)
                    toastMessage = "Burst Wave released"
                } else {
                    toastMessage = "Not enough shards yet"
                }
            }
        }
        activeBooster = null
    }

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
                    score >= (levelConfig.targetScore * 1.5).toInt() -> 3
                    score >= levelConfig.targetScore -> 2
                    else -> 1
                }
                starsEarned = finalStars
                val reward = 100 + finalStars * 25
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
        } else if (selectedIndices.isNotEmpty()) {
            toastMessage = "Almost there · link one more Lumen"
            soundManager.playSound("backtrack")
        }
        selectedIndices.clear()
        dragDirection = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fullscreen background image
        if (bgBitmap != null) {
            Image(
                bitmap = bgBitmap,
                contentDescription = "Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0D0422))
            )
        }

        // Vignette gradient
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

            // Board Box
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
                        .border(
                            2.dp,
                            if (activeBooster != null) Color(0xFFFFD700) else Color(0xFF00F0FF).copy(alpha = 0.4f),
                            RoundedCornerShape(20.dp)
                        )
                        .onGloballyPositioned { coordinates ->
                            boardPixelSize = coordinates.size
                        }
                        .pointerInput(board, activeBooster) {
                            if (activeBooster != null) {
                                detectTapGestures { offset ->
                                    val cell = getCellIndex(offset, boardPixelSize)
                                    if (cell != null && cell in board.indices) {
                                        applyTargetedBooster(activeBooster!, cell)
                                    }
                                }
                            } else {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        if (isWin || isGameOver || isPaused) return@detectDragGestures
                                        val cell = getCellIndex(offset, boardPixelSize)
                                        if (cell != null && cell in board.indices) {
                                            selectedIndices.clear()
                                            selectedIndices.add(cell)
                                            dragDirection = null
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

                                            // Backtracking
                                            if (selectedIndices.size > 1 && cell == selectedIndices[selectedIndices.size - 2]) {
                                                selectedIndices.removeAt(selectedIndices.size - 1)
                                                if (selectedIndices.size < 2) dragDirection = null
                                                soundManager.playSound("backtrack")
                                                vibrationManager.tick()
                                                return@detectDragGestures
                                            }

                                            // Raycast intermediate cells
                                            val dir = GameEngine.directionBetween(lastCell, cell)
                                            if (dir != null) {
                                                if (dragDirection == null) {
                                                    dragDirection = dir
                                                } else if (dragDirection != dir) {
                                                    return@detectDragGestures
                                                }
                                            }

                                            val targetTile = board[cell]
                                            val firstColor = board[selectedIndices.first()].color
                                            val isColorMatch = targetTile.color == firstColor || targetTile.isFusion || board[selectedIndices.first()].isFusion
                                            val isAdjacent = GameEngine.areAdjacent(lastCell, cell)

                                            if (isColorMatch && isAdjacent && !selectedIndices.contains(cell)) {
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
                                        dragDirection = null
                                    }
                                )
                            }
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

                    // Chain Trail glowing line overlay
                    if (selectedIndices.isNotEmpty()) {
                        val activeColorHex = board.getOrNull(selectedIndices.first())?.color?.hexColor ?: 0xFF00F0FF
                        val trailColor = Color(activeColorHex)
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val cellW = size.width / GameEngine.BOARD_SIZE.toFloat()
                            val cellH = size.height / GameEngine.BOARD_SIZE.toFloat()
                            val path = Path()
                            selectedIndices.forEachIndexed { idx, boardIdx ->
                                val col = boardIdx % GameEngine.BOARD_SIZE
                                val row = boardIdx / GameEngine.BOARD_SIZE
                                val cx = col * cellW + cellW / 2f
                                val cy = row * cellH + cellH / 2f
                                if (idx == 0) path.moveTo(cx, cy) else path.lineTo(cx, cy)
                            }
                            // Outer glow
                            drawPath(
                                path = path,
                                color = trailColor.copy(alpha = 0.4f),
                                style = Stroke(width = 16f, cap = StrokeCap.Round)
                            )
                            // Inner crisp line
                            drawPath(
                                path = path,
                                color = trailColor,
                                style = Stroke(width = 6f, cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            // Hint / Status text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                val statusText = when {
                    activeBooster != null -> "Tap a Lumen to aim your ${activeBooster?.title}"
                    selectedIndices.size >= 2 -> "${selectedIndices.size} linked · ${if (selectedIndices.size >= 3) "release to pop" else "find one more"}"
                    else -> "Drag matching Lumens to link 3 or more"
                }
                Text(
                    text = statusText,
                    color = if (activeBooster != null) Color(0xFFFFD700) else Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Booster Bar
            BoosterBar(
                coins = progress.coins,
                onBoosterClick = { booster ->
                    if (booster == BoosterKind.SHUFFLE) {
                        applyTargetedBooster(BoosterKind.SHUFFLE, 0)
                    } else {
                        if (progress.coins < booster.price) {
                            toastMessage = "Not enough shards yet"
                        } else {
                            activeBooster = if (activeBooster == booster) null else booster
                            toastMessage = if (activeBooster != null) "Tap a Lumen to aim your ${booster.title}" else "Tool holstered"
                        }
                    }
                }
            )
        }

        // Floating Toast message
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1B0B3B).copy(alpha = 0.95f))
                    .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = toastMessage ?: "",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Dialogs
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
