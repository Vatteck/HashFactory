package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.siliconsage.miner.util.FormatUtils

@Composable
fun StatPill(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = Modifier.padding(2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                 imageVector = icon, 
                 contentDescription = null, 
                 tint = color,
                 modifier = Modifier.size(24.dp).padding(end = 4.dp)
            )
            Text(
                text = text.uppercase(), 
                color = Color.White, 
                fontSize = 9.sp, 
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.1).sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UpgradeItem(
    name: String,
    type: UpgradeType,
    level: Int,
    onBuy: (UpgradeType) -> Boolean,
    onSell: (UpgradeType) -> Unit,
    cost: Double,
    rateText: String,
    desc: String,
    formatPower: (Double) -> String,
    formatCost: (Double) -> String,
    isSovereign: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "cardScale")
    
    val isGhost = type.name.startsWith("GHOST") || type.name.startsWith("SHADOW") || type.name.startsWith("VOID") ||
                  type.name.startsWith("WRAITH") || type.name.startsWith("NEURAL_MIST") || type.name.startsWith("SINGULARITY")
    
    val primaryColor = if (isSovereign && isGhost) com.siliconsage.miner.ui.theme.SanctuaryPurple else if (isGhost) ErrorRed else NeonGreen
    
    val cardGradient = remember(isGhost, isSovereign) {
        if (isGhost) {
            if (isSovereign) Brush.verticalGradient(listOf(Color(0xFF2D004D), Color.Black))
            else Brush.verticalGradient(listOf(Color(0xFF4D0000), Color.Black))
        } else {
            Brush.verticalGradient(listOf(Color(0xFF0F1A0F), Color.Black))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .background(cardGradient, RoundedCornerShape(8.dp))
            .border(
                width = if (isGhost) 1.5.dp else 1.dp,
                brush = Brush.horizontalGradient(listOf(primaryColor.copy(alpha=0.6f), primaryColor.copy(alpha=0.1f), primaryColor.copy(alpha=0.6f))),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onBuy(type) }
            .padding(14.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                if (isGhost && !isSovereign) {
                    SystemGlitchText(text = name, color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                } else {
                    Text(text = name, color = primaryColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.background(primaryColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(text = "LVL $level", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Text(text = desc, color = if (isGhost && isSovereign) Color.White else Color.Gray, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, lineHeight = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (type.isHardware && rateText.isNotEmpty()) {
                    StatPill(text = rateText, icon = Icons.Default.Computer, color = NeonGreen)
                }
                if (type.isCooling) {
                    StatPill(text = "${String.format("%.1f", type.baseHeat)}/S", icon = Icons.Default.AcUnit, color = ElectricBlue)
                }
                if (type.isPowerRelated && type.gridContribution > 0) {
                    StatPill(text = "+${FormatUtils.formatLargeNumber(type.gridContribution)}KW", icon = Icons.Default.Bolt, color = Color(0xFFFFD700))
                }
                if (type.basePower > 0 && !type.isGenerator) {
                    StatPill(text = "-${FormatUtils.formatLargeNumber(type.basePower)}W", icon = Icons.Default.Power, color = Color(0xFFFFD700))
                }
                if (type.isSecurity && type.gridContribution > 0) {
                    StatPill(text = "+${FormatUtils.formatLargeNumber(type.gridContribution)} SEC", icon = Icons.Default.Lock, color = ElectricBlue)
                }
                if (type.isHardware && type.baseHeat > 0) {
                    StatPill(text = "+${String.format("%.1f", type.baseHeat)}/S", icon = Icons.Default.DeviceThermostat, color = ErrorRed)
                }
                if (type.efficiencyBonus > 0) {
                    StatPill(text = "+${(type.efficiencyBonus * 100).toInt()}% EFF", icon = Icons.Default.Settings, color = NeonGreen)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                val canSell = level > 0 && type != UpgradeType.RESIDENTIAL_TAP
                if (canSell) {
                     Box(modifier = Modifier.clickable { onSell(type) }.background(ErrorRed.copy(alpha=0.1f), RoundedCornerShape(4.dp)).border(1.dp, ErrorRed.copy(alpha=0.5f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                         Text("SELL", color = ErrorRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                     }
                } else { Spacer(modifier = Modifier.width(1.dp)) }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("COST:", color = Color.Gray, fontSize = 10.sp, modifier = Modifier.padding(end = 4.dp))
                    Text(text = "${formatCost(cost)} $", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ExchangeSection(rate: Double, color: Color, unitName: String, currencyName: String, onExchange: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "sellScale")

    Button(
        onClick = onExchange,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }.background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp)).border(BorderStroke(1.dp, color), RoundedCornerShape(4.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = color),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SELL $unitName", fontSize = 12.sp)
            Text("1 = ${String.format("%.4f", rate)} $currencyName", color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

@Composable
fun StakingSection(color: Color, currencyName: String, onStake: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "stakeScale")

    Button(
        onClick = onStake,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }.background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp)).border(BorderStroke(1.dp, color), RoundedCornerShape(4.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = color),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("STAKE $100 $currencyName", fontSize = 12.sp)
            Text("+Efficiency", color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

@Composable
fun RepairSection(integrity: Double, cost: Double, color: Color, storyStage: Int, currencyName: String, onRepair: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1f, label = "repairScale")

    Button(
        onClick = onRepair,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth().graphicsLayer { scaleX = scale; scaleY = scale }.background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp)).border(BorderStroke(1.dp, if (integrity < 50) com.siliconsage.miner.ui.theme.ErrorRed else color), RoundedCornerShape(4.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = if (integrity < 50) com.siliconsage.miner.ui.theme.ErrorRed else color),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (storyStage < 1) "REPAIR HARDWARE" else "REPAIR CORE", fontSize = 12.sp)
            Text("${integrity.toInt()}% @ ${String.format("%.1f", cost)} $currencyName", color = Color.LightGray, fontSize = 10.sp)
        }
    }
}

private var activeGlitchCount = 0
private const val MAX_ACTIVE_GLITCHES = 10

@Composable
fun SystemGlitchText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    glitchFrequency: Double = 0.15,
    glitchDurationMs: Long = 200,
    corruptionLevel: Double = 0.0, // New: Linked to identityCorruption
    overflow: TextOverflow = TextOverflow.Ellipsis,
    softWrap: Boolean = true,
    maxLines: Int = 1,
    lineHeight: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    letterSpacing: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified
) {
    var displayedText by remember(text) { mutableStateOf(text) }
    var isGlitching by remember { mutableStateOf(false) }
    
    val effectiveFrequency = (glitchFrequency + (corruptionLevel / 2.0)).coerceIn(0.0, 0.8)
    
    val glitchedVariant = remember(text, corruptionLevel) {
        val glitched = text.toCharArray()
        val symbols = if (corruptionLevel > 0.6) {
            listOf('0', '1', 'X', '□', '■', ' ') 
        } else {
            listOf('!', '@', '#', '$', '%', '^', '&', '*', '?', 'X', '0', '1')
        }
        
        for (i in glitched.indices) { 
            // Glitch density increases with corruption
            if (Math.random() > (0.9 - (corruptionLevel * 0.4))) { 
                glitched[i] = symbols.random()
            } 
        }
        
        var result = String(glitched)
        // Critical Identity Melt: Randomly inject DEADC0DE strings at high corruption
        if (corruptionLevel > 0.8 && Math.random() > 0.7 && result.length > 10) {
            result = "0xDEADC0DE" 
        }
        result
    }
    val canGlitch = remember { if (activeGlitchCount < MAX_ACTIVE_GLITCHES) { activeGlitchCount++; true } else { false } }
    DisposableEffect(Unit) { onDispose { if (canGlitch) activeGlitchCount-- } }
    LaunchedEffect(text, glitchFrequency, canGlitch) {
        if (!canGlitch) { displayedText = text; return@LaunchedEffect }
        while (true) {
            if (Math.random() < glitchFrequency) { isGlitching = true; displayedText = glitchedVariant; delay(glitchDurationMs); displayedText = text; isGlitching = false }
            delay(500)
        }
    }
    Text(text = displayedText, modifier = modifier, style = style, color = color, fontSize = fontSize, fontWeight = fontWeight, maxLines = maxLines, overflow = overflow, softWrap = softWrap, lineHeight = lineHeight, letterSpacing = letterSpacing)
}
