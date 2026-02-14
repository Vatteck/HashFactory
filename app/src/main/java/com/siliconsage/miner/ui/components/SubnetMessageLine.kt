package com.siliconsage.miner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.util.SocialManager
import com.siliconsage.miner.viewmodel.GameViewModel
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun SubnetMessageLine(message: SocialManager.SubnetMessage, color: Color, viewModel: GameViewModel? = null) {
    val countdown = remember { mutableStateOf(0L) }
    
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
            Text(
                text = message.handle,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = ">>",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
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
        Text(
            text = message.content,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 2.dp)
        )

        // v3.4.18: Contextual Interactions
    if (viewModel != null && (message.interactionType != null || message.isForceReply)) {
            val stage = viewModel.storyStage.collectAsState().value
            
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
                            // v3.4.22: Weighted Response System
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
