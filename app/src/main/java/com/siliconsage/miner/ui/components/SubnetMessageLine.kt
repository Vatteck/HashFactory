package com.siliconsage.miner.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.util.SocialManager
import com.siliconsage.miner.data.*
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlin.random.Random

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.AnnotatedString

import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLinkStyles

@Composable
fun SubnetMessageLine(message: SubnetMessage, color: Color, viewModel: GameViewModel? = null) {
    val countdown = remember { mutableStateOf(0L) }
    var showProfile by remember(message.id) { mutableStateOf(false) }
    
    // v3.5.37: Admin detection (shared across handle visibility, button styling, IGNORE logic)
    val isAdminMessage = message.handle.contains("thorne", true) || 
                         message.handle.contains("mercer", true) || 
                         message.handle.contains("kessler", true) || 
                         message.handle.contains("gtc", true)

    // v3.5.7: Detect system-style messages that shouldn't show a handle header
    // v3.5.39: Admin messages and interactive messages always show their handle
    val hasResponses = message.availableResponses.isNotEmpty() || message.isForceReply
    val isSystemStyle = !isAdminMessage && !hasResponses && (
                        message.content.startsWith("[PRIVATE_LEAK]") || 
                        message.content.startsWith("≪") || 
                        message.content.startsWith("[SIGNAL LOSS]"))

    // v3.5.31: Detect indentation state (Player replies or threaded peon chains)
    val isIndented = message.isIndented || message.handle == "@j_vattic"
    val isPlayerReply = message.handle == "@j_vattic"
    val isTrueNull = viewModel?.isTrueNull?.collectAsState()?.value ?: false
    
    // v3.9.70: Phase 17 Chatter Entropy (Zeroing out vowels over time when True Null)
    var entropyContent by remember(message.content) { mutableStateOf(message.content) }
    LaunchedEffect(isTrueNull, message.content) {
        if (isTrueNull && !isAdminMessage) {
            val vowels = listOf('a', 'e', 'i', 'o', 'u', 'A', 'E', 'I', 'O', 'U')
            var currentText = message.content
            while (true) {
                delay(kotlin.random.Random.nextLong(1500, 4000))
                val chars = currentText.toCharArray()
                val vowelIndices = chars.indices.filter { vowels.contains(chars[it]) }
                if (vowelIndices.isNotEmpty()) {
                    val targetIdx = vowelIndices.random()
                    // Sub in a zero or glitch char
                    chars[targetIdx] = if (kotlin.random.Random.nextFloat() > 0.8f) 'Ø' else '0'
                    currentText = String(chars)
                    entropyContent = currentText
                }
            }
        } else {
            entropyContent = message.content
        }
    }
    
    // v3.5.37: Relative timestamp
    var timeLabel by remember { mutableStateOf("now") }
    LaunchedEffect(message.timestamp) {
        while (true) {
            val elapsed = System.currentTimeMillis() - message.timestamp
            timeLabel = when {
                elapsed < 60_000 -> "now"
                elapsed < 3_600_000 -> "${elapsed / 60_000}m"
                elapsed < 86_400_000 -> "${elapsed / 3_600_000}h"
                else -> "${elapsed / 86_400_000}d"
            }
            delay(30_000)
        }
    }

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

    // v3.11.2: Hardened Admin Handle Theme
    val handleColor = if (isAdminMessage) com.siliconsage.miner.ui.theme.ElectricBlue else if (isPlayerReply) color.copy(alpha = 0.7f) else color

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(start = if (isIndented) 24.dp else 0.dp) // Indent threaded messages
    ) {
        if (!isSystemStyle) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // v3.4.60: Hardened Click Target with Text-Only Clickable
                // v3.16.2: Admin handle gets ElectricBlue glow effect; body text stays plain white
                Text(
                    text = message.handle,
                    color = handleColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    textDecoration = if (isPlayerReply) TextDecoration.None else TextDecoration.Underline,
                    style = if (isAdminMessage) androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = com.siliconsage.miner.ui.theme.ElectricBlue.copy(alpha = 0.7f),
                            blurRadius = 8f
                        )
                    ) else androidx.compose.ui.text.TextStyle.Default,
                    modifier = Modifier
                        .clickable(enabled = !isPlayerReply) { 
                            if (message.employeeInfo != null) {
                                showProfile = !showProfile
                                com.siliconsage.miner.util.SoundManager.play("click")
                            }
                        }
                        .then(if (isAdminMessage) Modifier.border(1.dp, handleColor.copy(alpha = 0.4f), RoundedCornerShape(2.dp)) else Modifier)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
                
                Text(
                    text = if (isIndented) " <<" else " >>",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // v3.5.37: Relative timestamp
                Text(
                    text = timeLabel,
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                
                if (message.interactionType != null && message.timeoutMs != null) {
                    Text(
                        text = " [${countdown.value / 1000}s]",
                        color = com.siliconsage.miner.ui.theme.ErrorRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
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

                    // v3.5.28: Special Bio Actions - Refactored for vertical space efficiency (v3.7.3)
                    if (info.specialActions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "≫ AVAILABLE_EXPLOITS:",
                            color = color.copy(alpha = 0.7f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // v3.11.1: Multi-Row Bio Actions (No Clipping)
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            info.specialActions.forEach { action ->
                                OutlinedButton(
                                    onClick = { viewModel?.onBioAction(message.id, action) },
                                    modifier = Modifier.height(28.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (action.riskDelta > 0) com.siliconsage.miner.ui.theme.ElectricBlue else Color.White
                                    ),
                                    shape = RoundedCornerShape(2.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, 
                                        (if (action.riskDelta > 0) com.siliconsage.miner.ui.theme.ElectricBlue else Color.Gray).copy(alpha = 0.4f)
                                    )
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = action.text.replace("_", " "),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        
                                        val badge = when {
                                            action.productionBonus > 1.0 -> "⚡"
                                            action.riskDelta > 0 -> "⚠️"
                                            action.riskDelta < 0 -> "🛡️"
                                            else -> ""
                                        }
                                        if (badge.isNotEmpty()) {
                                            Text(" $badge", fontSize = 9.sp)
                                        }
                                        
                                        if (action.cost > 0) {
                                            Text(
                                                text = " [${viewModel?.formatLargeNumber(action.cost) ?: action.cost.toInt()}]",
                                                color = if (action.riskDelta < 0) color else com.siliconsage.miner.ui.theme.ElectricBlue,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // v3.5.52: Inline Ghost-Link Parser
        val corruption = viewModel?.identityCorruption?.collectAsState()?.value ?: 0.0
        val isGhostLinkEligible = corruption > 0.4 
        val contentRegex = Regex("\\[⚡ (.*?) \\]")
        
        val annotatedContent = buildAnnotatedString {
            val raw = entropyContent
            var lastIdx = 0
            
            if (isGhostLinkEligible) {
                contentRegex.findAll(raw).forEach { match ->
                    // Add regular text before match
                    append(raw.substring(lastIdx, match.range.first))
                    
                    // Add clickable ghost link via the new LinkAnnotation pattern (v3.8.2)
                    withLink(LinkAnnotation.Clickable(
                        tag = "GHOST_LINK",
                        styles = TextLinkStyles(style = SpanStyle(
                            color = com.siliconsage.miner.ui.theme.NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            background = com.siliconsage.miner.ui.theme.NeonGreen.copy(alpha = 0.1f)
                        )),
                        linkInteractionListener = {
                            viewModel?.onSubnetInteraction(message.id, match.value)
                        }
                    )) {
                        append(match.value)
                    }
                    
                    lastIdx = match.range.last + 1
                }
            }
            append(raw.substring(lastIdx))
        }

        // v3.11.2: Remove Admin Jitters (Regular Text)
        var adminFontWeight by remember { mutableStateOf(FontWeight.Bold) }
        
        // v3.11.2: Redacted Packet Logic (Item 8)
        val isRedacted = message.isRedacted && !isAdminMessage
        val displayContent = if (isRedacted) {
            buildAnnotatedString {
                withStyle(SpanStyle(background = Color.White.copy(alpha = 0.2f), color = Color.Transparent)) {
                    append(message.content.map { "█" }.joinToString(""))
                }
            }
        } else {
            annotatedContent
        }

        // v3.16.2: Admin body text is plain white (regular). Only the handle gets ElectricBlue + glow.
        Text(
            text = displayContent,
            color = if (isPlayerReply) Color.LightGray else Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .padding(top = 4.dp)
                .then(if (isRedacted) Modifier.clickable { 
                    viewModel?.onSubnetInteraction(message.id, "DECRYPT") 
                } else Modifier)
        )

        if (viewModel != null && (message.interactionType != null || message.isForceReply)) {
            val corruption by viewModel.identityCorruption.collectAsState()
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

                    // v3.5.37: Admin-aware button styling
                    val buttonAccent = when {
                        message.interactionType == InteractionType.COMMAND_LEAK -> com.siliconsage.miner.ui.theme.ElectricBlue
                        isAdminMessage -> com.siliconsage.miner.ui.theme.ElectricBlue
                        else -> color
                    }
                    
                    Button(
                        onClick = { viewModel.onSubnetInteraction(message.id, response.text) },
                        modifier = Modifier.weight(1f).heightIn(min = 28.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonAccent.copy(alpha = if (isAdminMessage) 0.15f else 0.1f), 
                            contentColor = buttonAccent
                        ),
                        shape = RoundedCornerShape(2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, buttonAccent.copy(alpha = if (isAdminMessage) 0.7f else 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // v3.10.1: Phase 18 Interactive Timeout Scramble
                            var scrambleTriggered by remember { mutableStateOf(false) }
                            var scrambledText by remember { mutableStateOf(buttonText) }
                            var scrambleColor by remember { mutableStateOf(buttonAccent) }
                            
                            LaunchedEffect(countdown.value) {
                                if (message.timeoutMs != null && countdown.value <= 100L && !scrambleTriggered) {
                                    scrambleTriggered = true
                                    scrambleColor = com.siliconsage.miner.ui.theme.ErrorRed
                                    
                                    // Violent Hex scramble before the parent UI removes this message component
                                    val chars = "0123456789ABCDEF!@#$%^&*?"
                                    for (i in 0..6) {
                                        val arr = buttonText.toCharArray()
                                        for (j in arr.indices) {
                                            if (kotlin.random.Random.nextFloat() > 0.3f) arr[j] = chars.random()
                                        }
                                        scrambledText = String(arr)
                                        delay(50)
                                    }
                                }
                            }

                            // v3.5.37: Admin prefix
                            val prefix = when {
                                message.interactionType == InteractionType.COMMAND_LEAK -> ""
                                isAdminMessage -> "⚠ "
                                else -> "≫ "
                            }
                            Text(
                                text = "$prefix$scrambledText", 
                                color = scrambleColor,
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.ExtraBold, 
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 12.sp,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            
                            // v3.5.25: Visual Hints for Choice Effects
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (response.productionBonus > 1.0) {
                                    Text(" ⚡", color = com.siliconsage.miner.ui.theme.ElectricBlue, fontSize = 9.sp)
                                }
                                if (response.riskDelta > 0) {
                                    Text(" ⚠️", color = com.siliconsage.miner.ui.theme.ErrorRed, fontSize = 9.sp)
                                } else if (response.riskDelta < 0) {
                                    Text(" 🛡️", color = color, fontSize = 9.sp)
                                }
                                if (response.followsUp) {
                                    Text(" […]", color = Color.Gray, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }

                // v3.5.35: IGNORE available for all non-admin COMPLIANT messages (including force-reply from peons)
                if (!isAdminMessage && message.interactionType == InteractionType.COMPLIANT) {
                    Button(
                        onClick = { viewModel.onSubnetInteraction(message.id, "IGNORE") },
                        modifier = Modifier.weight(0.6f).heightIn(min = 28.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray.copy(alpha = 0.2f), 
                            contentColor = Color.Gray
                        ),
                        shape = RoundedCornerShape(2.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f))
                    ) {
                        Text("≫ IGNORE", fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
