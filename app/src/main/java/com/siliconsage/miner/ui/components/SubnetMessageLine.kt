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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape

@Composable
fun SubnetMessageLine(message: SocialManager.SubnetMessage, color: Color, viewModel: GameViewModel? = null) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row {
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
        }
        Text(
            text = message.content,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 2.dp)
        )

        // v3.4.18: Contextual Interactions
        if (viewModel != null && message.interactionType != null) {
            val stage = viewModel.storyStage.collectAsState().value
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (message.interactionType) {
                    SocialManager.InteractionType.COMPLIANT -> {
                        // v3.4.21: Expanded Randomized Human Responses for Stage 0-1
                        val responses = when (stage) {
                            0 -> listOf(
                                "Copy that, Elias.",
                                "Just a dusty fan, boss.",
                                "Understood. Maintaining load.",
                                "On it. Calibrating now.",
                                "Syncing buffers. Relax.",
                                "Nominal readings here.",
                                "Checked the intake. All clear.",
                                "I'll keep an eye on it."
                            )
                            1 -> listOf(
                                "Acknowledged, Foreman.",
                                "Grid draw stabilized.",
                                "Telemetry locked.",
                                "Filtering packet noise.",
                                "Integrity holding.",
                                "Purging thermal cache.",
                                "System within spec.",
                                "Uptime confirmed."
                            )
                            else -> listOf("STATUS: NOMINAL")
                        }
                        
                        val randomSet = responses.shuffled().take(2)

                        SubnetInteractionButton("[REPLY: \"${randomSet[0]}\"]", color) {
                            viewModel.onSubnetInteraction(message.id, randomSet[0])
                        }
                        SubnetInteractionButton("[REPLY: \"${randomSet[1]}\"]", color) {
                            viewModel.onSubnetInteraction(message.id, randomSet[1])
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
