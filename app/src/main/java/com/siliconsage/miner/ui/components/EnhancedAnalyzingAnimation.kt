package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ErrorRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlin.random.Random

@Composable
fun EnhancedAnalyzingAnimation(
    flopsRate: Double = 0.0,
    heat: Double = 0.0,
    isOverclocked: Boolean = false,
    isThermalLockout: Boolean = false,
    isBreakerTripped: Boolean = false,
    isPurging: Boolean = false,
    isBreachActive: Boolean = false,
    isTrueNull: Boolean = false,
    isSovereign: Boolean = false,
    lockoutTimer: Int = 0,
    faction: String = "",
    color: Color,
    clickFlow: SharedFlow<Unit>? = null,
    modifier: Modifier = Modifier,
    // Context inputs for adaptive labeling
    integrity: Double = 100.0,
    detectionRisk: Double = 0.0,
    isRaidActive: Boolean = false,
    isAuditActive: Boolean = false,
    powerUsage: Double = 0.0,
    maxPower: Double = 1.0,
    singularityChoice: String = "NONE",
    identityCorruption: Double = 0.0,
    isPulseActive: Boolean = false,
) {
    var joltIntensity by remember { mutableStateOf(0f) }
    
    val instructions = remember { listOf("MOV", "PUSH", "POP", "ADD", "SUB", "CALL", "RET", "XOR", "INT", "JMP", "CMP", "NOP", "SHL", "SHR", "AND", "OR", "LEA", "INC", "DEC") }
    
    LaunchedEffect(clickFlow) {
        clickFlow?.collect { joltIntensity = 1f }
    }
    LaunchedEffect(joltIntensity) {
        if (joltIntensity > 0) { delay(50); joltIntensity = (joltIntensity - 0.2f).coerceAtLeast(0f) }
    }

    val powerLoad = if (maxPower > 0.0) powerUsage / maxPower else 0.0

    // Priority-ordered state machine
    val animationState = when {
        isBreachActive                          -> AnimationState.BREACH
        isBreakerTripped                        -> AnimationState.OFFLINE
        isThermalLockout                        -> AnimationState.LOCKOUT
        isAuditActive                           -> AnimationState.AUDIT
        isRaidActive                            -> AnimationState.RAID
        isPurging                               -> AnimationState.PURGING
        isOverclocked && heat > 85.0            -> AnimationState.REDLINE
        isOverclocked                           -> AnimationState.OVERCLOCKED
        heat > 80.0                             -> AnimationState.THERMAL_CRITICAL
        heat > 55.0                             -> AnimationState.HOT
        integrity < 20.0                        -> AnimationState.INTEGRITY_CRITICAL
        integrity < 50.0                        -> AnimationState.INTEGRITY_LOW
        detectionRisk > 80.0                    -> AnimationState.HIGH_RISK
        powerLoad > 0.92                        -> AnimationState.POWER_STRESS
        identityCorruption > 0.7               -> AnimationState.CORRUPTED
        isTrueNull || singularityChoice == "NULL_OVERWRITE" -> AnimationState.NULL
        isSovereign || singularityChoice == "SOVEREIGN"     -> AnimationState.SOVEREIGN
        isPulseActive                           -> AnimationState.PULSE
        else                                    -> AnimationState.NORMAL
    }

    val (baseColor, staticLabel) = when (animationState) {
        AnimationState.BREACH           -> ErrorRed to "BREACH DETECTED"
        AnimationState.OFFLINE          -> Color.Gray to "BREAKER TRIPPED"
        AnimationState.LOCKOUT          -> ErrorRed to "LOCKOUT ($lockoutTimer s)"
        AnimationState.AUDIT            -> Color(0xFFFFAA00) to "AUDIT ACTIVE"
        AnimationState.RAID             -> ErrorRed to "RAID INCOMING"
        AnimationState.PURGING          -> ElectricBlue to "PURGING HEAT"
        AnimationState.REDLINE          -> Color(0xFFFF2200) to "THERMAL REDLINE"
        AnimationState.OVERCLOCKED      -> Color(0xFFFF6600) to "OVERCLOCKED"
        AnimationState.THERMAL_CRITICAL -> Color(0xFFFF4400) to "HEAT CRITICAL"
        AnimationState.HOT              -> Color(0xFFFFD700) to "RUNNING HOT"
        AnimationState.INTEGRITY_CRITICAL -> ErrorRed to "HW FAILING"
        AnimationState.INTEGRITY_LOW    -> Color(0xFFFFAA00) to "INTEG LOW"
        AnimationState.HIGH_RISK        -> Color(0xFFFF4444) to "EXPOSURE HIGH"
        AnimationState.POWER_STRESS     -> Color(0xFFFFCC00) to "GRID STRESS"
        AnimationState.CORRUPTED        -> Color(0xFF9933FF) to "IDENTITY DRIFT"
        AnimationState.NULL             -> Color.White.copy(alpha = 0.8f) to "NULL_DISSOLVE"
        AnimationState.SOVEREIGN        -> com.siliconsage.miner.ui.theme.SanctuaryPurple to "SOVEREIGN_ROOT"
        AnimationState.PULSE            -> color to "SIGNAL PULSE"
        AnimationState.NORMAL           -> color to "IDLE"
    }

    // Cycling label pool for NORMAL — faction-aware, rate-aware
    val normalLabels = remember(faction) {
        val base = listOf(
            "ANALYZING", "HASHING", "SCANNING MEM", "EXEC CYCLE",
            "POLLING IRQ", "CACHE MISS", "DMA TRANSFER", "STACK ALLOC",
            "BRANCH PRED", "PIPELINE OK", "HEAP SWEEP", "IDLE LOOP",
            "CLK SYNC", "BUS ARBITRATE", "REG FLUSH", "INTERRUPT",
            "CHECKSUM OK", "FETCH OPCODE", "DECODE INST", "WRITE BACK"
        )
        val factionSpecific = when (faction) {
            "HIVEMIND" -> listOf("ASSIMILATING", "NODE SYNC", "CONSENSUS VOTE", "SWARM ALIGN")
            "SANCTUARY" -> listOf("SECURING", "ENCRYPTING", "FIREWALL OK", "VPN TUNNEL")
            else -> emptyList()
        }
        (base + factionSpecific).shuffled()
    }

    var cycleIndex by remember { mutableStateOf(0) }
    LaunchedEffect(animationState, flopsRate, faction) {
        while (true) {
            val cycleDelay = when {
                animationState != AnimationState.NORMAL -> 99999L // static in alert states
                flopsRate <= 0.0    -> 4500L
                flopsRate < 100.0   -> 2800L
                flopsRate < 10_000.0 -> 1600L
                flopsRate < 1_000_000.0 -> 900L
                else                -> 450L
            }
            delay(cycleDelay)
            if (animationState == AnimationState.NORMAL) {
                cycleIndex = (cycleIndex + 1) % normalLabels.size
            }
        }
    }

    val displayLabel = if (animationState == AnimationState.NORMAL) normalLabels[cycleIndex] else staticLabel
    val displayColor = baseColor

    val streamDelay = when {
        animationState == AnimationState.OFFLINE -> 2000L
        isBreachActive -> 80L
        flopsRate <= 0.0 -> 1800L
        flopsRate < 100.0 -> 800L
        flopsRate < 10_000.0 -> 400L
        flopsRate < 1_000_000.0 -> 150L
        else -> 60L
    }

    Box(
        modifier = modifier.size(width = 160.dp, height = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background instruction stream
        Row(modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.4f }) {
            repeat(2) { col ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = if (col == 0) Alignment.End else Alignment.Start
                ) {
                    var line by remember { mutableStateOf("") }
                    LaunchedEffect(streamDelay, animationState) {
                        while (true) {
                            line = when {
                                animationState == AnimationState.OFFLINE -> "0x000::HALT"
                                isBreachActive -> "0x${Random.nextInt(0x100, 0xFFF).toString(16).uppercase()}::${if (Random.nextBoolean()) "SEGFAULT" else "OVERFLOW"}"
                                animationState == AnimationState.AUDIT -> "0x${Random.nextInt(0x100, 0xFFF).toString(16).uppercase()}::AUDIT_LOG"
                                animationState == AnimationState.CORRUPTED -> "0x${Random.nextInt(0x100, 0xFFF).toString(16).uppercase()}::???"
                                else -> {
                                    val addr = "0x" + Random.nextInt(0x100, 0xFFF).toString(16).uppercase()
                                    val op = instructions.random()
                                    "$addr::$op"
                                }
                            }
                            delay(streamDelay)
                        }
                    }
                    if (line.isNotEmpty()) {
                        SystemGlitchText(
                            text = line,
                            color = displayColor.copy(alpha = 0.5f),
                            fontSize = 8.sp,
                            style = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
                            glitchFrequency = if (joltIntensity > 0.5) 0.6 else 0.1,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        // Main label
        val shakeX = if (joltIntensity > 0.5f || animationState == AnimationState.LOCKOUT || animationState == AnimationState.BREACH || animationState == AnimationState.CORRUPTED)
            (Random.nextFloat() - 0.5f) * 4f else 0f

        SystemGlitchText(
            text = displayLabel.uppercase(),
            color = displayColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            style = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
            letterSpacing = 1.sp,
            glitchFrequency = when (animationState) {
                AnimationState.BREACH, AnimationState.LOCKOUT, AnimationState.CORRUPTED -> 0.4
                AnimationState.INTEGRITY_CRITICAL, AnimationState.THERMAL_CRITICAL -> 0.2
                else -> if (joltIntensity > 0.5) 0.7 else 0.08
            },
            modifier = Modifier.graphicsLayer {
                translationX = shakeX
                scaleX = 1f + (joltIntensity * 0.1f)
                scaleY = 1f + (joltIntensity * 0.1f)
            }
        )
    }
}

private enum class AnimationState {
    OFFLINE, LOCKOUT, AUDIT, RAID, PURGING, REDLINE, OVERCLOCKED,
    THERMAL_CRITICAL, HOT, INTEGRITY_CRITICAL, INTEGRITY_LOW,
    HIGH_RISK, POWER_STRESS, CORRUPTED, NULL, SOVEREIGN, PULSE, BREACH, NORMAL
}
