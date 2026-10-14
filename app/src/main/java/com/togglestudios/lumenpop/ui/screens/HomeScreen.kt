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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.togglestudios.lumenpop.R
import com.togglestudios.lumenpop.data.SavedProgress
import com.togglestudios.lumenpop.ui.components.rememberAssetImage
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    progress: SavedProgress,
    onSelectLevel: (Int) -> Unit,
    onSettings: () -> Unit,
    onProfileClick: () -> Unit,
    onSaveProgress: (SavedProgress) -> Unit
) {
    val latest = progress.highestUnlocked
    val biomeIndex = ((latest - 1) % 10) + 1
    val bgBitmap = rememberAssetImage("bg_level_$biomeIndex.png")
    val mapBgBitmap = rememberAssetImage("bg_level_1.png")

    val totalLevelsToShow = maxOf(10, minOf(50, latest + 6))
    val mapLevels = remember(latest) { (1..totalLevelsToShow).toList() }
    val mapRows = (mapLevels.size + 3) / 4

    var notice by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(notice) {
        if (notice != null) {
            delay(2200)
            notice = null
        }
    }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val giftClaimed = progress.dailyGiftClaimedOn == todayStr

    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 18 -> "Good afternoon"
        else -> "Good evening"
    }
    val displayName = progress.username.ifBlank { "stargazer" }

    val totalStars = remember(progress.completed) {
        progress.completed.values.sumOf { it.stars }
    }

    // Number formatter for comma separation (e.g. 1,500)
    val formattedCoins = remember(progress.coins) {
        NumberFormat.getNumberInstance(Locale.US).format(progress.coins)
    }

    // Pulse animation for the currently active level node
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "node_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0730))
    ) {
        // World Background Image with low opacity
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

        // Main scrollable container matching web .home-content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            // Top Bar: Profile DP button on left, Energy coin pill on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar button with glowing neon gradient ring
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFF00F0FF).copy(alpha = 0.5f)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(
                                    Color(0xFF00F0FF),
                                    Color(0xFF6366F1),
                                    Color(0xFFEC4899),
                                    Color(0xFF00F0FF)
                                )
                            )
                        )
                        .clickable { onProfileClick() }
                        .padding(2.5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFF12053C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.take(1).uppercase(),
                            color = Color(0xFF00F0FF),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // Energy Pill: "🪙 1,500"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(99.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        // Glowing energy core dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .shadow(8.dp, CircleShape, spotColor = Color(0xFFFFD700))
                                .clip(CircleShape)
                                .background(Color(0xFFFFDA5C))
                        )
                        Text(
                            text = formattedCoins,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Welcome Card: "THE FIRST SPARK", "Good afternoon, {username}." + sparkle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Text(
                    text = "THE FIRST SPARK",
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$greeting,\n$displayName.",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp,
                        lineHeight = 36.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sparkles),
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier
                            .size(24.dp)
                            .offset(y = 8.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "The Lumens are humming your name.",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Map Card: Scrollable 4-column snake level path
            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val mapCardWidth = screenWidth - 40.dp
            val sceneHeight = (mapRows * 105 + 40).dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sceneHeight)
                    .clip(RoundedCornerShape(28.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.24f), RoundedCornerShape(28.dp))
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(28.dp))
            ) {
                // Map background illustration
                if (mapBgBitmap != null) {
                    Image(
                        bitmap = mapBgBitmap,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF280E6E).copy(alpha = 0.45f),
                                    Color(0xFF1E0649).copy(alpha = 0.82f)
                                )
                            )
                        )
                )

                // Dashed path line drawn on Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val nodeOffsets = mapLevels.map { lvl ->
                        val r = (lvl - 1) / 4
                        val slot = (lvl - 1) % 4
                        val order = if (r % 2 == 0) slot else 3 - slot
                        val x = w * (0.16f + order * 0.226f)
                        val y = (38 + r * 105).dp.toPx()
                        Offset(x, y)
                    }

                    if (nodeOffsets.size > 1) {
                        val path = Path().apply {
                            moveTo(nodeOffsets[0].x, nodeOffsets[0].y)
                            for (i in 1 until nodeOffsets.size) {
                                lineTo(nodeOffsets[i].x, nodeOffsets[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = 0.22f),
                            style = Stroke(
                                width = 4.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 18f), 0f)
                            )
                        )
                    }
                }

                // Level Node buttons
                mapLevels.forEach { lvl ->
                    val r = (lvl - 1) / 4
                    val slot = (lvl - 1) % 4
                    val order = if (r % 2 == 0) slot else 3 - slot
                    val isUnlocked = lvl <= latest
                    val isCurrent = lvl == latest
                    val stars = progress.completed[lvl]?.stars ?: 0

                    val xPercent = 0.16f + order * 0.226f
                    val yDp = (38 + r * 105).dp

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(
                                x = (mapCardWidth.value * xPercent).dp - 28.dp,
                                y = yDp - 28.dp
                            )
                            .clickable {
                                if (isUnlocked) {
                                    onSelectLevel(lvl)
                                } else {
                                    notice = "Clear earlier levels to reach this meadow"
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Orb Circle
                            Box(
                                modifier = Modifier
                                    .size(if (isCurrent) (54 * pulseScale).dp else 52.dp)
                                    .shadow(
                                        elevation = if (isUnlocked) 14.dp else 4.dp,
                                        shape = CircleShape,
                                        spotColor = if (isUnlocked) Color(0xFFFFD700).copy(alpha = 0.5f) else Color.Transparent
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        if (isUnlocked) {
                                            Brush.linearGradient(
                                                listOf(
                                                    Color(0xFFFFF8AB),
                                                    Color(0xFFFFC735),
                                                    Color(0xFFF68132)
                                                )
                                            )
                                        } else {
                                            Brush.linearGradient(
                                                listOf(Color(0xFF9588C7), Color(0xFF4A397E))
                                            )
                                        }
                                    )
                                    .border(
                                        width = if (isUnlocked) 3.5.dp else 2.dp,
                                        color = if (isUnlocked) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.35f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isUnlocked) {
                                    Text(
                                        text = "$lvl",
                                        color = Color(0xFF3A196E),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_lock),
                                        contentDescription = "Locked",
                                        tint = Color.White.copy(alpha = 0.65f),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Label Pill: "Up Next", "3 stars", or "Locked"
                            val labelText = when {
                                !isUnlocked -> "Locked"
                                isCurrent -> "Up Next"
                                stars > 0 -> "$stars stars"
                                lvl == 1 -> "First glow"
                                else -> "Ready"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF110439).copy(alpha = 0.82f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = labelText,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Big Primary Golden Pill Button: "✨ Continue to Level {latest} >"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(20.dp),
                        spotColor = Color(0xFFFFBC43).copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFE879), Color(0xFFFFBC43))
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onSelectLevel(latest) }
                    .padding(vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sparkles),
                        contentDescription = null,
                        tint = Color(0xFF291063),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue to Level $latest",
                        color = Color(0xFF291063),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.2.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = Color(0xFF291063),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row of Two Ghost Buttons: "🎁 Daily gift" & "⚙ Settings"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Daily Gift Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color.White.copy(alpha = if (giftClaimed) 0.06f else 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(15.dp))
                        .clickable {
                            if (!giftClaimed) {
                                progress.coins += 250
                                progress.dailyGiftClaimedOn = todayStr
                                onSaveProgress(progress)
                                notice = "250 shards claimed! Come back tomorrow."
                            } else {
                                notice = "You already claimed your gift today."
                            }
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_gift),
                            contentDescription = null,
                            tint = if (giftClaimed) Color.White.copy(alpha = 0.5f) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (giftClaimed) "Gift claimed" else "Daily gift",
                            color = if (giftClaimed) Color.White.copy(alpha = 0.5f) else Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Settings Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(15.dp))
                        .clickable { onSettings() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Settings",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Collection Bar: Crown + "Star trail" + "{stars} lit"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFD700).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_crown),
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Star trail",
                            color = Color.White,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "$totalStars lit",
                        color = Color(0xFFA5F3FC),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Animated Toast Notice
        AnimatedVisibility(
            visible = notice != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { 40 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(Color(0xFF1A0945).copy(alpha = 0.95f))
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(99.dp))
                    .shadow(16.dp, shape = RoundedCornerShape(99.dp))
                    .padding(horizontal = 20.dp, vertical = 11.dp)
            ) {
                Text(
                    text = notice ?: "",
                    color = Color.White,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
