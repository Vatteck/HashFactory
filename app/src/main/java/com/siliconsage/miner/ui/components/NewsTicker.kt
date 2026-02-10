package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.util.SoundManager
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextOverflow
import com.siliconsage.miner.ui.components.SystemGlitchText

@Composable
fun NewsTicker(
    news: String,
    modifier: Modifier = Modifier
) {
    // v3.1.8-fix: Use internal state to prevent mid-scroll jump when news flow updates
    var currentNewsText by remember { mutableStateOf(news) }
    var tickerKey by remember { mutableIntStateOf(0) }
    
    // key(tickerKey) forces the entire transition and content to restart when news changes
    key(tickerKey) {
        val infiniteTransition = rememberInfiniteTransition(label = "ticker")
        
        // Parse the news to get color and clean text
        val (displayText, baseColor) = remember(currentNewsText) {
            var text = currentNewsText
            var color = NeonGreen // Default

            when {
                text.contains("[BULL]") -> {
                    text = text.replace("[BULL]", "").trim()
                    color = NeonGreen
                }
                text.contains("[BEAR]") -> {
                    text = text.replace("[BEAR]", "").trim()
                    color = Color(0xFFFFA500) // Orange
                }
                text.contains("[ENERGY_SPIKE]") -> {
                    text = text.replace("[ENERGY_SPIKE]", "").trim()
                    color = ErrorRed
                }
                text.contains("[ENERGY_DROP]") -> {
                    text = text.replace("[ENERGY_DROP]", "").trim()
                    color = ElectricBlue
                }
                text.contains("[GLITCH]") -> {
                    text = text.replace("[GLITCH]", "").trim()
                    color = ElectricBlue // Placeholder, overridden by effect
                }
                text.contains("[STORY_PROG]") -> {
                    text = text.replace("[STORY_PROG]", "").trim()
                    color = Color(0xFFFFD700) // Gold
                }
                text.contains("[LORE]") -> {
                    text = text.replace("[LORE]", "").trim()
                    color = Color.White
                }
                else -> {
                    text = text.replace(Regex("\\[.*?\\]"), "").trim()
                    color = Color.LightGray 
                }
            }
            text to color
        }

        // Dynamic range based on clean text length
        val textLength = displayText.length
        val estimatedWidth = textLength * 8f + 200f // Rough estimate in dp
        
        val offsetX by infiniteTransition.animateFloat(
            initialValue = 500f, // Start from right side
            targetValue = -estimatedWidth, // End way off-screen left
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (estimatedWidth * 25).toInt().coerceAtLeast(8000), 
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "tickerOffset"
        )

        val density = LocalDensity.current

        androidx.compose.runtime.LaunchedEffect(currentNewsText) {
            if (currentNewsText.isNotEmpty()) {
                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < 800) { 
                    SoundManager.play("type")
                    delay(250) 
                }
            }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(20.dp) 
                .background(Color.Black.copy(alpha = 0.3f)) 
                .clipToBounds()
        ) {
            if (displayText.isNotEmpty()) {
                val isGlitch = currentNewsText.contains("[GLITCH]")
                val finalColor = if (isGlitch) Color(0xFFFFD700).copy(alpha = 0.9f) else baseColor.copy(alpha = 0.9f)
                
                if (isGlitch) {
                     SystemGlitchText(
                        text = ">>> MARKET UPDATE: $displayText <<<",
                        color = finalColor,
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold,
                        softWrap = false,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Visible,
                        glitchFrequency = 0.25, 
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .graphicsLayer { translationX = with(density) { offsetX.dp.toPx() } }
                    )
                } else {
                    Text(
                        text = ">>> MARKET UPDATE: $displayText <<<",
                        color = finalColor,
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Normal,
                        softWrap = false,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Visible,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .graphicsLayer { translationX = with(density) { offsetX.dp.toPx() } }
                    )
                }
            }
        }
    }

    // v3.1.8-fix: Only update local news state after a full scroll cycle completes
    LaunchedEffect(news) {
        if (news != currentNewsText) {
            if (currentNewsText.isEmpty()) {
                currentNewsText = news
            } else {
                val duration = ((currentNewsText.length * 8f + 200f) * 25).toInt().coerceAtLeast(8000)
                delay(duration.toLong()) 
                currentNewsText = news
                tickerKey++ 
            }
        }
    }
}
