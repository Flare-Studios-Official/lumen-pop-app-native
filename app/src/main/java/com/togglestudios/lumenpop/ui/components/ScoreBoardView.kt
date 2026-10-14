package com.togglestudios.lumenpop.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.togglestudios.lumenpop.R
import com.togglestudios.lumenpop.data.LevelConfig

@Composable
fun GameHud(
    levelConfig: LevelConfig,
    score: Int,
    moves: Int,
    lessonOrBoosterText: String,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onPause: () -> Unit
) {
    val progressPercent = ((score.toFloat() / levelConfig.targetScore) * 100).coerceIn(0f, 100f)
    val displayPercent = progressPercent.toInt()

    // Shimmer sweep animation for the progress bar
    val infiniteTransition = rememberInfiniteTransition(label = "hud_shimmer")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 280f,
        animationSpec = infiniteRepeatable(
            animation = tween(1700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // Topbar: ←, World Name, ⚙ Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.11f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }

            // World Name
            Text(
                text = levelConfig.world.uppercase(),
                color = Color.White.copy(alpha = 0.64f),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            // Settings Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.11f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                    .clickable { onSettings() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        // Level Heading: Level title on left, Pause button on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Level ${levelConfig.level}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "  ·  ",
                        color = Color(0xFFA5F3FC),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = levelConfig.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lessonOrBoosterText,
                    color = Color.White.copy(alpha = 0.62f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            // Pause Button: ❚❚
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Color.White.copy(alpha = 0.13f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(13.dp))
                    .clickable { onPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pause),
                    contentDescription = "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Stats Row: Target Glow progress bar + Moves circle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Target Glow Progress Column
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sparkles),
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "TARGET GLOW",
                            color = Color(0xFFA5F3FC),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Text(
                        text = "$displayPercent%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                // Glowing Animated Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(Color(0xFF0A0320).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.4f), RoundedCornerShape(99.dp))
                        .padding(2.dp)
                ) {
                    val fillFraction = (progressPercent / 100f).coerceIn(0.04f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fillFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(99.dp))
                            .background(
                                if (progressPercent >= 100f) {
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFFFFE066),
                                            Color(0xFFFF70A6),
                                            Color(0xFFFF007F),
                                            Color(0xFF70D6FF)
                                        )
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF00F2FE),
                                            Color(0xFF4FACFE),
                                            Color(0xFF00F0FF),
                                            Color(0xFFFFE259)
                                        )
                                    )
                                }
                            )
                    ) {
                        // Shimmer highlight sweep
                        Box(
                            modifier = Modifier
                                .offset(x = shimmerOffset.dp)
                                .fillMaxHeight()
                                .width(55.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color.White.copy(alpha = 0.75f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                }
            }

            // Circular Glowing Cyan Moves Badge
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(14.dp, CircleShape, spotColor = Color(0xFF00F0FF).copy(alpha = 0.5f))
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF210959), Color(0xFF0F042D))
                        )
                    )
                    .border(3.dp, Color(0xFF00F0FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MOVES",
                        color = Color(0xFFA5F3FC),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$moves",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
