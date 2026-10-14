package com.togglestudios.lumenpop.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.togglestudios.lumenpop.data.SpecialType
import com.togglestudios.lumenpop.data.Tile

@Composable
fun LumenTileView(
    tile: Tile,
    isSelected: Boolean,
    isRefilled: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.18f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "tileScale"
    )

    val assetName = when {
        tile.isFusion -> "fusion_orb.png"
        isSelected -> tile.color.openedAsset
        else -> tile.color.closedAsset
    }

    val imageBitmap = rememberAssetImage(assetName)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(tile.color.hexColor).copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(2.dp, Color(0xFF00F0FF), CircleShape)
            )
        }

        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = tile.color.colorName,
                modifier = Modifier
                    .fillMaxSize(if (isSelected) 0.95f else 0.88f)
                    .clip(RoundedCornerShape(8.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.8f)
                    .clip(CircleShape)
                    .background(Color(tile.color.hexColor))
            )
        }

        if (tile.special != SpecialType.NONE) {
            val badgeText = when (tile.special) {
                SpecialType.BEAM_H -> "↔"
                SpecialType.BEAM_V -> "↕"
                SpecialType.NOVA -> "★"
                SpecialType.CROSS -> "✚"
                SpecialType.NONE -> ""
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, Color(0xFFFFD700), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = Color(0xFFFFD700),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
