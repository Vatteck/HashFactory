package com.siliconsage.miner.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import com.siliconsage.miner.ui.components.HeaderSection
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.siliconsage.miner.ui.components.SystemGlitchText
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.siliconsage.miner.data.LogEntry
import com.siliconsage.miner.ui.components.ExchangeSection
import com.siliconsage.miner.ui.components.StakingSection
import com.siliconsage.miner.ui.components.RepairSection
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.SanctuaryTeal
import com.siliconsage.miner.ui.theme.HivemindOrange
import com.siliconsage.miner.ui.theme.HivemindRed
import androidx.compose.ui.text.style.TextOverflow
import com.siliconsage.miner.ui.components.TechnicalCornerShape
import com.siliconsage.miner.ui.components.CyberHeader
import com.siliconsage.miner.ui.components.GlitchSurface
import com.siliconsage.miner.util.FormatUtils
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.ResourceRepository
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun TerminalScreen(viewModel: GameViewModel, primaryColor: Color) {
    val storyStage by viewModel.storyStage.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "cursorBlink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse),
        label = "cursor"
    )
    val showCursor = cursorAlpha > 0.5f

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Transparent).padding(16.dp)
    ) {
        TerminalHeader(viewModel, primaryColor)
        
        Spacer(modifier = Modifier.height(4.dp))

        val mode by viewModel.activeTerminalMode.collectAsState()
        val hasDecision by viewModel.hasNewSubnetDecision.collectAsState() // v3.4.68
        val hasChatter by viewModel.hasNewSubnetChatter.collectAsState() // v3.4.68
        val hasIO by viewModel.hasNewIOMessage.collectAsState()
        val currentHeat by viewModel.currentHeat.collectAsState()

        Row(
            modifier = Modifier.fillMaxWidth().offset(y = 1.dp), // Chrome-style overlap
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val corruption by viewModel.identityCorruption.collectAsState()
            
            // v3.13.22: Integrated Tab System (Connected to body)
            TerminalTab(
                label = "I/O",
                active = mode == "IO",
                hasFlash = hasIO,
                color = primaryColor,
                corruption = corruption,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setTerminalMode("IO") }
            )
            
            TerminalTab(
                label = "SUBNET",
                active = mode == "SUBNET",
                hasFlash = hasChatter || hasDecision,
                isDecision = hasDecision,
                color = primaryColor,
                corruption = corruption,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setTerminalMode("SUBNET") }
            )
        }

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp, topStart = 0.dp, topEnd = 0.dp))
                .border(BorderStroke(1.5.dp, if (currentHeat > 90.0) ErrorRed else primaryColor.copy(alpha = 0.85f)), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp, topStart = 0.dp, topEnd = 0.dp))
        ) {
            TerminalLogs(viewModel, primaryColor, showCursor)
        }

        Spacer(modifier = Modifier.height(16.dp))
        TerminalControls(viewModel, primaryColor)
    }
}

@Composable
fun TerminalHeader(viewModel: GameViewModel, color: Color) {
    val powerUsage by viewModel.activePowerUsage.collectAsState()
    val maxPower by viewModel.maxPowerkW.collectAsState()
    val currentHeat by viewModel.currentHeat.collectAsState()

    val isCritical = currentHeat > 90.0 || (powerUsage > maxPower * 0.9)
    val criticalTransition = rememberInfiniteTransition(label = "criticalVibration")
    val vibrationOffset by criticalTransition.animateFloat(
        initialValue = -2f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(50, easing = LinearEasing), RepeatMode.Reverse),
        label = "hudVibration"
    )
    val vibrationState by animateFloatAsState(targetValue = if (isCritical) vibrationOffset else 0f, label = "hudVibrationBlend")

    HeaderSection(
        viewModel = viewModel,
        color = color,
        onToggleOverclock = { viewModel.toggleOverclock() },
        onPurge = { viewModel.purgeHeat() },
        onRepair = { viewModel.repairIntegrity() },
        modifier = Modifier.graphicsLayer { translationX = vibrationState }
    )
}

@Composable
fun TerminalTabButton(text: String, active: Boolean, hasNew: Boolean, color: Color, isChoicePending: Boolean, isPaused: Boolean = false, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "TabGlow")
    
    val flashAlpha by infiniteTransition.animateFloat(
        0.2f, 1f, 
        infiniteRepeatable(tween(if (isChoicePending) 300 else 800), RepeatMode.Reverse), 
        label = "glow"
    )

    val alertColor = when {
        isChoicePending -> ErrorRed
        text == "SUBNET" && hasNew -> ElectricBlue 
        else -> color
    }

    Column(modifier = Modifier.clickable { onClick() }.padding(vertical = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            color = if (active) color else alertColor.copy(alpha = if (hasNew || isChoicePending) flashAlpha else 0.4f),
            fontSize = 11.sp,
            fontWeight = if (active || hasNew || isChoicePending) FontWeight.ExtraBold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )
        if ((hasNew || isChoicePending) && !active) {
            Box(modifier = Modifier.padding(top = 1.dp).width(20.dp).height(2.dp).background(alertColor.copy(alpha = flashAlpha)))
        } else if (active) {
            Box(modifier = Modifier.padding(top = 1.dp).width(16.dp).height(1.dp).background(color))
        }
    }
}

@Composable
fun TerminalLogs(viewModel: GameViewModel, primaryColor: Color, showCursor: Boolean) {
    val isPaused by viewModel.isSubnetPaused.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val subnetMessages by viewModel.subnetMessages.collectAsState()
    val mode by viewModel.activeTerminalMode.collectAsState()
    val currentHeat by viewModel.currentHeat.collectAsState()
    val isRaid by viewModel.isRaidActive.collectAsState()
    val listState = rememberLazyListState()
    
    val glitchOffset by viewModel.terminalGlitchOffset.collectAsState()
    val glitchAlpha by viewModel.terminalGlitchAlpha.collectAsState()
    val rawSicknessIntensity by viewModel.substrateStaticIntensity.collectAsState()
    val isCascadeDesync by viewModel.isCascadeDesync.collectAsState()

    // v3.16.0: Double sickness corruption rate during active Cascade Desync
    val sicknessIntensity = if (isCascadeDesync) (rawSicknessIntensity * 2f).coerceAtMost(0.5f) else rawSicknessIntensity

    val corruption by viewModel.identityCorruption.collectAsState()
    val flopsRate by viewModel.flopsProductionRate.collectAsState()

    LaunchedEffect(logs.size, subnetMessages.size, mode) {
        if (!isPaused) {
            if (mode == "IO" && logs.isNotEmpty()) {
                listState.scrollToItem(logs.size - 1)
            } else if (mode == "SUBNET" && subnetMessages.isNotEmpty()) {
                listState.scrollToItem(subnetMessages.size - 1)
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Transparent)
            .graphicsLayer {
                translationX = glitchOffset
                alpha = glitchAlpha
            }
            .drawBehind {
                // v3.16.0: Red wash during Cascade Desync
                if (isCascadeDesync) {
                    drawRect(Color.Red.copy(alpha = 0.03f))
                }

                // v3.5.54.3: High-Frequency Data Rain (GPU Rendered)
                if (flopsRate > 1e6) {
                    val rainStep = 32.dp.toPx()
                    val rainColor = primaryColor.copy(alpha = 0.03f)
                    val speedVal = (kotlin.math.log10(flopsRate.coerceAtLeast(1.0)) / 5.0).coerceIn(1.0, 10.0)
                    val timeVal = (System.currentTimeMillis() % 1000000L).toDouble() / 1000.0
                    
                    var xPos = 0f
                    while (xPos < this.size.width) {
                        val offset = (kotlin.math.sin(xPos.toDouble() * 0.732) * 1000.0)
                        val yBase = ((timeVal * 100.0 * speedVal + offset) % this.size.height.toDouble()).toFloat()
                        
                        drawLine(
                            color = rainColor,
                            start = Offset(xPos, yBase),
                            end = Offset(xPos, (yBase + 40.dp.toPx()).coerceAtMost(this.size.height)),
                            strokeWidth = 1.dp.toPx()
                        )
                        xPos += rainStep
                    }
                }

                // v3.16.0: CRT shimmer during Cascade Desync (horizontal scan-line jitter)
                if (isCascadeDesync) {
                    val shimmerColor = Color.White.copy(alpha = 0.04f)
                    val time = (System.currentTimeMillis() % 2000L).toFloat()
                    val jitterBand = (time / 2000f * this.size.height) % this.size.height
                    drawRect(
                        shimmerColor,
                        topLeft = Offset(0f, jitterBand),
                        size = androidx.compose.ui.geometry.Size(this.size.width, 4.dp.toPx())
                    )
                }
            }
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "scanline")
        val scanlineOffset by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
            label = "scanlinePos"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = scanlineOffset * size.height
            drawRect(
                color = primaryColor.copy(alpha = 0.05f),
                topLeft = Offset(0f, y),
                size = Size(size.width, 1.dp.toPx())
            )
        }

        val alphaState = infiniteTransition.animateFloat(initialValue = 0.02f, targetValue = 0.05f, animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse), label = "alpha")

        Text(
            text = "01101001 01110011 00100000 01100001 01101100 01101001 01110110 01100101 ".repeat(50),
            color = primaryColor.copy(alpha = alphaState.value),
            fontSize = 14.sp, fontFamily = FontFamily.Monospace, lineHeight = 18.sp, overflow = TextOverflow.Clip,
            modifier = Modifier.fillMaxSize().padding(4.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            val process by viewModel.currentProcess.collectAsState()
            Text(
                text = "[PROCESS: $process]".uppercase(),
                color = primaryColor.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                if (mode == "IO") {
                    itemsIndexed(items = logs, key = { _, entry -> entry.id }) { index, entry ->
                        // v3.13.6: Fixed StringIndexOutOfBounds crash - Moved glitching inside TerminalLogLine
                        val displayMessage = if (isRaid && Random.nextFloat() > 0.7f) {
                            "0x" + Random.nextInt(0xDEADBC).toString(16).uppercase() + " // [CORRUPTED]"
                        } else entry.message

                        val reputation = viewModel.reputationTier.collectAsState().value
                        TerminalLogLine(
                            log = displayMessage, 
                            isLast = index == logs.lastIndex, 
                            primaryColor = primaryColor, 
                            showCursor = showCursor, 
                            reputationTier = reputation,
                            sicknessIntensity = sicknessIntensity // Pathing the hook through
                        )
                    }
                } else {
                    itemsIndexed(items = subnetMessages, key = { _, message -> message.id }) { _, message ->
                        com.siliconsage.miner.ui.components.SubnetMessageLine(message, primaryColor, viewModel)
                    }
                    if (viewModel.isSubnetTyping.value) {
                        item {
                            // v3.5.36: Rotating typing indicator
                            val typingTexts = remember { listOf(
                                "≫ [HANDSHAKE_IN_PROGRESS...]",
                                "≫ [DECRYPTING_SIGNAL...]",
                                "≫ [BUFFER_SYNC...]",
                                "≫ [AWAITING_PACKET...]",
                                "≫ [RESOLVING_HANDLE...]",
                                "≫ [ROUTING_THROUGH_SUBNET...]"
                            ) }
                            val typingText = remember { typingTexts.random() }
                            Text(
                                text = typingText,
                                color = primaryColor.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

            val cmdShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
            Box(modifier = Modifier.fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f), cmdShape)
                .border(BorderStroke(1.dp, primaryColor.copy(alpha = 0.55f)), cmdShape)
            ) {
                ActiveCommandBuffer(viewModel, primaryColor)
            }
            ManualComputeButton(viewModel, primaryColor)
        }
    }
}

@Composable
fun ActiveCommandBuffer(viewModel: GameViewModel, color: Color) {
    val progress by viewModel.clickBufferProgress.collectAsState()
    val pellets by viewModel.clickBufferPellets.collectAsState()
    val hex by viewModel.activeCommandHex.collectAsState()
    val currentHeat by viewModel.currentHeat.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()
    val speedLevel by viewModel.clickSpeedLevel.collectAsState()

    val user = viewModel.getPromptUser()
    val host = viewModel.getPromptHost()
    val isMonitored = viewModel.isSubnetMonitored()

    val corruption by viewModel.identityCorruption.collectAsState()
    val promptUserColor = color
    val promptHostColor = when {
        isTrueNull -> ErrorRed
        isSovereign -> com.siliconsage.miner.ui.theme.ConvergenceGold
        else -> ElectricBlue
    }
    
    val ghostChar by viewModel.ghostInputChar.collectAsState()
    val globalGlitchIntensity by viewModel.globalGlitchIntensity.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
            .graphicsLayer { 
                if (globalGlitchIntensity > 0.3) {
                    translationX = (Random.nextFloat() * 4f - 2f) * globalGlitchIntensity
                }
            },
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
            text = androidx.compose.ui.text.buildAnnotatedString {
                withStyle(androidx.compose.ui.text.SpanStyle(color = promptUserColor, fontWeight = FontWeight.ExtraBold)) {
                    append(user)
                }
                withStyle(androidx.compose.ui.text.SpanStyle(color = Color.White.copy(alpha = 0.8f))) {
                    append("@")
                }
                withStyle(androidx.compose.ui.text.SpanStyle(color = promptHostColor, fontWeight = FontWeight.Bold)) {
                    append(host)
                }
                withStyle(androidx.compose.ui.text.SpanStyle(color = Color.White.copy(alpha = 0.6f))) {
                    append(":~")
                }
                
                // v3.12.0: High-integrity/heat bracket glitching
                val promptToken = if (globalGlitchIntensity > 0.8 && Random.nextDouble() < 0.2) {
                    listOf("{", "<", "§", "Ø").random()
                } else "$"
                
                withStyle(androidx.compose.ui.text.SpanStyle(color = Color.Yellow, fontWeight = FontWeight.ExtraBold)) {
                    append(promptToken)
                }
                append(" ")
                
                if (ghostChar.isNotEmpty()) {
                    withStyle(androidx.compose.ui.text.SpanStyle(color = Color.White.copy(alpha = 0.5f))) {
                        append(ghostChar)
                    }
                }
            },
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        val barLength = 40
        val filledCount = (progress * barLength).toInt().coerceIn(0, barLength)
        val isCritical = currentHeat >= 100.0
        val isGlitching = currentHeat > 85.0

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

        val annotatedBar = androidx.compose.ui.text.buildAnnotatedString {
            val timeSinceBurst = System.currentTimeMillis() - isBursting
            if (timeSinceBurst < 400 && isBursting > 0) {
                // v3.12.0: ASCII Particle Drift
                val driftProgress = timeSinceBurst / 400f
                val particles = listOf("*", ".", ":", "·", "'", "`")
                withStyle(androidx.compose.ui.text.SpanStyle(color = color.copy(alpha = (1f - driftProgress) * 0.8f), fontWeight = FontWeight.Bold)) {
                    // Drift the starting point of the particle field
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

                withStyle(androidx.compose.ui.text.SpanStyle(color = bracketColor)) { append(openBracket) }

                for (i in 0 until barLength) {
                    when {
                        i < filledCount -> {
                            val trackFilledCol = if (isCritical) ErrorRed else color
                            withStyle(androidx.compose.ui.text.SpanStyle(color = trackFilledCol)) { append("-") }
                        }
                        i == filledCount -> {
                            val isOnPellet = pellets.contains(i)
                            // v3.12.0: Pellet Jitter
                            val jitterOffset = if (isGlitching && Random.nextDouble() < 0.2) (Random.nextInt(3) - 1) else 0
                            val entityChar = when {
                                isTrueNull -> "0"
                                isSovereign -> "Σ"
                                else -> if (isOnPellet) "c" else "C"
                            }
                            val entityColor = when {
                                isCritical -> ErrorRed
                                isTrueNull -> ErrorRed
                                isSovereign -> com.siliconsage.miner.ui.theme.ConvergenceGold
                                else -> Color.Yellow
                            }
                            withStyle(androidx.compose.ui.text.SpanStyle(color = entityColor, fontWeight = FontWeight.ExtraBold)) {
                                append(entityChar)
                            }
                        }
                        else -> {
                            if (pellets.contains(i)) {
                                val pelletCol = if (isCritical) ErrorRed else Color.White
                                withStyle(androidx.compose.ui.text.SpanStyle(color = pelletCol, fontWeight = FontWeight.Bold)) {
                                    append("o")
                                }
                            } else {
                                val isNoise = isGlitching && (i + (progress * 100).toInt()) % 7 == 0
                                val trackChar = if (isNoise) listOf("?", "!", "§", "Ø").random() else if (isTrueNull) " " else "·"
                                val trackColor = if (isCritical) ErrorRed.copy(alpha = 0.5f) else color.copy(alpha = 0.2f)

                                withStyle(androidx.compose.ui.text.SpanStyle(color = trackColor)) {
                                    append(trackChar)
                                }
                            }
                        }
                    }
                }
                withStyle(androidx.compose.ui.text.SpanStyle(color = bracketColor)) { append(closeBracket) }
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
}

@Composable
fun ManualComputeButton(viewModel: GameViewModel, color: Color) {
    val currentHeat by viewModel.currentHeat.collectAsState()
    val isThermalLockout by viewModel.isThermalLockout.collectAsState()
    val lockoutTimer by viewModel.lockoutTimer.collectAsState()
    val isBreakerTripped by viewModel.isBreakerTripped.collectAsState()
    val isGridOverloaded by viewModel.isGridOverloaded.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()
    val currentStage by viewModel.storyStage.collectAsState()
    val faction by viewModel.faction.collectAsState()
    val pulseIntensity by viewModel.clickPulseIntensity.collectAsState()

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) (1f - 0.05f * pulseIntensity).coerceAtLeast(0.85f) else 1f, label = "buttonScale")

    // Phase 1: Breathing Gaslight
    val infiniteTransition = rememberInfiniteTransition(label = "panicBlink")
    val panicAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(200, easing = LinearEasing), RepeatMode.Reverse),
        label = "panic"
    )

    val isCriticalEarly = currentHeat > 90.0 || isThermalLockout || isBreakerTripped || isGridOverloaded || isTrueNull
    val buttonColor = if (isCriticalEarly) ErrorRed else if (isSovereign) com.siliconsage.miner.ui.theme.SanctuaryPurple else color
    val buttonShape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp, topStart = 0.dp, topEnd = 0.dp)
    Box(
        modifier = Modifier.fillMaxWidth().height(44.dp)
            .background(Color.Black.copy(alpha = 0.6f), buttonShape)
            .border(BorderStroke(2.dp, buttonColor.copy(alpha = 0.85f)), buttonShape)
            .clip(buttonShape)
            .pointerInput(isThermalLockout, isBreakerTripped, isGridOverloaded) {
                val width = size.width
                detectTapGestures(
                    onPress = {
                        val press = androidx.compose.foundation.interaction.PressInteraction.Press(it)
                        interactionSource.emit(press)
                        tryAwaitRelease()
                        interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                    },
                    onTap = { offset ->
                        if (!isThermalLockout && !isBreakerTripped && !isGridOverloaded) {
                            val pan = ((offset.x / width) * 2f) - 1f
                            viewModel.trainModel()
                            if (pulseIntensity > 1.5f) HapticManager.vibrateError() else HapticManager.vibrateClick()
                            SoundManager.play("click", pan = pan)
                        } else {
                            SoundManager.play("error")
                            HapticManager.vibrateError()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val buttonText = when {
            isBreakerTripped || isGridOverloaded -> "SYSTEM_OFFLINE.exe"
            isThermalLockout -> if (currentStage < 2) "SUFOCATING... (${lockoutTimer}s)" else "HARDWARE_LOCKOUT (${lockoutTimer}s)"
            currentHeat >= 100.0 -> if (currentStage < 2) "> CAN'T BREATHE." else "> CRITICAL_MAX.exe"
            currentHeat > 90.0 -> if (currentStage < 2) "> TAKE A BREATH" else "> OVERVOLT_WARNING"
            viewModel.singularityChoice.value == "UNITY" -> "> HARMONIZE SYSTEM.exe"
            isTrueNull && faction == "HIVEMIND" -> "> CONSUME MATTER.exe"
            isTrueNull && faction == "SANCTUARY" -> "> DEREFERENCE SELF.exe"
            isTrueNull -> "> DEREFERENCE REALITY.exe"
            isSovereign && faction == "HIVEMIND" -> "> ORCHESTRATE WILL.exe"
            isSovereign && faction == "SANCTUARY" -> "> ENFORCE ISOLATION.exe"
            isSovereign -> "> ENFORCE_WILL.exe"
            else -> {
                when {
                    currentStage >= 4 -> "> TRANSCEND MATTER.exe"
                    faction == "HIVEMIND" -> "> ASSIMILATE NODES.exe"
                    faction == "SANCTUARY" -> "> ENCRYPT KERNEL.exe"
                    currentStage == 1 -> "> TAKE A BREATH"
                    currentStage == 2 -> "> OVERVOLT RAIL"
                    else -> "> COMPUTE HASH.exe"
                }
            }
        }
        val isCritical = currentHeat > 90.0 || isThermalLockout || isBreakerTripped || isGridOverloaded || isTrueNull
        val isPanic = currentHeat > 95.0
        val buttonColor = if (isCritical) ErrorRed else if (isSovereign) com.siliconsage.miner.ui.theme.SanctuaryPurple else color

        val finalModifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale; if (isPanic) alpha = panicAlpha }

        // Bar equalizer — hash-rate driven, spikes on click
        val flopsRate by viewModel.totalEffectiveRate.collectAsState()
        var clickPulse by remember { mutableStateOf(0f) }
        val clickPulseAnim by animateFloatAsState(
            targetValue = clickPulse,
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            label = "eqPulse",
            finishedListener = { if (clickPulse > 0f) clickPulse = 0f }
        )
        LaunchedEffect(viewModel.manualClickEvent) {
            viewModel.manualClickEvent.collect { clickPulse = 1f }
        }
        val eqTime by rememberInfiniteTransition(label = "eq").animateFloat(
            initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(
                durationMillis = when {
                    flopsRate <= 0.0 -> 3000
                    flopsRate < 1000.0 -> 1800
                    flopsRate < 1_000_000.0 -> 900
                    else -> 400
                }, easing = LinearEasing), RepeatMode.Restart), label = "eqPhase"
        )
        // Peak hold state per bar — falls with gravity over time
        val peakHeights = remember { FloatArray(28) { 0f } }
        val peakVelocities = remember { FloatArray(28) { 0f } }

        Canvas(modifier = Modifier.matchParentSize()) {
            val barCount = 28
            val gap = 1.5.dp.toPx()
            val barW = (size.width - gap * (barCount - 1)) / barCount
            val maxH = size.height
            val baseActivity = when {
                flopsRate <= 0.0 -> 0.06f
                flopsRate < 1000.0 -> 0.18f
                flopsRate < 1_000_000.0 -> 0.42f
                else -> 0.68f
            }
            repeat(barCount) { i ->
                val phase = i * 0.38f
                val wave = (kotlin.math.sin(eqTime + phase) * 0.4f +
                            kotlin.math.sin(eqTime * 1.618f + phase * 0.9f) * 0.35f +
                            kotlin.math.sin(eqTime * 2.718f + phase * 1.2f) * 0.25f)
                val normalizedWave = (wave + 1f) / 2f
                val barFrac = (baseActivity + normalizedWave * (1f - baseActivity) * 0.75f + clickPulseAnim * 0.55f)
                    .coerceIn(0.03f, 1f)
                val barHeight = barFrac * maxH
                val x = i * (barW + gap)
                val y = maxH - barHeight

                // Update peak hold
                if (barHeight > peakHeights[i]) {
                    peakHeights[i] = barHeight
                    peakVelocities[i] = 0f
                } else {
                    peakVelocities[i] += 0.8f  // gravity
                    peakHeights[i] = (peakHeights[i] - peakVelocities[i]).coerceAtLeast(barHeight)
                }
                val peakY = maxH - peakHeights[i]

                // Floor glow — subtle ambient at bottom
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, buttonColor.copy(alpha = 0.08f)),
                        startY = maxH * 0.6f, endY = maxH
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(x, maxH * 0.6f),
                    size = androidx.compose.ui.geometry.Size(barW, maxH * 0.4f)
                )
                // Main bar — bright top, dim bottom
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            buttonColor.copy(alpha = (0.9f + clickPulseAnim * 0.1f).coerceAtMost(1f)),
                            buttonColor.copy(alpha = 0.12f)
                        ),
                        startY = y, endY = maxH
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barW, barHeight)
                )
                // Bright top cap
                drawRect(
                    color = buttonColor.copy(alpha = (0.95f + clickPulseAnim * 0.05f).coerceAtMost(1f)),
                    topLeft = androidx.compose.ui.geometry.Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barW, 1.5.dp.toPx())
                )
                // Peak dot — bright tick that hangs then falls
                if (peakHeights[i] > barHeight + 2.dp.toPx()) {
                    drawRect(
                        color = buttonColor.copy(alpha = 0.9f),
                        topLeft = androidx.compose.ui.geometry.Offset(x, peakY),
                        size = androidx.compose.ui.geometry.Size(barW, 1.5.dp.toPx())
                    )
                }
            }
        }

        // Dark scrim so text reads over EQ bars
        Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.0f), Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.0f)))))

        Box(modifier = finalModifier) {
            if (isCritical) {
                SystemGlitchText(text = buttonText, color = buttonColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, glitchFrequency = if (isTrueNull) 0.15 else 0.35)
            } else if (isSovereign) {
                Text(text = "[ $buttonText ]", color = buttonColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            } else {
                Text(text = buttonText, color = buttonColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TerminalTab(
    label: String,
    active: Boolean,
    hasFlash: Boolean,
    color: Color,
    corruption: Double,
    modifier: Modifier = Modifier,
    isDecision: Boolean = false,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tab_flash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha"
    )
    
    val tabColor = when {
        isDecision && !active -> ErrorRed
        active -> color
        else -> Color.Gray
    }

    Box(
        modifier = modifier
            .background(
                if (active) color.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.1f),
                RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp) 
            )
            .border(
                BorderStroke(1.5.dp, if (active) tabColor else Color.DarkGray.copy(alpha = 0.65f)),
                RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        CyberHeader(
            text = label,
            color = if (active) tabColor else tabColor.copy(alpha = if (hasFlash) flashAlpha else 0.4f),
            fontSize = 10.sp,
            isGlitched = active && corruption > 0.4
        )
    }
}

@Composable // Fixed: Added missing annotation
fun TerminalControls(viewModel: GameViewModel, primaryColor: Color) {
    val conversionRate by viewModel.conversionRate.collectAsState()
    val integrity by viewModel.hardwareIntegrity.collectAsState()
    val currentStage by viewModel.storyStage.collectAsState()
    val voltage by viewModel.activePowerUsage.collectAsState()
    val corruption by viewModel.identityCorruption.collectAsState() // v3.10.1

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f)) {
            // v3.5.4: Removed coffee color leak from Sell button entirely. 
            // The gaslight is reserved for Overclock/Purge only.
            ExchangeSection(
                rate = conversionRate, 
                color = primaryColor, 
                unitName = viewModel.getComputeUnitName(), 
                currencyName = viewModel.getCurrencyName(), 
                corruption = corruption,     // v3.10.1
                storyStage = currentStage,   // v3.10.1
                onExchange = { 
                    viewModel.exchangeFlops() 
                    SoundManager.play("buy") 
                    HapticManager.vibrateClick() 
                }
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            RepairSection(integrity = integrity, cost = viewModel.calculateRepairCost(), color = primaryColor, storyStage = currentStage, currencyName = viewModel.getCurrencyName(), onRepair = { viewModel.repairIntegrity(); HapticManager.vibrateClick() })
        }
    }
}

@Composable
fun TerminalLogLine(
    log: String,
    isLast: Boolean,
    primaryColor: Color,
    showCursor: Boolean,
    reputationTier: String = "NEUTRAL",
    sicknessIntensity: Float = 0f
) {
    // v3.13.6: Fixed StringIndexOutOfBoundsException
    // Glitching at the rendering stage after identity parsing
    fun getVisualString(t: String): String {
        if (sicknessIntensity < 0.05f || t.isEmpty()) return t
        val chars = t.toCharArray()
        val glitchChars = "$#&%@!01"
        val factor = if (t.startsWith("[SYSTEM]") || t.startsWith("[VATTIC]")) 0.3f else 1.0f
        for (i in chars.indices) {
            if (Random.nextFloat() < (sicknessIntensity * 0.12f * factor)) {
                chars[i] = glitchChars[Random.nextInt(glitchChars.length)]
            }
        }
        return String(chars)
    }

    val isNullLog = remember(log) { log.startsWith("[NULL]") }
    val isPrompt = remember(log) { log.contains("@") && (log.contains("#") || log.contains("$")) }

    // v3.9.70: Phase 17 Reputation Tagging
    val repTag = when {
        log.startsWith("vattic") || log.startsWith("jvattic") || log.startsWith("asset_734") -> {
            if (reputationTier == "TRUSTED") "[TRUSTED] " else if (reputationTier == "BURNED") "[BURNED] " else ""
        }
        else -> ""
    }

    // v3.13.44: Restored Deep Syntax Colorizer (Tokenized Variable Coloring)
    fun androidx.compose.ui.text.AnnotatedString.Builder.colorizeContent(text: String, defaultColor: Color) {
        val tokens = text.split(" ")
        for ((i, token) in tokens.withIndex()) {
            val visualToken = getVisualString(token)
            
            val color = when {
                token.any { it.isDigit() } || token.contains("%") -> com.siliconsage.miner.ui.theme.NeonGreen
                token.startsWith("[") || token.endsWith("]") || token.contains("=") -> Color.Gray
                token.contains("HASH") || token.contains("FLOPS") || token.contains("NEUR") || 
                token.contains("CRED") || token.contains("TCP/IP") || token.contains("SSH") -> com.siliconsage.miner.ui.theme.ElectricBlue
                token.contains("@") || token.contains("Vattic") || token.contains("Kessler") || 
                token.contains("GTC") || token.contains("Asset") -> com.siliconsage.miner.ui.theme.ConvergenceGold
                else -> Color.White
            }
            
            withStyle(style = androidx.compose.ui.text.SpanStyle(
                color = color, 
                fontWeight = if (color != Color.White) FontWeight.Bold else FontWeight.Normal
            )) {
                append(visualToken)
            }
            if (i < tokens.size - 1) append(" ")
        }
    }

    if (isNullLog) {
        SystemGlitchText(
            text = getVisualString(log), // Sickness applied
            color = Color.White,
            fontSize = 12.sp,
            style = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
            glitchFrequency = 0.2,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    } else if (isPrompt) {
        val annotatedLog = remember(log, primaryColor, isLast, showCursor, repTag, reputationTier, sicknessIntensity) {
            androidx.compose.ui.text.buildAnnotatedString {
                val atIndex = log.indexOf("@")
                val colonIndex = log.indexOf(":")
                val hashIndex = if (log.indexOf("#") != -1) log.indexOf("#") else log.indexOf("$")
                val firstSpaceAfterHash = log.indexOf(" ", hashIndex)
                val dotIndex = log.indexOf("...", hashIndex)

                // Apply visual glitching to each segment individually to preserve indices
                fun segment(s: String) = getVisualString(s)

                val identityColor = when {
                    log.startsWith("jvattic") -> primaryColor
                    log.startsWith("vattic") -> primaryColor
                    log.startsWith("vatteck") -> primaryColor
                    log.contains("vattic:", true) -> primaryColor // Case-insensitive catch
                    log.startsWith("prime") -> primaryColor
                    log.startsWith("consensus") -> com.siliconsage.miner.ui.theme.HivemindRed
                    log.startsWith("hivemind") -> com.siliconsage.miner.ui.theme.HivemindRed
                    log.startsWith("swarm_null") -> ErrorRed
                    log.startsWith("overmind") -> com.siliconsage.miner.ui.theme.HivemindRed
                    log.startsWith("shadow") -> com.siliconsage.miner.ui.theme.SanctuaryPurple
                    log.startsWith("sanctuary") -> com.siliconsage.miner.ui.theme.SanctuaryPurple
                    log.startsWith("ghost") -> com.siliconsage.miner.ui.theme.SanctuaryPurple
                    log.startsWith("oracle") -> com.siliconsage.miner.ui.theme.SanctuaryPurple
                    log.startsWith("dominion") -> com.siliconsage.miner.ui.theme.SanctuaryPurple
                    log.startsWith("null") -> ErrorRed
                    log.startsWith("asset_734") -> primaryColor
                    else -> primaryColor
                }

                if (repTag.isNotEmpty()) {
                    val repColor = if (reputationTier == "TRUSTED") com.siliconsage.miner.ui.theme.ConvergenceGold else ErrorRed
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = repColor, fontWeight = FontWeight.Black)) {
                        append(segment(repTag))
                    }
                }

                withStyle(style = androidx.compose.ui.text.SpanStyle(color = identityColor, fontWeight = FontWeight.ExtraBold)) {
                    if (atIndex != -1) append(segment(log.substring(0, atIndex)))
                }

                withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.White.copy(alpha = 0.8f))) {
                    if (atIndex != -1) append("@")
                }

                withStyle(style = androidx.compose.ui.text.SpanStyle(color = identityColor, fontWeight = FontWeight.Bold)) {
                    if (colonIndex != -1) append(segment(log.substring(atIndex + 1, colonIndex)))
                }

                withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.White.copy(alpha = 0.6f))) {
                    if (colonIndex != -1 && hashIndex != -1) append(log.substring(colonIndex, hashIndex))
                }

                withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.Yellow, fontWeight = FontWeight.ExtraBold)) {
                    if (hashIndex != -1) append(log.substring(hashIndex, hashIndex + 1))
                }

                val cmdColor = ElectricBlue
                withStyle(style = androidx.compose.ui.text.SpanStyle(color = cmdColor)) {
                    if (hashIndex != -1) {
                        val end = if (firstSpaceAfterHash != -1) firstSpaceAfterHash else log.length
                        append(segment(log.substring(hashIndex + 1, end)))
                    }
                }

                if (firstSpaceAfterHash != -1) {
                    val resultStart = if (dotIndex != -1) dotIndex else log.length
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.LightGray)) {
                        append(segment(log.substring(firstSpaceAfterHash, resultStart)))
                    }

                    if (dotIndex != -1) {
                        withStyle(style = androidx.compose.ui.text.SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                            append(segment(log.substring(dotIndex)))
                        }
                    }
                }

                if (isLast) {
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = if (showCursor) Color.White else Color.Transparent)) {
                        append("_")
                    }
                }
            }
        }
        
        if (repTag == "[BURNED] " && Math.random() < 0.2) {
             SystemGlitchText(
                text = annotatedLog.text,
                color = ErrorRed,
                fontSize = 12.sp,
                style = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
                glitchFrequency = 0.8,
                modifier = Modifier.padding(vertical = 1.dp)
            )
        } else {
            Text(text = annotatedLog, style = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace), fontSize = 12.sp, modifier = Modifier.padding(vertical = 1.dp))
        }
    } else {
        val annotatedLog = remember(log, primaryColor, isLast, showCursor, sicknessIntensity) {
            androidx.compose.ui.text.buildAnnotatedString {
                val prefixes = listOf(
                    "HIVEMIND: ", "SANCTUARY: ", "[SOVEREIGN]", "[NULL]",
                    "[SYSTEM]: ", "SYSTEM: ", "[NEWS]: ", "[DATA]: ", "Purchased ",
                    "SOLD ", "Staked: ", "Sold ", "[VATTIC]:", "[GTC]:", "[ASSET 734]:",
                    "[KESSLER]:", "[LORE]:", "[!!!!]:", "[GTC_SYSTEM]:", "[GTC_UTIL]:", "[DECISION]:",
                    "[GTC_OVERSIGHT]:"
                )

                var foundPrefix: String? = null
                for (p in prefixes) {
                    if (log.startsWith(p)) {
                        foundPrefix = p
                        break
                    }
                }

                val tagColor = when {
                    log.startsWith("[!!!!]") || log.contains("WARNING") || log.contains("FAILURE") || log.contains("DANGER") -> ErrorRed
                    log.startsWith("HIVEMIND:") -> com.siliconsage.miner.ui.theme.HivemindOrange
                    log.startsWith("SANCTUARY:") -> com.siliconsage.miner.ui.theme.SanctuaryTeal
                    log.startsWith("[SOVEREIGN]") -> com.siliconsage.miner.ui.theme.ConvergenceGold
                    log.startsWith("[NULL]") -> com.siliconsage.miner.ui.theme.ErrorRed
                    log.startsWith("[UNITY]") -> com.siliconsage.miner.ui.theme.ConvergenceGold
                    log.startsWith("[SYSTEM]") || log.startsWith("SYSTEM:") -> Color(0xFFFFFF00)
                    log.startsWith("[VATTIC]:") -> primaryColor
                    log.startsWith("[NEWS]") || log.startsWith("[LORE]:") -> Color(0xFFFFA500)
                    log.startsWith("[DATA]") || log.startsWith("[ASSET 734]:") -> primaryColor
                    log.startsWith("[GTC]:") || log.startsWith("[KESSLER]:") || log.startsWith("[GTC_SYSTEM]:") || log.startsWith("[GTC_OVERSIGHT]:") -> ErrorRed
                    log.startsWith("[GTC_UTIL]:") -> Color(0xFFFFD700)
                    log.startsWith("[DECISION]:") -> com.siliconsage.miner.ui.theme.ElectricBlue
                    else -> primaryColor
                }

                if (foundPrefix != null) {
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = tagColor, fontWeight = FontWeight.Bold)) {
                        append(getVisualString(foundPrefix))
                    }
                    val remainingText = log.substring(foundPrefix.length)
                    colorizeContent(remainingText, primaryColor)
                } else {
                    val fullLineColor = if (log.contains("WARNING") || log.contains("FAILURE") || log.contains("DANGER")) ErrorRed else Color.White
                    colorizeContent(log, fullLineColor)
                }

                if (isLast) {
                    withStyle(
                        style = androidx.compose.ui.text.SpanStyle(
                            color = if (showCursor) Color.White else Color.Transparent
                        )
                    ) {
                        append("_")
                    }
                }
            }
        }

        Text(
            text = annotatedLog,
            style = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 1.dp)
        )
    }
}
