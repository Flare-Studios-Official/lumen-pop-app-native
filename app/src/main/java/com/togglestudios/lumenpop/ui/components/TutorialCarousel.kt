package com.togglestudios.lumenpop.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TutorialCard(
    val title: String,
    val description: String,
    val image: String
)

@Composable
fun TutorialCarousel(
    onComplete: () -> Unit
) {
    val cards = remember {
        listOf(
            TutorialCard(
                title = "How to Play",
                description = "Drag to link at least 3 Lumens horizontally, vertically, or diagonally.",
                image = "solar_opened.png"
            ),
            TutorialCard(
                title = "4-Link: Beam Blast",
                description = "Match 4 Lumens to release a Beam Blast that clears an entire row or column.",
                image = "nova_opened.png"
            ),
            TutorialCard(
                title = "5-Link: Nova Bomb",
                description = "Match 5 Lumens to forge a Nova Bomb that blasts a 3x3 pocket of the board.",
                image = "blaze_opened.png"
            ),
            TutorialCard(
                title = "6-Link: Cross Blast",
                description = "Match 6 Lumens to unleash a Cross Blast, clearing both a row and a column.",
                image = "verdant_opened.png"
            ),
            TutorialCard(
                title = "7+ Link: Vortex",
                description = "Match 7 or more Lumens to summon a Vortex. It sweeps away all Lumens of that color!",
                image = "aether_opened.png"
            ),
            TutorialCard(
                title = "Prism Vortex",
                description = "A rare Prism Vortex has a 3% chance to spawn! Tap it to absorb all nearby Lumens.",
                image = "fusion_orb.png"
            ),
            TutorialCard(
                title = "Target Glow",
                description = "Reach the target glow score before running out of moves to win the level.",
                image = "terra_opened.png"
            )
        )
    }

    var slideIndex by remember { mutableIntStateOf(0) }
    val currentCard = cards[slideIndex]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.82f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF12053C))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = currentCard,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "slideContent"
                ) { card ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val img = rememberAssetImage(card.image)
                        if (img != null) {
                            Image(
                                bitmap = img,
                                contentDescription = card.title,
                                modifier = Modifier
                                    .size(130.dp)
                                    .padding(8.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF00F0FF).copy(alpha = 0.2f))
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = card.title,
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = card.description,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .height(56.dp)
                                .padding(horizontal = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bottom bar: dots and next button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pagination Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        cards.forEachIndexed { idx, _ ->
                            val isSelected = idx == slideIndex
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(if (isSelected) 20.dp else 6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        if (isSelected) Color(0xFF00F0FF) else Color.White.copy(alpha = 0.25f)
                                    )
                            )
                        }
                    }

                    // Next / Play Button
                    Button(
                        onClick = {
                            if (slideIndex < cards.size - 1) {
                                slideIndex++
                            } else {
                                onComplete()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (slideIndex < cards.size - 1) "Next →" else "Play ✨",
                            color = Color(0xFF0D0422),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
