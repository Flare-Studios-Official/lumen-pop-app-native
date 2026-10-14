package com.togglestudios.lumenpop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.togglestudios.lumenpop.R
import com.togglestudios.lumenpop.data.BoosterKind
import com.togglestudios.lumenpop.data.LevelConfig
import com.togglestudios.lumenpop.data.LevelStats
import com.togglestudios.lumenpop.data.LumenColor
import com.togglestudios.lumenpop.data.SavedProgress
import com.togglestudios.lumenpop.data.SpecialType
import com.togglestudios.lumenpop.data.Tile
import com.togglestudios.lumenpop.engine.GameEngine
import com.togglestudios.lumenpop.engine.SoundManager
import com.togglestudios.lumenpop.engine.VibrationManager
import com.togglestudios.lumenpop.ui.components.BoosterBar
import com.togglestudios.lumenpop.ui.components.GameHud
import com.togglestudios.lumenpop.ui.components.OverlayType
import com.togglestudios.lumenpop.ui.components.ResultOverlay
import com.togglestudios.lumenpop.ui.components.rememberAssetImage
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sign
import kotlin.random.Random

private val TRAIL_COLORS = mapOf(
    LumenColor.SOLAR to Color(0xFFFFE56F),
    LumenColor.VERDANT to Color(0xFF7EF4B0),
    LumenColor.TERRA to Color(0xFFFF9A65),
    LumenColor.NOVA to Color(0xFFFF8CCC),
    LumenColor.COSMIC to Color(0xFFC29AFF),
    LumenColor.AETHER to Color(0xFF69EAFF),
    LumenColor.BLAZE to Color(0xFFFF6F8D)
)

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
        if (progress.music) {
            soundManager.playMusic("gameplay_music.mp3")
        }
    }

    var score by remember { mutableIntStateOf(0) }
    var movesLeft by remember { mutableIntStateOf(levelConfig.moves) }
    val board = remember { mutableStateListOf<Tile>().apply { addAll(engine.createBoard(level)) } }
    val selectedIndices = remember { mutableStateListOf<Int>() }
    var activeColor by remember { mutableStateOf<LumenColor?>(null) }
    var lockedDirection by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isDragging by remember { mutableStateOf(false) }

    var activeBooster by remember { mutableStateOf<BoosterKind?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var activeOverlay by remember { mutableStateOf<OverlayType?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    val bgBitmap = rememberAssetImage(levelConfig.bgAsset)

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(1800)
            toastMessage = null
        }
    }

    fun scoreForChain(length: Int): Int = when {
        length < 3 -> 0
        length == 3 -> 30
        length == 4 -> 55
        length == 5 -> 85
        length == 6 -> 120
        else -> 120 + (length - 6) * 35
    }

    fun starsForScore(currentScore: Int): Int = when {
        currentScore >= levelConfig.targetScore -> 3
        currentScore >= (levelConfig.targetScore * 0.66).toInt() -> 2
        currentScore >= (levelConfig.targetScore * 0.33).toInt() -> 1
        else -> 0
    }

    fun clearChain() {
        selectedIndices.clear()
        activeColor = null
        lockedDirection = null
        isDragging = false
    }

    // Direct tap on a special tile or fusion vortex
    fun triggerDirectSpecial(index: Int) {
        if (isBusy || activeOverlay != null) return
        isBusy = true
        val tile = board.getOrNull(index) ?: run { isBusy = false; return }

        val centerRow = GameEngine.rowOf(index)
        val centerCol = GameEngine.colOf(index)
        val cleared = mutableSetOf<Int>()
        cleared.add(index)

        if (tile.isFusion) {
            for (i in 0 until GameEngine.TOTAL_TILES) {
                val r = GameEngine.rowOf(i)
                val c = GameEngine.colOf(i)
                val dist = hypot((r - centerRow).toDouble(), (c - centerCol).toDouble())
                if (dist <= 2.25 || (dist <= 3.1 && board[i].color == tile.color)) {
                    cleared.add(i)
                }
            }
        } else {
            when (tile.special) {
                SpecialType.NOVA -> {
                    for (dr in -1..1) for (dc in -1..1) {
                        val nr = centerRow + dr
                        val nc = centerCol + dc
                        if (nr in 0 until 7 && nc in 0 until 7) cleared.add(nr * 7 + nc)
                    }
                }
                SpecialType.BEAM_H -> {
                    for (c in 0 until 7) cleared.add(centerRow * 7 + c)
                }
                SpecialType.BEAM_V -> {
                    for (r in 0 until 7) cleared.add(r * 7 + centerCol)
                }
                SpecialType.CROSS -> {
                    for (c in 0 until 7) cleared.add(centerRow * 7 + c)
                    for (r in 0 until 7) cleared.add(r * 7 + centerCol)
                }
                SpecialType.NONE -> Unit
            }
        }

        movesLeft = (movesLeft - 1).coerceAtLeast(0)
        toastMessage = "Special released · energy mixed"
        clearChain()
        soundManager.playSound("special", (cleared.size / 3).coerceIn(1, 3))
        vibrationManager.heavy()

        val points = cleared.size * 10
        score += points

        val (newBoard, _) = engine.collapseAndRefill(board, cleared, level)
        board.clear()
        board.addAll(newBoard)
        soundManager.playSound("gravity")

        if (score >= levelConfig.targetScore) {
            val earnedStars = starsForScore(score)
            val prev = progress.completed[level]
            progress.highestUnlocked = maxOf(progress.highestUnlocked, level + 1)
            progress.coins += 100 + earnedStars * 25
            progress.completed[level] = LevelStats(
                stars = maxOf(prev?.stars ?: 0, earnedStars),
                bestScore = maxOf(prev?.bestScore ?: 0, score)
            )
            onSaveProgress(progress)
            soundManager.playSound("win")
            activeOverlay = OverlayType.COMPLETE
        } else if (movesLeft <= 0) {
            soundManager.playSound("lose")
            activeOverlay = OverlayType.FAIL
        }

        isBusy = false
    }

    // Process completed chain on drag release
    fun finishTurn(chain: List<Int>) {
        if (isBusy || activeOverlay != null) return

        // Check if user touched a vortex / special tile directly
        val specialIndex = chain.firstOrNull { board.getOrNull(it)?.isFusion == true || board.getOrNull(it)?.special != SpecialType.NONE }
        if (specialIndex != null) {
            triggerDirectSpecial(specialIndex)
            return
        }

        if (chain.size < 3) {
            if (chain.isNotEmpty()) {
                toastMessage = "Almost there · link one more Lumen"
                soundManager.playSound("backtrack")
                vibrationManager.tick()
            }
            clearChain()
            return
        }

        isBusy = true
        val chainColor = board[chain[0]].color
        val extra = mutableSetOf<Int>()
        val lastTile = chain.last()
        val centerRow = GameEngine.rowOf(lastTile)
        val centerCol = GameEngine.colOf(lastTile)

        // Web logic power-ups on chain release
        when {
            chain.size >= 7 -> {
                toastMessage = "7+ Surge · All ${chainColor.colorName} Lumens cleared!"
                board.forEachIndexed { i, t ->
                    if (t.color == chainColor) extra.add(i)
                }
            }
            chain.size == 6 -> {
                toastMessage = "6-link · Cross Blast!"
                for (c in 0 until 7) extra.add(centerRow * 7 + c)
                for (r in 0 until 7) extra.add(r * 7 + centerCol)
            }
            chain.size == 5 -> {
                toastMessage = "5-link · Nova Blast!"
                for (dr in -1..1) for (dc in -1..1) {
                    val nr = centerRow + dr
                    val nc = centerCol + dc
                    if (nr in 0 until 7 && nc in 0 until 7) extra.add(nr * 7 + nc)
                }
            }
            chain.size == 4 -> {
                toastMessage = "4-link · Beam Blast!"
                val isRow = Random.nextBoolean()
                if (isRow) {
                    for (c in 0 until 7) extra.add(centerRow * 7 + c)
                } else {
                    for (r in 0 until 7) extra.add(r * 7 + centerCol)
                }
            }
        }

        val allToClear = (chain + extra).toSet()
        val gained = scoreForChain(chain.size) + extra.size * 10
        score += gained
        movesLeft = (movesLeft - 1).coerceAtLeast(0)

        soundManager.playSound(if (extra.isNotEmpty()) "special" else "pop", (chain.size / 2).coerceIn(1, 3))
        vibrationManager.pop(chain.size)

        val (newBoard, _) = engine.collapseAndRefill(board, allToClear, level)
        board.clear()
        board.addAll(newBoard)
        soundManager.playSound("gravity")

        clearChain()

        if (score >= levelConfig.targetScore) {
            val earnedStars = starsForScore(score)
            val prev = progress.completed[level]
            progress.highestUnlocked = maxOf(progress.highestUnlocked, level + 1)
            progress.coins += 100 + earnedStars * 25
            progress.completed[level] = LevelStats(
                stars = maxOf(prev?.stars ?: 0, earnedStars),
                bestScore = maxOf(prev?.bestScore ?: 0, score)
            )
            onSaveProgress(progress)
            soundManager.playSound("win")
            activeOverlay = OverlayType.COMPLETE
        } else if (movesLeft <= 0) {
            soundManager.playSound("lose")
            activeOverlay = OverlayType.FAIL
        }

        isBusy = false
    }

    // Apply booster
    fun applyBooster(kind: BoosterKind, cellIndex: Int) {
        if (progress.coins < kind.price) {
            toastMessage = "Not enough shards yet"
            activeBooster = null
            return
        }
        progress.coins -= kind.price
        onSaveProgress(progress)
        activeBooster = null

        when (kind) {
            BoosterKind.SHUFFLE -> {
                val shuffled = engine.shuffleBoard(board)
                board.clear()
                board.addAll(shuffled)
                soundManager.playSound("booster")
                vibrationManager.pop()
                toastMessage = "The board rearranged · a path remains"
            }
            BoosterKind.BOMB -> {
                val (newB, pts) = engine.executeBombBooster(board, cellIndex)
                score += pts
                board.clear()
                board.addAll(newB)
                soundManager.playSound("special")
                vibrationManager.heavy()
                toastMessage = "Pulse Bomb cleared a pocket"
            }
            BoosterKind.BURST -> {
                val (newB, pts) = engine.executeBurstBooster(board, cellIndex)
                score += pts
                board.clear()
                board.addAll(newB)
                soundManager.playSound("special")
                vibrationManager.heavy()
                toastMessage = "Burst Wave released"
            }
        }

        if (score >= levelConfig.targetScore) {
            val earnedStars = starsForScore(score)
            val prev = progress.completed[level]
            progress.highestUnlocked = maxOf(progress.highestUnlocked, level + 1)
            progress.coins += 100 + earnedStars * 25
            progress.completed[level] = LevelStats(
                stars = maxOf(prev?.stars ?: 0, earnedStars),
                bestScore = maxOf(prev?.bestScore ?: 0, score)
            )
            onSaveProgress(progress)
            soundManager.playSound("win")
            activeOverlay = OverlayType.COMPLETE
        }
    }

    fun restartGame() {
        score = 0
        movesLeft = levelConfig.moves
        activeOverlay = null
        isBusy = false
        clearChain()
        board.clear()
        board.addAll(engine.createBoard(level))
        toastMessage = "A fresh glow is ready"
    }

    val formattedShards = remember(progress.coins) {
        NumberFormat.getNumberInstance(Locale.US).format(progress.coins)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0730))
    ) {
        // Biome background with 0.28 opacity
        if (bgBitmap != null) {
            Image(
                bitmap = bgBitmap,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.28f
            )
        }

        // Floating ambient sparkles
        CosmicSparkles()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // HUD Top section: Topbar, Heading, Stats
            GameHud(
                levelConfig = levelConfig,
                score = score,
                moves = movesLeft,
                lessonOrBoosterText = if (activeBooster != null) "Choose a cell for your ${activeBooster?.title}" else levelConfig.lesson,
                onBack = onHome,
                onSettings = { activeOverlay = OverlayType.PAUSE },
                onPause = {
                    clearChain()
                    activeBooster = null
                    activeOverlay = OverlayType.PAUSE
                }
            )

            // Board Container: Rounded frame with 7x7 grid and chain trail
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                val availableSize = minOf(maxWidth, maxHeight)
                val boardPaddingDp = 10.dp
                val gridGapDp = 6.dp
                val innerSize = availableSize - (boardPaddingDp * 2)
                val tileSizeDp = (innerSize - (gridGapDp * 6)) / 7

                Box(
                    modifier = Modifier
                        .size(availableSize)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFF0B0330).copy(alpha = 0.85f))
                        .border(
                            width = 1.dp,
                            color = if (isDragging) Color(0xFFFFEC7D).copy(alpha = 0.75f) else Color.White.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(26.dp)
                        )
                        .padding(boardPaddingDp)
                        .pointerInput(board, activeBooster, isBusy) {
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                if (isBusy || activeOverlay != null) return@awaitEachGesture

                                val bWidth = size.width.toFloat()
                                val bHeight = size.height.toFloat()
                                val cW = bWidth / 7f
                                val cH = bHeight / 7f

                                val startCol = (down.position.x / cW).toInt().coerceIn(0, 6)
                                val startRow = (down.position.y / cH).toInt().coerceIn(0, 6)
                                val startIdx = startRow * 7 + startCol

                                if (activeBooster != null) {
                                    applyBooster(activeBooster!!, startIdx)
                                    return@awaitEachGesture
                                }

                                val startTile = board.getOrNull(startIdx) ?: return@awaitEachGesture
                                selectedIndices.clear()
                                selectedIndices.add(startIdx)
                                activeColor = startTile.color
                                lockedDirection = null
                                isDragging = true
                                soundManager.playSound("wake")
                                vibrationManager.tick()

                                var lastIdx = startIdx

                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull() ?: break
                                    if (!change.pressed) {
                                        break
                                    }

                                    val curCol = (change.position.x / cW).toInt().coerceIn(0, 6)
                                    val curRow = (change.position.y / cH).toInt().coerceIn(0, 6)
                                    val targetIdx = curRow * 7 + curCol

                                    if (targetIdx == lastIdx) continue

                                    // Backtracking
                                    if (selectedIndices.contains(targetIdx)) {
                                        val pos = selectedIndices.indexOf(targetIdx)
                                        if (pos < selectedIndices.size - 1) {
                                            while (selectedIndices.size > pos + 1) {
                                                selectedIndices.removeAt(selectedIndices.size - 1)
                                            }
                                            lastIdx = selectedIndices.last()
                                            lockedDirection = if (selectedIndices.size > 1) {
                                                val f = selectedIndices[0]
                                                val s = selectedIndices[1]
                                                Pair(
                                                    (GameEngine.rowOf(s) - GameEngine.rowOf(f)).coerceIn(-1, 1),
                                                    (GameEngine.colOf(s) - GameEngine.colOf(f)).coerceIn(-1, 1)
                                                )
                                            } else null
                                            soundManager.playSound("backtrack")
                                            vibrationManager.tick()
                                        }
                                        continue
                                    }

                                    // Raycasting intermediate tiles
                                    val rowDiff = curRow - GameEngine.rowOf(lastIdx)
                                    val colDiff = curCol - GameEngine.colOf(lastIdx)
                                    if (rowDiff == 0 && colDiff == 0) continue

                                    val stepDir = when {
                                        rowDiff == 0 -> Pair(0, sign(colDiff.toDouble()).toInt())
                                        colDiff == 0 -> Pair(sign(rowDiff.toDouble()).toInt(), 0)
                                        abs(rowDiff) == abs(colDiff) -> Pair(sign(rowDiff.toDouble()).toInt(), sign(colDiff.toDouble()).toInt())
                                        else -> null
                                    } ?: continue

                                    if (lockedDirection != null && lockedDirection != stepDir) {
                                        continue
                                    }

                                    var curr = lastIdx
                                    val toAdd = mutableListOf<Int>()
                                    var valid = true

                                    while (curr != targetIdx) {
                                        val nR = GameEngine.rowOf(curr) + stepDir.first
                                        val nC = GameEngine.colOf(curr) + stepDir.second
                                        if (nR !in 0 until 7 || nC !in 0 until 7) {
                                            valid = false
                                            break
                                        }
                                        val nIdx = nR * 7 + nC
                                        val nTile = board.getOrNull(nIdx)
                                        if (nTile == null || nTile.color != activeColor || selectedIndices.contains(nIdx)) {
                                            valid = false
                                            break
                                        }
                                        toAdd.add(nIdx)
                                        curr = nIdx
                                    }

                                    if (valid && toAdd.isNotEmpty()) {
                                        if (lockedDirection == null) {
                                            lockedDirection = stepDir
                                        }
                                        selectedIndices.addAll(toAdd)
                                        lastIdx = selectedIndices.last()
                                        soundManager.playSound("link", selectedIndices.size)
                                        vibrationManager.tick()
                                    }
                                }

                                // Pointer Up: finish chain
                                finishTurn(selectedIndices.toList())
                            }
                        }
                ) {
                    // 7x7 Grid of Lumens
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (r in 0 until 7) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for (c in 0 until 7) {
                                    val idx = r * 7 + c
                                    val tile = board.getOrNull(idx)
                                    val isSelected = selectedIndices.contains(idx)

                                    if (tile != null) {
                                        val asset = when {
                                            tile.isFusion -> "fusion_orb.png"
                                            isSelected -> tile.color.openedAsset
                                            else -> tile.color.closedAsset
                                        }
                                        val bitmap = rememberAssetImage(asset)

                                        Box(
                                            modifier = Modifier
                                                .size(tileSizeDp)
                                                .scale(if (isSelected) 1.07f else 1.0f)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(Color.White.copy(alpha = 0.055f))
                                                .border(
                                                    width = if (isSelected) 2.dp else 0.dp,
                                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color.Transparent,
                                                    shape = RoundedCornerShape(14.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (bitmap != null) {
                                                Image(
                                                    bitmap = bitmap,
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(0.85f),
                                                    contentScale = ContentScale.Fit
                                                )
                                            }

                                            // Special tile overlay icon
                                            if (tile.special != SpecialType.NONE) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF00F0FF).copy(alpha = 0.8f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = when (tile.special) {
                                                            SpecialType.BEAM_H -> "↔"
                                                            SpecialType.BEAM_V -> "↕"
                                                            SpecialType.NOVA -> "★"
                                                            SpecialType.CROSS -> "✚"
                                                            SpecialType.NONE -> ""
                                                        },
                                                        color = Color(0xFF0E0730),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(tileSizeDp))
                                    }
                                }
                            }
                        }
                    }

                    // Chain Trail Line Canvas Overlay
                    if (selectedIndices.size > 1 && activeColor != null) {
                        val trailColor = TRAIL_COLORS[activeColor] ?: Color(0xFFFFE56F)

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val cW = w / 7f
                            val cH = h / 7f

                            val points = selectedIndices.map { idx ->
                                Offset(
                                    (GameEngine.colOf(idx) + 0.5f) * cW,
                                    (GameEngine.rowOf(idx) + 0.5f) * cH
                                )
                            }

                            val path = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }

                            // Outer glow stroke
                            drawPath(
                                path = path,
                                color = trailColor.copy(alpha = 0.85f),
                                style = Stroke(
                                    width = 7.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Inner white core stroke
                            drawPath(
                                path = path,
                                color = Color.White.copy(alpha = 0.9f),
                                style = Stroke(
                                    width = 2.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )

                            // Circle nodes at each connection
                            points.forEach { pt ->
                                drawCircle(
                                    color = Color.White,
                                    radius = 3.5.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }

                    // Floating Chain Count Badge (e.g. "4 LINK" / "5 CHARGE")
                    if (isDragging && selectedIndices.size > 1) {
                        val lastIdx = selectedIndices.last()
                        val lastCol = GameEngine.colOf(lastIdx)
                        val lastRow = GameEngine.rowOf(lastIdx)

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = (-8).dp, y = (-12).dp)
                                .size(44.dp)
                                .shadow(12.dp, CircleShape, spotColor = Color(0xFFFFE26F))
                                .clip(CircleShape)
                                .background(Color(0xFFFFE26F))
                                .border(2.dp, Color.White.copy(alpha = 0.9f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${selectedIndices.size}",
                                    color = Color(0xFF30136E),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    lineHeight = 15.sp
                                )
                                Text(
                                    text = if (selectedIndices.size >= 5) "CHARGE" else "LINK",
                                    color = Color(0xFF30136E),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Section: Hint, Boosters, Currency Line
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
            ) {
                // Hint text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sparkles),
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedIndices.size >= 2) {
                            "${selectedIndices.size} linked · ${if (selectedIndices.size >= 3) "release to pop" else "find one more"}"
                        } else {
                            "Drag through matching Lumens to link 3 or more"
                        },
                        color = Color.White.copy(alpha = 0.55f),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Booster Bar
                BoosterBar(
                    coins = progress.coins,
                    activeBooster = activeBooster,
                    disabled = isBusy,
                    onBoosterClick = { kind ->
                        if (kind == BoosterKind.SHUFFLE) {
                            applyBooster(kind, 0)
                        } else {
                            activeBooster = if (activeBooster == kind) null else kind
                            toastMessage = if (activeBooster == kind) "Tap a Lumen to aim your ${kind.title}" else "Tool holstered"
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Currency line
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$formattedShards shards",
                        color = Color(0xFFFFE176),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (activeBooster != null) "Tap a Lumen to aim" else "Prism Vortexes appear after 5-link charges",
                        color = Color.White.copy(alpha = 0.45f),
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Animated Toast Notice
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 30 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 30 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF1A0945).copy(alpha = 0.95f))
                    .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(99.dp))
                    .shadow(16.dp, shape = RoundedCornerShape(99.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = toastMessage ?: "",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Result / Pause Overlay
        if (activeOverlay != null) {
            ResultOverlay(
                type = activeOverlay!!,
                score = score,
                targetScore = levelConfig.targetScore,
                stars = starsForScore(score),
                onPrimary = {
                    when (activeOverlay) {
                        OverlayType.COMPLETE -> {
                            activeOverlay = null
                            onNextLevel(level + 1)
                        }
                        OverlayType.FAIL -> restartGame()
                        OverlayType.PAUSE -> activeOverlay = null
                        null -> Unit
                    }
                },
                onSecondary = {
                    when (activeOverlay) {
                        OverlayType.COMPLETE -> restartGame()
                        OverlayType.PAUSE, OverlayType.FAIL -> onHome()
                        null -> Unit
                    }
                },
                onClose = { activeOverlay = null }
            )
        }
    }
}
