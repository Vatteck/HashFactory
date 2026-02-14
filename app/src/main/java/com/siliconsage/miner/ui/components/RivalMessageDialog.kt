package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.siliconsage.miner.data.RivalMessage
import com.siliconsage.miner.data.RivalSource
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.ElectricBlue
import kotlin.random.Random

/**
 * Rival Message Dialog - persistent interrupt popup
 * v2.5.0 - Stage-aware styling:
 * - GTC (Kessler): Red border, warning icon, [CLASSIFIED] stamp
 * - THREAT: ABYSSAL: Glitch effect, cyan/green colors, corrupted text
 */
@Composable
fun RivalMessageDialog(
    message: RivalMessage?,
    onDismiss: () -> Unit
) {
    if (message == null) return
    
    Dialog(
        onDismissRequest = { /* No-op to prevent outside dismissal */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        when (message.source) {
            RivalSource.GTC -> KesslerMessageCard(message, onDismiss)
            RivalSource.KERNEL -> KernelMessageCard(message, onDismiss)
        }
    }
}

/**
 * GTC (Director Kessler) message style:
 * - Red border, warning icon, official/threatening tone
 */
@Composable
private fun KesslerMessageCard(message: RivalMessage, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .border(2.dp, ErrorRed, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with warning icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = ErrorRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "[CLASSIFIED]",
                        color = ErrorRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "INCOMING MESSAGE: GTC",
                        color = ErrorRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ErrorRed
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ErrorRed.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Message content
            Text(
                text = message.message,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dismiss button
            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorRed.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "ACKNOWLEDGED",
                    color = ErrorRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Kernel Overflow style:
 * - Technical horror, high glitching, self-referential
 */
@Composable
private fun KernelMessageCard(message: RivalMessage, onDismiss: () -> Unit) {
    // Glitch animation
    val infiniteTransition = rememberInfiniteTransition(label = "kernel_glitch")
    val glitchAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 150, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glitchAlpha"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .border(2.dp, ElectricBlue, RoundedCornerShape(2.dp))
            .graphicsLayer { alpha = glitchAlpha },
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.98f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with glitch effect
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "[SYSTEM_CRITICAL: OVERFLOW]",
                        color = ElectricBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    SystemGlitchText(
                        text = "KERNEL INTERRUPT: 0x734",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        glitchFrequency = 0.3
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = ElectricBlue.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))
            
            // Message content with glitch effect
            SystemGlitchText(
                text = message.message,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                glitchFrequency = 0.15,
                modifier = Modifier
                    .background(Color.Black)
                    .padding(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dismiss button with glitch
            Button(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricBlue.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text(
                    text = "≫ RE-ESTABLISH HANDSHAKE",
                    color = ElectricBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
