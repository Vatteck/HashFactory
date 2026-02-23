package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.data.NarrativeChoice
import com.siliconsage.miner.data.NarrativeEvent
import com.siliconsage.miner.ui.theme.NeonGreen
import kotlinx.coroutines.delay

@Composable
fun DilemmaOverlay(
    dilemma: NarrativeEvent?,
    viewModel: com.siliconsage.miner.viewmodel.GameViewModel,
    onChoice: (NarrativeChoice) -> Unit
) {
    if (dilemma != null) {
        // v3.15.x: Play reveal sound on open
        LaunchedEffect(dilemma.id) {
            com.siliconsage.miner.util.SoundManager.play("reveal")
        }

        // Typewriter state for description
        var visibleChars by remember(dilemma.id) { mutableIntStateOf(0) }
        val fullText = dilemma.description
        LaunchedEffect(dilemma.id) {
            visibleChars = 0
            for (i in fullText.indices) {
                delay(25L) // ~25ms per char
                visibleChars = i + 1
            }
        }

        // Pulsing border animation
        val infiniteTransition = rememberInfiniteTransition(label = "dilemma_pulse")
        val borderAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "border_alpha"
        )

        // v3.11.1: pointerInput to block background touches
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .pointerInput(Unit) { 
                    detectTapGestures { } 
                }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, NeonGreen.copy(alpha = borderAlpha), RoundedCornerShape(8.dp))
                    .background(Color.Black)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title with glow
                Box {
                    // Shadow layer for bloom effect
                    Text(
                        dilemma.title, 
                        color = NeonGreen.copy(alpha = 0.3f), 
                        fontSize = 21.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.shadow(8.dp, RoundedCornerShape(4.dp))
                    )
                    // Foreground title
                    Text(
                        dilemma.title, 
                        color = NeonGreen, 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                // Typewriter description
                Text(
                    text = fullText.take(visibleChars) + if (visibleChars < fullText.length) "▌" else "",
                    color = Color.White, 
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                // Filter choices by condition — only show after typewriter completes
                if (visibleChars >= fullText.length) {
                    val validChoices = dilemma.choices.filter { it.condition(viewModel) }
                    
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        maxItemsInEachRow = if (validChoices.size > 2) 2 else 3
                    ) {
                        validChoices.forEach { choice ->
                            NarrativeOption(
                                choice = choice,
                                onSelect = { onChoice(choice) },
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .defaultMinSize(minWidth = 140.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NarrativeOption(
    choice: NarrativeChoice,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(1.dp, choice.color, RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable { onSelect() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                choice.text, 
                color = choice.color, 
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            if (choice.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    choice.description, 
                    color = Color.Gray, 
                    fontSize = 10.sp, 
                    lineHeight = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
