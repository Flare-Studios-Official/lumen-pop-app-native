package com.togglestudios.lumenpop.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun SettingsScreen(
    progress: SavedProgress,
    onSaveProgress: (SavedProgress) -> Unit,
    onResetProgress: () -> Unit,
    onBack: () -> Unit
) {
    var music by remember { mutableStateOf(progress.music) }
    var sound by remember { mutableStateOf(progress.sound) }
    var haptics by remember { mutableStateOf(progress.haptics) }
    var highContrast by remember { mutableStateOf(progress.highContrast) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0D0422), Color(0xFF1B0B3B), Color(0xFF12053C))
                )
            )
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
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
                    text = "SETTINGS",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = 16.dp),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF1B0B3B).copy(alpha = 0.9f))
                    .padding(16.dp)
            ) {
                SettingToggleRow(
                    label = "Music",
                    checked = music,
                    onCheckedChange = {
                        music = it
                        progress.music = it
                        onSaveProgress(progress)
                    }
                )

                SettingToggleRow(
                    label = "Sound Effects",
                    checked = sound,
                    onCheckedChange = {
                        sound = it
                        progress.sound = it
                        onSaveProgress(progress)
                    }
                )

                SettingToggleRow(
                    label = "Haptic Feedback",
                    checked = haptics,
                    onCheckedChange = {
                        haptics = it
                        progress.haptics = it
                        onSaveProgress(progress)
                    }
                )

                SettingToggleRow(
                    label = "High Contrast Mode",
                    checked = highContrast,
                    onCheckedChange = {
                        highContrast = it
                        progress.highContrast = it
                        onSaveProgress(progress)
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            OutlinedButton(
                onClick = onResetProgress,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "RESET PROGRESS",
                    color = Color(0xFFFF4500),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF00F0FF),
                checkedTrackColor = Color(0xFF00F0FF).copy(alpha = 0.4f),
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.15f)
            )
        )
    }
}
