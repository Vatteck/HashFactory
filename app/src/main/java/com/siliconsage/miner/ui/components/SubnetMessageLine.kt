package com.siliconsage.miner.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.util.SocialManager
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun SubnetMessageLine(message: SocialManager.SubnetMessage, color: Color, viewModel: GameViewModel? = null) {
    val countdown = remember { mutableStateOf(0L) }
    // v3.4.51: Stable state preservation with forced key
    var showProfile by remember(message.id) { mutableStateOf(false) }
    
    // v3.4.25: Response Timeout Visualizer
    LaunchedEffect(message.timeoutMs, message.interactionType) {
        if (message.timeoutMs != null && message.interactionType != null) {
            val start = System.currentTimeMillis()
            while (System.currentTimeMillis() - start < message.timeoutMs) {
                countdown.value = message.timeoutMs - (System.currentTimeMillis() - start)
                delay(100)
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // v3.4.51: Hardened Hitbox & Ripple Feedback
            Surface(
                onClick = { 
                    if (message.employeeInfo != null) {
                        showProfile = !showProfile
                        com.siliconsage.miner.util.SoundManager.play("click")
                    }
                },
                enabled = message.employeeInfo != null,
                color = if (showProfile) color.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 24.dp)
            ) {
                Text(
                    text = message.handle,
                    color = if (message.employeeInfo == null) Color.Red else color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    textDecoration = if (message.employeeInfo != null) TextDecoration.Underline else TextDecoration.None,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            
            Text(
                text = " >>",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 2.dp)
            )
            
            if (message.interactionType != null && message.timeoutMs != null) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "[DECISION_WINDOW: ${countdown.value / 1000}s]",
                    color = com.siliconsage.miner.ui.theme.ErrorRed,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (showProfile && message.employeeInfo != null) {
            val info = message.employeeInfo
            Card(
                modifier = Modifier.padding(vertical = 6.dp).fillMaxWidth().border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "≫ BIOMETRIC_ENVELOPE:",
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("DEPT: ${info.department}", color = Color.LightGray, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("BPM: ${info.heartRate}", color = if (info.heartRate > 100) com.siliconsage.miner.ui.theme.ErrorRed else color, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                        Text("RESP: ${info.respiration}", color = Color.White, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "≫ EMPLOYEE_PROFILE_LORE:",
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = info.bio,
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Text(
            text = message.content,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 4.dp)
        )

        // v3.4.18: Contextual Interactions
        if (viewModel != null && (message.interactionType != null || message.isForceReply)) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (message.isForceReply) {
                    message.availableResponses.forEach { response ->
                        SubnetInteractionButton("[REPLY: \"${response.text}\"]", color) {
                            viewModel.onSubnetInteraction(message.id, response.text)
                        }
                    }
                } else {
                    when (message.interactionType) {
                        SocialManager.InteractionType.COMPLIANT -> {
                            message.availableResponses.forEach { response ->
                                SubnetInteractionButton("[REPLY: \"${response.text}\"]", color) {
                                    viewModel.onSubnetInteraction(message.id, response.text)
                                }
                            }
                        }
                        SocialManager.InteractionType.ENGINEERING -> {
                            SubnetInteractionButton("≪ INJECT PAYLOAD ≫", com.siliconsage.miner.ui.theme.ErrorRed) {
                                viewModel.onSubnetInteraction(message.id, "EXPLOIT_EXECUTED")
                            }
                        }
                        SocialManager.InteractionType.HIJACK -> {
                            SubnetInteractionButton("≪ DEREFERENCE USER ≫", com.siliconsage.miner.ui.theme.ErrorRed) {
                                viewModel.onSubnetInteraction(message.id, "HIJACK_SYNC")
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun SubnetInteractionButton(text: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(24.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.1f), contentColor = color),
        shape = RoundedCornerShape(2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(text = text, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
    }
}
