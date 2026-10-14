package com.togglestudios.lumenpop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.togglestudios.lumenpop.R
import com.togglestudios.lumenpop.data.BoosterKind

@Composable
fun BoosterBar(
    coins: Int,
    activeBooster: BoosterKind?,
    disabled: Boolean = false,
    onBoosterClick: (BoosterKind) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoosterCard(
            kind = BoosterKind.SHUFFLE,
            title = "Shuffle",
            iconRes = R.drawable.ic_shuffle,
            iconBgColor = Color(0xFFFFCF4E),
            canAfford = coins >= BoosterKind.SHUFFLE.price,
            isActive = activeBooster == BoosterKind.SHUFFLE,
            disabled = disabled,
            onClick = { onBoosterClick(BoosterKind.SHUFFLE) },
            modifier = Modifier.weight(1f)
        )
        BoosterCard(
            kind = BoosterKind.BOMB,
            title = "Bomb",
            iconRes = R.drawable.ic_bomb,
            iconBgColor = Color(0xFFFF7C9F),
            canAfford = coins >= BoosterKind.BOMB.price,
            isActive = activeBooster == BoosterKind.BOMB,
            disabled = disabled,
            onClick = { onBoosterClick(BoosterKind.BOMB) },
            modifier = Modifier.weight(1f)
        )
        BoosterCard(
            kind = BoosterKind.BURST,
            title = "Burst",
            iconRes = R.drawable.ic_burst,
            iconBgColor = Color(0xFF7CF4E3),
            canAfford = coins >= BoosterKind.BURST.price,
            isActive = activeBooster == BoosterKind.BURST,
            disabled = disabled,
            onClick = { onBoosterClick(BoosterKind.BURST) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BoosterCard(
    kind: BoosterKind,
    title: String,
    iconRes: Int,
    iconBgColor: Color,
    canAfford: Boolean,
    isActive: Boolean,
    disabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEnabled = !disabled && canAfford
    val borderColor = when {
        isActive -> Color(0xFFFFD700)
        isEnabled -> Color.White.copy(alpha = 0.2f)
        else -> Color.White.copy(alpha = 0.08f)
    }
    val bgColor = when {
        isActive -> Color(0xFFFFD700).copy(alpha = 0.18f)
        isEnabled -> Color.White.copy(alpha = 0.1f)
        else -> Color.White.copy(alpha = 0.05f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(if (isActive) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = isEnabled) { onClick() }
            .padding(horizontal = 6.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Colored circular / squircle icon badge
            Box(
                modifier = Modifier
                    .size(31.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isEnabled) iconBgColor else iconBgColor.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    tint = Color(0xFF23104C),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = title,
                    color = if (isEnabled) Color.White else Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${kind.price} shards",
                    color = if (isEnabled) Color.White.copy(alpha = 0.58f) else Color.White.copy(alpha = 0.3f),
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
