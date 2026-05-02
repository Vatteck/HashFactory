package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.*
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlin.random.Random

@Composable
fun ActiveCommandBuffer(viewModel: GameViewModel, color: Color) {
    val hex by viewModel.activeCommandHex.collectAsState()
    
    // v3.38.0: Unified Buffer Logic
    val activeDataset by viewModel.activeDataset.collectAsState()
    
    val baseProgress by viewModel.clickBufferProgress.collectAsState()
    val assignedHashProgress by viewModel.assignedHashProgress.collectAsState()
    // Manual compute owns the terminal buffer while a hash packet is in flight.
    // Dataset progress can still render when idle, but it must not mask COMPUTE HASH clicks.
    val showDatasetBuffer = activeDataset != null && baseProgress <= 0f
    val progress = if (showDatasetBuffer) activeDataset!!.progress.toFloat() else baseProgress
    val assignedQueuePercent = (assignedHashProgress * 100).toInt().coerceIn(0, 100)
    val pellets by viewModel.clickBufferPellets.collectAsState()

    val currentHeat by viewModel.currentHeat.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()
    val speedLevel by viewModel.clickSpeedLevel.collectAsState()
    val detectionRisk by viewModel.detectionRisk.collectAsState()

    val user = viewModel.getPromptUser()
    val host = viewModel.getPromptHost()
    val isMonitored = viewModel.isSubnetMonitored()

    val corruption by viewModel.identityCorruption.collectAsState()
    val promptUserColor = color
    val promptHostColor = when {
        isTrueNull -> ErrorRed
        isSovereign -> ConvergenceGold
        else -> ElectricBlue
    }

    val ghostChar by viewModel.ghostInputChar.collectAsState()
    val globalGlitchIntensity by viewModel.globalGlitchIntensity.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
            .graphicsLayer {
                if (globalGlitchIntensity > 0.3) {
                    translationX = (Random.nextFloat() * 4f - 2f) * globalGlitchIntensity
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
        // v3.12.0: Subtle "The Eye" Glyph for Monitoring
        if (isMonitored) {
            val infiniteTransition = rememberInfiniteTransition(label = "EyePulse")
            val eyeAlpha by infiniteTransition.animateFloat(
                initialValue = 0.1f, targetValue = 0.4f,
                animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
                label = "eye"
            )
            Text(
                text = "👁",
                color = Color.White.copy(alpha = eyeAlpha),
                fontSize = 11.sp,
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        Text(
            text = buildAnnotatedString {
                if (showDatasetBuffer) {
                    withStyle(SpanStyle(color = promptUserColor, fontWeight = FontWeight.ExtraBold)) { append(user) }
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.8f))) { append("@") }
                    withStyle(SpanStyle(color = promptHostColor, fontWeight = FontWeight.Bold)) { append(host) }
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.6f))) { append(":~> ") }
                    withStyle(SpanStyle(color = NeonGreen, fontWeight = FontWeight.Bold)) { append("EXEC [${activeDataset!!.name}]") }
                } else {
                    withStyle(SpanStyle(color = promptUserColor, fontWeight = FontWeight.ExtraBold)) { append(user) }
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.8f))) { append("@") }
                    withStyle(SpanStyle(color = promptHostColor, fontWeight = FontWeight.Bold)) { append(host) }
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.6f))) { append(":~") }
                }

                // v3.12.0: High-integrity/heat bracket glitching
                if (!showDatasetBuffer) {
                    val promptToken = if (globalGlitchIntensity > 0.8 && Random.nextDouble() < 0.2) {
                        listOf("{", "<", "§", "Ø").random()
                    } else "$"
                    withStyle(SpanStyle(color = Color.Yellow, fontWeight = FontWeight.ExtraBold)) { append(promptToken) }
                }
                append(" ")

                if (ghostChar.isNotEmpty()) {
                    withStyle(SpanStyle(color = Color.White.copy(alpha = 0.5f))) { append(ghostChar) }
                }
            },
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        val barLength = 40
        val filledCount = (progress * barLength).toInt().coerceIn(0, barLength)
        val isCritical = currentHeat >= 100.0
        val isGlitching = currentHeat > 85.0

        // A1: Heat-reactive buffer color
        val baseBufferColor = when {
            currentHeat >= 95.0 -> ErrorRed
            currentHeat >= 85.0 -> Color(0xFFFF6600) // Orange
            currentHeat >= 60.0 -> Color(0xFFFFB000) // Amber
            else -> color
        }
        
        // v3.38.0 Purity visual
        val bufferHeatColor = if (showDatasetBuffer && activeDataset!!.purity < 0.8) ErrorRed.copy(alpha = 0.8f) else baseBufferColor

        // A2: Pellet ghost trail — track last 3 positions
        val pelletHistory = remember { androidx.compose.runtime.snapshots.SnapshotStateList<Int>() }
        LaunchedEffect(filledCount) {
            if (filledCount > 0) {
                if (pelletHistory.isEmpty() || pelletHistory.first() != filledCount) {
                    pelletHistory.add(0, filledCount)
                    if (pelletHistory.size > 4) pelletHistory.removeAt(pelletHistory.size - 1)
                }
            } else {
                pelletHistory.clear()
            }
        }

        // v3.12.0: Interactive Haptic Feedback on Progress
        val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
        var lastHapticStep by remember { mutableIntStateOf(0) }
        LaunchedEffect(filledCount) {
            val step = (progress * 10).toInt() // 0-10 steps
            if (step > lastHapticStep && step > 0) {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                lastHapticStep = step
            } else if (filledCount == 0) {
                lastHapticStep = 0
            }
        }

        var isBursting by remember { mutableLongStateOf(0L) }
        LaunchedEffect(progress) {
            if (progress == 0f && filledCount == 0) {
                isBursting = System.currentTimeMillis()
            }
        }

        val annotatedBar = buildAnnotatedString {
            val timeSinceBurst = System.currentTimeMillis() - isBursting
            if (timeSinceBurst < 400 && isBursting > 0) {
                // v3.12.0: ASCII Particle Drift on commit
                val driftProgress = timeSinceBurst / 400f
                val particles = listOf("*", ".", ":", "·", "'", "`")
                withStyle(SpanStyle(color = color.copy(alpha = (1f - driftProgress) * 0.8f), fontWeight = FontWeight.Bold)) {
                    val startPad = (driftProgress * 15).toInt()
                    append(" ".repeat(startPad))
                    repeat(25) { append(particles.random()) }
                    if (driftProgress < 0.5f) append(" [COMMITTED]")
                }
            } else {
                val bracketColor = if (isCritical) ErrorRed else color.copy(alpha = 0.4f)

                // v3.12.0: Bracket Glitching
                val openBracket = if (globalGlitchIntensity > 0.7 && Random.nextDouble() < 0.1) "{" else "["
                val closeBracket = if (globalGlitchIntensity > 0.7 && Random.nextDouble() < 0.1) "}" else "]"

                withStyle(SpanStyle(color = bracketColor)) { append(openBracket) }

                // A3: Signal noise params — density/chars driven by globalGlitchIntensity
                val noiseMod = when {
                    globalGlitchIntensity > 0.6f -> 3
                    globalGlitchIntensity > 0.3f -> 5
                    isGlitching -> 7
                    else -> Int.MAX_VALUE
                }
                val noiseAlpha = when {
                    globalGlitchIntensity > 0.6f -> 0.18f
                    globalGlitchIntensity > 0.3f -> 0.10f
                    else -> 0.06f
                }
                val noiseChars = if (globalGlitchIntensity > 0.5f)
                    listOf("?", "!", "§", "Ø", "▒", "░") else listOf("·", ".", "·", ":")

                // Proposal 7: Mouth animation cycle (250ms) via InfiniteTransition to force recomposition
                val chompTransition = rememberInfiniteTransition(label = "chomp_cycle")
                val isChompOpen by chompTransition.animateFloat(
                    initialValue = 0f, targetValue = 1f,
                    animationSpec = infiniteRepeatable(animation = tween(125, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
                    label = "chomp"
                )
                val chompCycle = isChompOpen > 0.5f

                for (i in 0 until barLength) {
                    when {
                        i < filledCount -> {
                            // Proposal 9: Ghost chasing from the left
                            val risk = detectionRisk
                            val isGhostPos = risk > 75.0 && (i == ((System.currentTimeMillis() / 400) % filledCount.coerceAtLeast(1)).toInt())

                            if (isGhostPos) {
                                val flicker = if (globalGlitchIntensity > 0.5f) (Random.nextDouble() > 0.3) else true
                                val ghostAlpha = if (flicker) 1.0f else 0.2f
                                val ghostColor = if (isCritical) ErrorRed else Color.Cyan
                                withStyle(SpanStyle(color = ghostColor.copy(alpha = ghostAlpha), fontWeight = FontWeight.ExtraBold)) { append("G") }
                            } else {
                                // A1: Filled track uses heat-reactive color
                                withStyle(SpanStyle(color = bufferHeatColor)) { append("-") }
                            }
                        }
                        i == filledCount -> {
                            // Pac-Man head — 'c' when mouth open on pellet, 'C' closed
                            val isOnPellet = pellets.contains(i)
                            val entityChar = when {
                                isTrueNull -> "0"
                                isSovereign -> "Σ"
                                else -> if (chompCycle || isOnPellet) "c" else "C"
                            }
                            val entityColor = when {
                                isCritical -> ErrorRed
                                isTrueNull -> ErrorRed
                                isSovereign -> ConvergenceGold
                                else -> Color.Yellow
                            }
                            withStyle(SpanStyle(color = entityColor, fontWeight = FontWeight.ExtraBold)) { append(entityChar) }
                        }
                        else -> {
                            val trailIdx = pelletHistory.indexOf(i)
                            
                            when {
                                trailIdx in 1..3 -> {
                                    val trailAlpha = when (trailIdx) { 1 -> 0.45f; 2 -> 0.22f; else -> 0.10f }
                                    val trailColor = if (isCritical) ErrorRed.copy(alpha = trailAlpha)
                                                     else Color.Yellow.copy(alpha = trailAlpha)
                                    withStyle(SpanStyle(color = trailColor, fontWeight = FontWeight.Bold)) { append("·") }
                                }
                                pellets.contains(i) -> {
                                    val pelletCol = if (isCritical) ErrorRed else Color.White
                                    withStyle(SpanStyle(color = pelletCol, fontWeight = FontWeight.Bold)) { append("o") }
                                }
                                else -> {
                                    // A3: Signal noise in empty track
                                    val isNoise = noiseMod < Int.MAX_VALUE &&
                                                  (i + (progress * 100).toInt()) % noiseMod == 0
                                    val trackChar = when {
                                        isNoise -> noiseChars.random()
                                        isTrueNull -> " "
                                        else -> "·"
                                    }
                                    val trackColor = when {
                                        isCritical -> ErrorRed.copy(alpha = 0.5f)
                                        isNoise -> bufferHeatColor.copy(alpha = noiseAlpha)
                                        else -> color.copy(alpha = 0.2f)
                                    }
                                    withStyle(SpanStyle(color = trackColor)) { append(trackChar) }
                                }
                            }
                        }
                    }
                }
                withStyle(SpanStyle(color = bracketColor)) { append(closeBracket) }
            }
        }

        Text(
            text = annotatedBar,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )

        val hexColor = when (speedLevel) {
            1 -> NeonGreen
            2 -> ErrorRed
            else -> Color.White.copy(alpha = 0.5f)
        }
        Text(
            text = if (isCritical) "LOCKED" else hex,
            color = hexColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (speedLevel > 0) FontWeight.Bold else FontWeight.Normal
        )
        }

        Text(
            text = "ASSIGNED QUEUE: $assignedQueuePercent%",
            color = color.copy(alpha = 0.48f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(top = 1.dp)
        )
    }
}
