package com.togglestudios.lumenpop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.togglestudios.lumenpop.data.SavedProgress

@Composable
fun HomeScreen(
    progress: SavedProgress,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D0422), Color(0xFF1B0B3B), Color(0xFF12053C))
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
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
                    text = "SELECT LEVEL",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🪙 ${progress.coins}",
                        color = Color(0xFFFFD700),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(30) { index ->
                    val level = index + 1
                    val isUnlocked = level <= progress.highestUnlocked
                    val stats = progress.completed[level]
                    val stars = stats?.stars ?: 0

                    LevelCard(
                        level = level,
                        isUnlocked = isUnlocked,
                        stars = stars,
                        isCurrent = level == progress.highestUnlocked,
                        onClick = {
                            if (isUnlocked) onSelectLevel(level)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelCard(
    level: Int,
    isUnlocked: Boolean,
    stars: Int,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    isCurrent -> Color(0xFF00F0FF).copy(alpha = 0.25f)
                    isUnlocked -> Color(0xFF1B0B3B).copy(alpha = 0.9f)
                    else -> Color.Black.copy(alpha = 0.35f)
                }
            )
            .border(
                1.5.dp,
                when {
                    isCurrent -> Color(0xFF00F0FF)
                    isUnlocked -> Color.White.copy(alpha = 0.25f)
                    else -> Color.White.copy(alpha = 0.06f)
                },
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = isUnlocked) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isUnlocked) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "$level",
                    color = if (isCurrent) Color(0xFF00F0FF) else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                if (stars > 0) {
                    Text(
                        text = "★".repeat(stars),
                        color = Color(0xFFFFD700),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            Text(
                text = "🔒",
                fontSize = 14.sp
            )
        }
    }
}
