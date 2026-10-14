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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.togglestudios.lumenpop.data.BoosterKind

@Composable
fun BoosterBar(
    coins: Int,
    onBoosterClick: (BoosterKind) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoosterButton(
            kind = BoosterKind.SHUFFLE,
            icon = "🔀",
            canAfford = coins >= BoosterKind.SHUFFLE.price,
            onClick = { onBoosterClick(BoosterKind.SHUFFLE) }
        )
        BoosterButton(
            kind = BoosterKind.BOMB,
            icon = "💣",
            canAfford = coins >= BoosterKind.BOMB.price,
            onClick = { onBoosterClick(BoosterKind.BOMB) }
        )
        BoosterButton(
            kind = BoosterKind.BURST,
            icon = "⚡",
            canAfford = coins >= BoosterKind.BURST.price,
            onClick = { onBoosterClick(BoosterKind.BURST) }
        )
    }
}

@Composable
private fun BoosterButton(
    kind: BoosterKind,
    icon: String,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (canAfford) Color(0xFF1B0B3B).copy(alpha = 0.85f)
                else Color(0xFF1B0B3B).copy(alpha = 0.45f)
            )
            .border(
                1.dp,
                if (canAfford) Color(0xFF00F0FF).copy(alpha = 0.4f)
                else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = canAfford) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = kind.title,
            color = if (canAfford) Color.White else Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        ) {
            Text(
                text = "🪙 ${kind.price}",
                color = if (canAfford) Color(0xFFFFD700) else Color(0xFFFFD700).copy(alpha = 0.4f),
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
