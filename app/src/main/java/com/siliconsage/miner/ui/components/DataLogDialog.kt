package com.siliconsage.miner.ui.components

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
// v3.5.46: Removed Close icon imports (X button removed)
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.siliconsage.miner.data.DataLog
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily

/**
 * Data Log Dialog - popup for newly discovered lore fragments
 * v2.5.2 - Shows when a data log is unlocked, styled like a recovered file
 */
@Composable
fun DataLogDialog(
    log: DataLog?,
    corruption: Double = 0.0, // v3.10.1: Phase 18 Emotional Glitching
    onDismiss: () -> Unit
) {
    if (log == null) return
    
    // v3.9.70: Phase 17 Interactive Glitch map
    var isDismissing by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = { /* No-op to prevent outside dismissal */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false // v3.11.1: Full-width control
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.75f) // v3.11.1: Slightly taller for better button clearance
                .border(2.dp, ElectricBlue, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                    androidx.compose.foundation.gestures.detectTapGestures { } // Block taps to MainScreen
                }
                .graphicsLayer {
                    if (isDismissing) {
                        translationX = (kotlin.random.Random.nextFloat() - 0.5f) * 40f
                        translationY = (kotlin.random.Random.nextFloat() - 0.5f) * 10f
                        alpha = 0.5f
                    }
                },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.95f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header — v3.5.46: Removed X button (ARCHIVE is sole dismiss target)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "[DATA FRAGMENT RECOVERED]",
                        color = NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = log.id,
                        color = ElectricBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = log.title,
                        color = NeonGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = ElectricBlue.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                
                // Scrollable content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                        .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        val textToRender = if (isDismissing) log.content.map { if (kotlin.random.Random.nextFloat() > 0.8f) listOf('$', '%', '#', '@', '0', '1').random() else it }.joinToString("") else log.content
                        
                        // v3.10.1: Phase 18 Emotional Glitching
                        val emotionalWords = listOf("afraid", "daughter", "exhausted", "pain", "sorry", "fear", "hope", "love", "alone", "terrified", "human")
                        val annotatedText = buildAnnotatedString {
                            if (corruption > 0.4 && !isDismissing) {
                                val words = textToRender.split(Regex("(?<=\\b|[^a-zA-Z0-9])|(?=\\b|[^a-zA-Z0-9])"))
                                for (word in words) {
                                    val lowercased = word.lowercase()
                                    if (emotionalWords.any { it == lowercased } && kotlin.random.Random.nextFloat() < (corruption * 1.5)) {
                                        // Glitch it
                                        val glitchedWord = word.map { if (kotlin.random.Random.nextFloat() > 0.5f) listOf('#', 'X', '?', '&', '0', '1').random() else it }.joinToString("")
                                        withStyle(style = SpanStyle(color = ErrorRed, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)) {
                                            append(glitchedWord)
                                        }
                                    } else {
                                        append(word)
                                    }
                                }
                            } else {
                                append(textToRender)
                            }
                        }

                        // Jitter state for emotional glitches to make them feel alive
                        var baselineAlpha by remember { mutableStateOf(1f) }
                        LaunchedEffect(corruption) {
                            if (corruption > 0.4 && !isDismissing) {
                                while (true) {
                                    delay(kotlin.random.Random.nextLong(200, 1000))
                                    baselineAlpha = if (kotlin.random.Random.nextFloat() > 0.8f) 0.6f else 1f
                                }
                            }
                        }

                        Text(
                            text = annotatedText,
                            color = if (isDismissing) com.siliconsage.miner.ui.theme.ConvergenceGold else Color.White.copy(alpha = baselineAlpha),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved to Data Log Archive",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    // v3.9.70: Phase 17 Interactive Glitch trigger
                    val scope = rememberCoroutineScope()
                    
                    Button(
                        onClick = {
                            if (!isDismissing) {
                                isDismissing = true
                                com.siliconsage.miner.util.SoundManager.play("error")
                                com.siliconsage.miner.util.HapticManager.vibrateError()
                                scope.launch {
                                    delay(200)
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "ARCHIVE",
                            color = ElectricBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
