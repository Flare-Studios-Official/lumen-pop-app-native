package com.togglestudios.lumenpop.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.togglestudios.lumenpop.R
import java.text.NumberFormat
import java.util.Locale

enum class OverlayType {
    COMPLETE,
    FAIL,
    PAUSE
}

@Composable
fun ResultOverlay(
    type: OverlayType,
    score: Int,
    targetScore: Int,
    stars: Int,
    onPrimary: () -> Unit,
    onSecondary: (() -> Unit)? = null,
    onClose: () -> Unit
) {
    val won = type == OverlayType.COMPLETE

    val headingText = when (type) {
        OverlayType.COMPLETE -> "Glow-getter!"
        OverlayType.PAUSE -> "Glow paused"
        OverlayType.FAIL -> "The Lumens went shy"
    }

    val descText = when (type) {
        OverlayType.COMPLETE -> "That chain lit up the whole meadow. Your constellation is shining."
        OverlayType.PAUSE -> "Your board is waiting exactly where you left it."
        OverlayType.FAIL -> "Every little light gets another chance. Try a shorter, brighter chain."
    }

    val formattedScore = NumberFormat.getNumberInstance(Locale.US).format(score)
    val formattedTarget = NumberFormat.getNumberInstance(Locale.US).format(targetScore)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D032A).copy(alpha = 0.72f))
            .clickable { onClose() },
        contentAlignment = Alignment.Center
    ) {
        // Modal Card
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clickable(enabled = false) {}
                .clip(RoundedCornerShape(30.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFF45228F).copy(alpha = 0.96f),
                            Color(0xFF1B0A4E).copy(alpha = 0.98f)
                        )
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(30.dp))
                .shadow(32.dp, shape = RoundedCornerShape(30.dp))
                .padding(26.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Success / Pause / Retry Orb Hero
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .rotate(8f)
                        .shadow(20.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFFFFD700).copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            if (won) {
                                Brush.linearGradient(
                                    listOf(Color(0xFFFFF58D), Color(0xFFFFB53A))
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFF4C1D95))
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val iconRes = when (type) {
                        OverlayType.COMPLETE -> R.drawable.ic_sparkles
                        OverlayType.PAUSE -> R.drawable.ic_pause
                        OverlayType.FAIL -> R.drawable.ic_shuffle
                    }
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = if (won) Color(0xFF582A9E) else Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = headingText,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = descText,
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // If Win: Result row with Score pill and Stars pill
                if (won) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Score Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formattedScore,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = "OF $formattedTarget",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Stars Pill
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                    repeat(3) { idx ->
                                        val active = idx < stars
                                        Text(
                                            text = "★",
                                            color = if (active) Color(0xFFFFDC63) else Color.White.copy(alpha = 0.25f),
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                                Text(
                                    text = "STARS",
                                    color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Primary Golden Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFFFBC43).copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFE879), Color(0xFFFFBC43))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .clickable { onPrimary() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when (type) {
                                OverlayType.COMPLETE -> "Next level"
                                OverlayType.PAUSE -> "Resume glow"
                                OverlayType.FAIL -> "Try again"
                            },
                            color = Color(0xFF291063),
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            painter = painterResource(
                                id = when (type) {
                                    OverlayType.COMPLETE -> R.drawable.ic_chevron_right
                                    OverlayType.PAUSE -> R.drawable.ic_play
                                    OverlayType.FAIL -> R.drawable.ic_shuffle
                                }
                            ),
                            contentDescription = null,
                            tint = Color(0xFF291063),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Secondary Ghost Button (Replay / Leave level)
                if (onSecondary != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color.White.copy(alpha = 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(15.dp))
                            .clickable { onSecondary() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (won) "Replay level" else "Leave level",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tertiary Ghost Button
                Text(
                    text = when (type) {
                        OverlayType.COMPLETE -> "Keep exploring"
                        OverlayType.PAUSE -> "Close pause"
                        OverlayType.FAIL -> "Keep playing"
                    },
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onClose() }
                )
            }
        }
    }
}
