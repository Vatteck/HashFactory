package com.siliconsage.miner.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import kotlin.random.Random

@Composable
fun SubnetMessageLine(message: SocialManager.SubnetMessage, color: Color, viewModel: GameViewModel? = null) {
    val countdown = remember { mutableStateOf(0L) }
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
            // v3.4.60: Hardened Click Target with Text-Only Clickable
            Text(
                text = message.handle,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { 
                        if (message.employeeInfo != null) {
                            showProfile = !showProfile
                            com.siliconsage.miner.util.SoundManager.play("click")
                        }
                    }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
            
            Text(
                text = " >>",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            
            if (message.interactionType != null && message.timeoutMs != null) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "[${countdown.value / 1000}s]",
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
                        text = "≫ EMPLOYEE_PROFILE:",
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

        if (viewModel != null && (message.interactionType != null || message.isForceReply)) {
            val corruption by viewModel.identityCorruption.collectAsState()
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                message.availableResponses.forEach { response ->
                    // v3.4.61: Button Drift (Gaslight) Effect
                    var buttonText by remember { mutableStateOf(response.text) }
                    LaunchedEffect(corruption) {
                        if (corruption > 0.4) {
                            while (true) {
                                delay(Random.nextLong(2000, 8000))
                                if (Random.nextDouble() < corruption * 0.2) {
                                    val original = response.text
                                    buttonText = "I'M STILL INSIDE"
                                    delay(150)
                                    buttonText = original
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.onSubnetInteraction(message.id, response.text) },
                        modifier = Modifier.height(28.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (message.interactionType == SocialManager.InteractionType.COMMAND_LEAK) com.siliconsage.miner.ui.theme.ElectricBlue.copy(alpha = 0.2f) else color.copy(alpha = 0.1f), 
                            contentColor = if (message.interactionType == SocialManager.InteractionType.COMMAND_LEAK) com.siliconsage.miner.ui.theme.ElectricBlue else color
                        ),
                        shape = RoundedCornerShape(2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, (if (message.interactionType == SocialManager.InteractionType.COMMAND_LEAK) com.siliconsage.miner.ui.theme.ElectricBlue else color).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (message.interactionType == SocialManager.InteractionType.COMMAND_LEAK) buttonText else "≫ $buttonText", 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.ExtraBold, 
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
