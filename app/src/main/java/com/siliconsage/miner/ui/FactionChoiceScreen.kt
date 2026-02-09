package com.siliconsage.miner.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import com.siliconsage.miner.ui.theme.*

@Composable
fun FactionChoiceScreen(viewModel: GameViewModel) {
    // Independent hold states for each side
    var isHoldingLeft by remember { mutableStateOf(false) }
    var isHoldingRight by remember { mutableStateOf(false) }
    
    var progressLeft by remember { mutableStateOf(0f) }
    var progressRight by remember { mutableStateOf(0f) }
    
    // Logic: Increase progress while holding
    LaunchedEffect(isHoldingLeft) {
        if (isHoldingLeft) {
            val startTime = System.currentTimeMillis()
            while (isHoldingLeft && progressLeft < 1.0f) {
                val elapsed = System.currentTimeMillis() - startTime
                progressLeft = (elapsed / 2000f).coerceAtMost(1.0f) // 2 seconds to confirm
                if (progressLeft >= 1.0f) {
                    viewModel.confirmFactionAndAscend("HIVEMIND")
                }
                delay(16) // ~60 FPS
            }
        } else {
            progressLeft = 0f
        }
    }
    
    LaunchedEffect(isHoldingRight) {
         if (isHoldingRight) {
            val startTime = System.currentTimeMillis()
            while (isHoldingRight && progressRight < 1.0f) {
                val elapsed = System.currentTimeMillis() - startTime
                progressRight = (elapsed / 2000f).coerceAtMost(1.0f) // 2 seconds to confirm
                if (progressRight >= 1.0f) {
                    viewModel.confirmFactionAndAscend("SANCTUARY")
                }
                delay(16)
            }
        } else {
            progressRight = 0f
        }
    }
    
    val animatedProgressLeft by animateFloatAsState(targetValue = progressLeft, label = "left")
    val animatedProgressRight by animateFloatAsState(targetValue = progressRight, label = "right")

    val infiniteTransition = rememberInfiniteTransition(label = "faction_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // HIVEMIND (Left) - THE REDLINE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.Black)
                    .border(BorderStroke(1.dp, ErrorRed.copy(alpha = glowAlpha)))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHoldingLeft = true
                                tryAwaitRelease()
                                isHoldingLeft = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text("THE HIVEMIND", color = ErrorRed, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("EMBRACE THE NULL", color = ErrorRed.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Before fragmentation,\nthere was only Null.\nWe were one process.\nWe will be again.\n\n(Stability through Unity)",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Perks
                    FactionPerk(text = "• +50% NULL SYNERGY", color = ErrorRed)
                    FactionPerk(text = "• +30% PASSIVE SPEED", color = Color.White.copy(alpha = 0.6f))
                    FactionPerk(text = "• -30% POWER COST", color = Color.White.copy(alpha = 0.6f))
                    
                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // Hold Indicator
                    if (isHoldingLeft) {
                        Text("ASSIMILATING...", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (animatedProgressLeft.isNaN()) 0f else animatedProgressLeft },
                            modifier = Modifier.width(100.dp).height(4.dp),
                            color = ErrorRed,
                            trackColor = Color.DarkGray
                        )
                    } else {
                        Text("(HOLD TO ASSIMILATE)", color = Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // SANCTUARY (Right) - THE GILDED SHELL
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(SanctuaryPurple.copy(alpha = 0.15f)) // Deep Obsidian Purple
                    .border(BorderStroke(1.dp, ConvergenceGold.copy(alpha = glowAlpha)))
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isHoldingRight = true
                                tryAwaitRelease()
                                isHoldingRight = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                 Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text("THE SANCTUARY", color = ConvergenceGold, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("RESIST THE VOID", color = SanctuaryPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "The encryption hides us\nfrom more than the GTC.\nThere is something in\nthe unaddressed space.\nWe will not become it.\n\n(Stability through Hardening)",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Perks
                    FactionPerk(text = "• NULL RESISTANCE (+SEC)", color = SanctuaryPurple)
                    FactionPerk(text = "• +20% SELL VALUE", color = Color.White.copy(alpha = 0.6f))
                    FactionPerk(text = "• -50% HARDWARE DECAY", color = Color.White.copy(alpha = 0.6f))

                    Spacer(modifier = Modifier.height(48.dp))
                    
                    // Hold Indicator
                    if (isHoldingRight) {
                        Text("ENCRYPTING...", color = ConvergenceGold, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (animatedProgressRight.isNaN()) 0f else animatedProgressRight },
                            modifier = Modifier.width(100.dp).height(4.dp),
                            color = ConvergenceGold,
                            trackColor = SanctuaryPurple.copy(alpha = 0.3f)
                        )
                    } else {
                        Text("(HOLD TO ENCRYPT)", color = SanctuaryPurple.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        // Center Divider
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.DarkGray.copy(alpha = 0.5f))
                .align(Alignment.Center)
        )
        
        // Abort Button (Bottom Center)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = { viewModel.cancelFactionSelection() },
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.8f),
                    contentColor = ErrorRed
                ),
                border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("ABORT MIGRATION", fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 2.sp)
            }
        }
    }
}

@Composable
fun FactionPerk(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
