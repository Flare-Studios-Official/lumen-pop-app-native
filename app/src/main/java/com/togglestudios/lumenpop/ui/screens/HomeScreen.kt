package com.togglestudios.lumenpop.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.togglestudios.lumenpop.data.SavedProgress
import com.togglestudios.lumenpop.ui.components.rememberAssetImage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    progress: SavedProgress,
    onSelectLevel: (Int) -> Unit,
    onSettings: () -> Unit,
    onSaveProgress: (SavedProgress) -> Unit,
    onBack: () -> Unit
) {
    val latest = progress.highestUnlocked
    val biomeIndex = ((latest - 1) % 10) + 1
    val bgBitmap = rememberAssetImage("bg_level_$biomeIndex.png")

    val mapLevels = remember(latest) {
        (1..maxOf(10, minOf(30, latest + 5))).toList()
    }
    val mapRows = (mapLevels.size + 3) / 4

    var toastNotice by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(toastNotice) {
        if (toastNotice != null) {
            delay(2200)
            toastNotice = null
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

    val totalStars = remember(progress.completed) {
        progress.completed.values.sumOf { it.stars }
    }

    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val rowHeightDp = 105.dp
    val mapHeightDp = (mapRows * 105 + 60).dp

    Box(modifier = Modifier.fillMaxSize()) {
        // Biome Background
        if (bgBitmap != null) {
            Image(
                bitmap = bgBitmap,
                contentDescription = "Biome Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.32f
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0D0422), Color(0xFF1B0B3B), Color(0xFF12053C))
                        )
                    )
            )
        }

        // Vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0D0422).copy(alpha = 0.65f), Color(0xFF12053C).copy(alpha = 0.85f))
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Topbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "←", color = Color.White, fontSize = 20.sp)
                }

                Text(
                    text = "LEVEL TRAIL",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "🪙 ${progress.coins}",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Welcome card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "THE FIRST SPARK",
                            color = Color(0xFF00F0FF),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$greeting,\n${progress.username}.",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 26.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "The Lumens are humming your name.",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Winding Map Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mapHeightDp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF1B0B3B).copy(alpha = 0.8f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                ) {
                    // Winding Path Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path()
                        val w = size.width
                        val paddingX = 40.dp.toPx()
                        val usableW = w - paddingX * 2
                        val slotW = usableW / 3f

                        mapLevels.forEachIndexed { index, level ->
                            val row = (level - 1) / 4
                            val slot = (level - 1) % 4
                            val order = if (row % 2 == 0) slot else 3 - slot
                            val cx = paddingX + order * slotW
                            val cy = 40.dp.toPx() + row * rowHeightDp.toPx()

                            if (index == 0) path.moveTo(cx, cy) else path.lineTo(cx, cy)
                        }

                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = 0.18f),
                            style = Stroke(
                                width = 4.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 16f), 0f)
                            )
                        )
                    }

                    // Level Nodes Overlay
                    mapLevels.forEach { level ->
                        val unlocked = level <= latest
                        val isCurrent = level == latest
                        val stars = progress.completed[level]?.stars ?: 0

                        val row = (level - 1) / 4
                        val slot = (level - 1) % 4
                        val order = if (row % 2 == 0) slot else 3 - slot

                        // Responsive column positioning
                        val colPercent = 0.08f + order * 0.26f
                        val topOffset = 20.dp + (row * 105).dp

                        Column(
                            modifier = Modifier
                                .offset(x = screenWidth * colPercent, y = topOffset)
                                .clickable {
                                    if (unlocked) {
                                        onSelectLevel(level)
                                    } else {
                                        toastNotice = "Clear earlier levels to reach this meadow"
                                    }
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Orb
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> Brush.radialGradient(listOf(Color(0xFF00F0FF), Color(0xFF0080FF)))
                                            unlocked -> Brush.radialGradient(listOf(Color(0xFF381B74), Color(0xFF220C4D)))
                                            else -> Brush.radialGradient(listOf(Color(0xFF1B0B3B).copy(alpha = 0.7f), Color.Black))
                                        }
                                    )
                                    .border(
                                        2.dp,
                                        when {
                                            isCurrent -> Color(0xFFFFD700)
                                            unlocked -> Color(0xFF00F0FF).copy(alpha = 0.5f)
                                            else -> Color.White.copy(alpha = 0.15f)
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (unlocked) {
                                    Text(
                                        text = "$level",
                                        color = if (isCurrent) Color(0xFF0D0422) else Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                } else {
                                    Text(text = "🔒", fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            // Label below node
                            Text(
                                text = when {
                                    isCurrent -> "Up Next"
                                    unlocked -> if (stars > 0) "★".repeat(stars) else "Lit"
                                    else -> "Locked"
                                },
                                color = when {
                                    isCurrent -> Color(0xFFFFD700)
                                    unlocked -> Color(0xFF00F0FF).copy(alpha = 0.8f)
                                    else -> Color.White.copy(alpha = 0.4f)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Continue Button
                Button(
                    onClick = { onSelectLevel(latest) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF))
                ) {
                    Text(
                        text = "✨ Continue to Level $latest →",
                        color = Color(0xFF0D0422),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary row: Daily gift and Settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (!giftClaimed) {
                                progress.coins += 250
                                progress.dailyGiftClaimedOn = todayStr
                                onSaveProgress(progress)
                                toastNotice = "250 shards claimed! Come back tomorrow."
                            } else {
                                toastNotice = "You already claimed your gift today."
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (giftClaimed) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.16f)
                        )
                    ) {
                        Text(
                            text = if (giftClaimed) "✓ Gift Claimed" else "🎁 Daily Gift",
                            color = if (giftClaimed) Color.White.copy(alpha = 0.5f) else Color(0xFFFFD700),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onSettings,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.14f))
                    ) {
                        Text(
                            text = "⚙ Settings",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Star Trail collection counter
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "👑", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Star Trail",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "$totalStars lit",
                            color = Color(0xFFFFD700),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // Floating Toast
        AnimatedVisibility(
            visible = toastNotice != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1B0B3B).copy(alpha = 0.95f))
                    .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = toastNotice ?: "",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
