package com.togglestudios.lumenpop.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.togglestudios.lumenpop.R
import com.togglestudios.lumenpop.data.SavedProgress
import com.togglestudios.lumenpop.ui.components.rememberAssetImage

@Composable
fun StartScreen(
    @Suppress("UNUSED_PARAMETER") progress: SavedProgress,
    onStart: () -> Unit
) {
    val logoBitmap = rememberAssetImage("lumen-pop-logo.png")

    // Ambient floating animation for logo hero
    val infiniteTransition = rememberInfiniteTransition(label = "hero_anim")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_float"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E0730))
    ) {
        // Deep cosmic radial backgrounds matching .game-shell::before
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF10052F),
                            Color(0xFF21105F),
                            Color(0xFF100934)
                        )
                    )
                )
        )

        // Soft ambient decorative glowing blobs
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 30.dp, y = 80.dp)
                .clip(CircleShape)
                .background(Color(0xFF00F0FF).copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-20).dp, y = (-60).dp)
                .clip(CircleShape)
                .background(Color(0xFFFF8CCC).copy(alpha = 0.07f))
        )

        // Background floating cosmic sparkles
        CosmicSparkles()

        // Main Center Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Hero Logo with rounded squircle and soft glow
            Box(
                modifier = Modifier
                    .offset(y = floatOffset.dp)
                    .size(190.dp)
                    .shadow(
                        elevation = 28.dp,
                        shape = RoundedCornerShape(48.dp),
                        spotColor = Color(0xFFFFD700).copy(alpha = 0.45f),
                        ambientColor = Color(0xFF00F0FF).copy(alpha = 0.35f)
                    )
                    .clip(RoundedCornerShape(48.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.5f),
                                Color(0xFFFFD700).copy(alpha = 0.3f),
                                Color(0xFF00F0FF).copy(alpha = 0.2f)
                            )
                        ),
                        shape = RoundedCornerShape(48.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (logoBitmap != null) {
                    Image(
                        bitmap = logoBitmap,
                        contentDescription = "Lumen Pop Star",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Eyebrow
            Text(
                text = "A POCKET ADVENTURE OF TINY LIGHTS",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Main Title
            Text(
                text = "Wake the wonder.",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.2).sp,
                lineHeight = 40.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Pop the light.",
                color = Color(0xFFA5F3FC),
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.2).sp,
                lineHeight = 40.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Description
            Text(
                text = "Link three or more Lumens by touch. Every chain wakes a little more of the world.",
                color = Color.White.copy(alpha = 0.68f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(34.dp))

            // Golden Gradient Button: "▶ Begin the journey >"
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = Color(0xFFFFBC43).copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFFE879), Color(0xFFFFBC43))
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .clickable { onStart() }
                    .padding(horizontal = 30.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_play),
                        contentDescription = null,
                        tint = Color(0xFF291063),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Begin the journey",
                        color = Color(0xFF291063),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = Color(0xFF291063),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Footer subtext
            Text(
                text = "No rush. Just little bursts of magic.",
                color = Color.White.copy(alpha = 0.42f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.3.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CosmicSparkles() {
    val infiniteTransition = rememberInfiniteTransition(label = "sparkles")
    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sp1"
    )
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.15f,
        animationSpec = infiniteRepeatable(tween(2400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sp2"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "✦",
            color = Color(0xFFFFFB8F).copy(alpha = alpha1),
            fontSize = 14.sp,
            modifier = Modifier.offset(x = 45.dp, y = 110.dp)
        )
        Text(
            text = "★",
            color = Color(0xFFFFFB8F).copy(alpha = alpha2),
            fontSize = 16.sp,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-55).dp, y = 170.dp)
        )
        Text(
            text = "✦",
            color = Color(0xFFFFFB8F).copy(alpha = alpha2),
            fontSize = 13.sp,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 30.dp, y = 40.dp)
        )
        Text(
            text = "★",
            color = Color(0xFFFFFB8F).copy(alpha = alpha1),
            fontSize = 15.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-40).dp, y = (-120).dp)
        )
    }
}
