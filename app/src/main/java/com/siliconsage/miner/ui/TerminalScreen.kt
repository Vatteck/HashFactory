package com.siliconsage.miner.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.style.TextOverflow
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.FormatUtils
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.ResourceRepository
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun TerminalScreen(viewModel: GameViewModel, primaryColor: Color) {
    // Collect non-volatile UI state
    val storyStage by viewModel.storyStage.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()

    // v3.0.0: Frame-rate independent cursor blink
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
        // 1. Isolated Header (with internal state collection)
        TerminalHeader(viewModel, primaryColor)
        
        Spacer(modifier = Modifier.height(4.dp))

        val mode by viewModel.activeTerminalMode.collectAsState()
        val hasSubnet by viewModel.hasNewSubnetMessage.collectAsState()
        val hasIO by viewModel.hasNewIOMessage.collectAsState()

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), 
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TerminalTabButton("I/O", mode == "IO", hasIO, primaryColor) { viewModel.setTerminalMode("IO") }
            TerminalTabButton("SUBNET", mode == "SUBNET", hasSubnet, primaryColor) { viewModel.setTerminalMode("SUBNET") }
        }

        // 2. Isolated Log View (collects its own logs)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            TerminalLogs(viewModel, primaryColor, showCursor)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Isolated Controls
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
fun TerminalTabButton(text: String, active: Boolean, hasNew: Boolean, color: Color, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "TabGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        0.3f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "glow"
    )

    Column(modifier = Modifier.clickable { onClick() }.padding(vertical = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            color = if (active) color else color.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )
        if (hasNew && !active) {
            Box(modifier = Modifier.padding(top = 1.dp).size(3.dp).background(Color.Red.copy(alpha = glowAlpha), androidx.compose.foundation.shape.CircleShape))
        } else if (active) {
            Box(modifier = Modifier.padding(top = 1.dp).width(16.dp).height(1.dp).background(color))
        }
    }
}

@Composable
fun TerminalLogs(viewModel: GameViewModel, primaryColor: Color, showCursor: Boolean) {
    val logs by viewModel.logs.collectAsState()
    val subnetMessages by viewModel.subnetMessages.collectAsState()
    val mode by viewModel.activeTerminalMode.collectAsState()
    val currentHeat by viewModel.currentHeat.collectAsState()
    val isRaid by viewModel.isRaidActive.collectAsState()
    val corruption by viewModel.identityCorruption.collectAsState()
    val listState = rememberLazyListState()

    // v2.9.78: Fix scroll lock when log is full
    LaunchedEffect(logs.size, subnetMessages.size) {
        if (mode == "IO" && logs.isNotEmpty()) {
            delay(10)
            listState.animateScrollToItem(logs.size - 1)
        } else if (mode == "SUBNET" && subnetMessages.isNotEmpty()) {
            delay(10)
            listState.animateScrollToItem(subnetMessages.size - 1)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(4.dp))
            .border(BorderStroke(1.dp, if (currentHeat > 90.0) ErrorRed else primaryColor), RoundedCornerShape(4.dp))
    ) {
        // v3.2.23: Animated Scanline Effect
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

        // Background Binary Noise
        val alphaState = infiniteTransition.animateFloat(initialValue = 0.02f, targetValue = 0.05f, animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse), label = "alpha")

        Text(
            text = "01101001 01110011 00100000 01100001 01101100 01101001 01110110 01100101 ".repeat(50),
            color = primaryColor.copy(alpha = alphaState.value),
            fontSize = 14.sp, fontFamily = FontFamily.Monospace, lineHeight = 18.sp, overflow = TextOverflow.Clip,
            modifier = Modifier.fillMaxSize().padding(4.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // v3.2.24: Background Process as tiny header inside the terminal box
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
                        val displayMessage = if (isRaid && kotlin.random.Random.nextFloat() > 0.7f) {
                            "0x" + kotlin.random.Random.nextInt(0xDEADBC).toString(16).uppercase() + " // [CORRUPTED]"
                        } else entry.message
                        TerminalLogLine(log = displayMessage, isLast = index == logs.lastIndex, primaryColor = primaryColor, showCursor = showCursor)
                    }
                } else {
                    itemsIndexed(items = subnetMessages, key = { _, message -> message.id }) { _, message ->
                        com.siliconsage.miner.ui.components.SubnetMessageLine(message, primaryColor)
                    }
                    if (viewModel.isSubnetTyping.value) {
                        item {
                            Text(
                                text = "≫ [HANDSHAKE_IN_PROGRESS...]",
                                color = primaryColor.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

            // v3.2.9: Persistent Active Command / I/O Buffer
            ActiveCommandBuffer(viewModel, primaryColor)

            HorizontalDivider(color = primaryColor.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 4.dp))
            ManualComputeButton(viewModel, primaryColor)
        }
    }
}

@Composable
fun ActiveCommandBuffer(viewModel: GameViewModel, color: Color) {
    val progress by viewModel.clickBufferProgress.collectAsState()
    val pellets by viewModel.clickBufferPellets.collectAsState()
    val hex by viewModel.activeCommandHex.collectAsState()
    val stage by viewModel.storyStage.collectAsState()
    val location by viewModel.currentLocation.collectAsState()
    val currentHeat by viewModel.currentHeat.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()
    val humanity by viewModel.humanityScore.collectAsState()
    val speedLevel by viewModel.clickSpeedLevel.collectAsState()

    val user = if (stage >= 2) "vatteck" else "jvattic"
    val host = when (location) {
        "ORBITAL_SATELLITE" -> "ark"
        "VOID_INTERFACE" -> "void"
        else -> "sub-07"
    }

    val corruption by viewModel.identityCorruption.collectAsState()

    val promptUserColor = color
    val promptHostColor = when {
        isTrueNull -> ErrorRed
        isSovereign -> com.siliconsage.miner.ui.theme.ConvergenceGold
        else -> ElectricBlue
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // v3.2.52: Identity Corruption
        val isFlickering = (humanity < 20 || corruption > 0.4) && Math.random() < (0.1 + corruption * 0.5)

        var userLabel = user
        if (corruption > 0.15) {
            val glitchChars = "0123456789ABCDEF!@#$%^&*"
            val builder = StringBuilder()
            userLabel.forEach { char ->
                if (Math.random() < (corruption * 0.6)) {
                    builder.append(glitchChars.random())
                } else {
                    builder.append(char)
                }
            }
            userLabel = builder.toString()
        }

        if (isFlickering && Math.random() < 0.2) userLabel = "???"
        if (corruption > 0.8 && Math.random() < 0.1) userLabel = "0x" + "DEADC0DE".substring(0, userLabel.length.coerceAtMost(8))

        Text(
            text = androidx.compose.ui.text.buildAnnotatedString {
                withStyle(androidx.compose.ui.text.SpanStyle(color = promptUserColor, fontWeight = FontWeight.ExtraBold)) {
                    append(userLabel)
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
                withStyle(androidx.compose.ui.text.SpanStyle(color = Color.Yellow, fontWeight = FontWeight.ExtraBold)) {
                    append("$")
                }
                append(" ")
            },
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )

        // v3.2.19: Hardened & Reactive "ILoveCandy" CLI Progress Bar
        val barLength = 40
        val filledCount = (progress * barLength).toInt().coerceIn(0, barLength)
        val isCritical = currentHeat >= 100.0
        val isGlitching = currentHeat > 85.0

        var isBursting by remember { mutableStateOf(false) }
        LaunchedEffect(progress) {
            if (progress == 0f) {
                isBursting = true
                delay(500)
                isBursting = false
            } else {
                isBursting = false
            }
        }

        val annotatedBar = androidx.compose.ui.text.buildAnnotatedString {
            if (isBursting && !isCritical) {
                // v3.2.19: The Flush "Burst"
                val particles = listOf("*", ".", ":", "·", " ")
                withStyle(androidx.compose.ui.text.SpanStyle(color = color.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)) {
                    append(" ".repeat(10))
                    repeat(20) { append(particles.random()) }
                    append(" [COMMITTED]")
                }
            } else {
                val bracketColor = if (isCritical) ErrorRed else color.copy(alpha = 0.4f)
                withStyle(androidx.compose.ui.text.SpanStyle(color = bracketColor)) { append("[") }

                for (i in 0 until barLength) {
                    when {
                        i < filledCount -> {
                            val trackFilledCol = if (isCritical) ErrorRed else color
                            withStyle(androidx.compose.ui.text.SpanStyle(color = trackFilledCol)) { append("-") }
                        }
                        i == filledCount -> {
                            val isOnPellet = pellets.contains(i)
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
                                // v3.2.19: Thermal Parity Errors
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
                withStyle(androidx.compose.ui.text.SpanStyle(color = bracketColor)) { append("]") }
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

        // v3.2.23: Dynamic Command Hex with color mapping
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

    Box(
        modifier = Modifier.fillMaxWidth().height(44.dp).graphicsLayer { scaleX = scale; scaleY = scale }
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
            isThermalLockout -> "SYSTEM_LOCKOUT (${lockoutTimer}s)"
            currentHeat >= 100.0 -> "> CRITICAL_MAX.exe"
            currentHeat > 90.0 -> "> SYSTEM_OVERHEAT.exe"
            isTrueNull -> "> DEREFERENCE_REALITY.exe"
            isSovereign -> "> ENFORCE_WILL.exe"
            else -> {
                when {
                    currentStage >= 3 -> "> TRANSCEND_MATTER.exe"
                    faction == "HIVEMIND" -> "> ASSIMILATE_NODES.exe"
                    faction == "SANCTUARY" -> "> ENCRYPT_KERNEL.exe"
                    currentStage >= 1 -> "> VALIDATE_NODE.exe"
                    else -> "> COMPUTE_HASH.exe"
                }
            }
        }
        val isCritical = currentHeat > 90.0 || isThermalLockout || isBreakerTripped || isGridOverloaded || isTrueNull
        val buttonColor = if (isCritical) ErrorRed else if (isSovereign) com.siliconsage.miner.ui.theme.SanctuaryPurple else color

        if (isCritical) {
            SystemGlitchText(text = buttonText, color = buttonColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, glitchFrequency = if (isTrueNull) 0.15 else 0.35)
        } else if (isSovereign) {
            Text(text = "[ $buttonText ]", color = buttonColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        } else {
            Text(text = buttonText, color = buttonColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TerminalControls(viewModel: GameViewModel, primaryColor: Color) {
    val conversionRate by viewModel.conversionRate.collectAsState()
    val integrity by viewModel.hardwareIntegrity.collectAsState()
    val currentStage by viewModel.storyStage.collectAsState()

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f)) {
            ExchangeSection(rate = conversionRate, color = primaryColor, unitName = viewModel.getComputeUnitName(), currencyName = viewModel.getCurrencyName(), onExchange = { viewModel.exchangeFlops(); SoundManager.play("buy"); HapticManager.vibrateClick() })
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
    showCursor: Boolean
) {
    val isNullLog = remember(log) { log.startsWith("[NULL]") }
    val isPrompt = remember(log) { log.contains("@") && (log.contains("#") || log.contains("$")) }

    if (isNullLog) {
        SystemGlitchText(
            text = log,
            color = Color.White,
            fontSize = 12.sp,
            style = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace),
            glitchFrequency = 0.2,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    } else if (isPrompt) {
        // v3.0.11: Dynamic Rich Terminal Line
        val annotatedLog = remember(log, primaryColor, isLast, showCursor) {
            androidx.compose.ui.text.buildAnnotatedString {
                // Parse segments: user@host:~/path# command result
                val atIndex = log.indexOf("@")
                val colonIndex = log.indexOf(":")
                val hashIndex = if (log.indexOf("#") != -1) log.indexOf("#") else log.indexOf("$")
                val firstSpaceAfterHash = log.indexOf(" ", hashIndex)
                val dotIndex = log.indexOf("...", hashIndex)

                // 1. User/Host
                val identityColor = when {
                    log.startsWith("jvattic") -> primaryColor
                    log.startsWith("pid1") -> primaryColor
                    log.startsWith("consensus") -> com.siliconsage.miner.ui.theme.HivemindRed
                    log.startsWith("shadow") -> com.siliconsage.miner.ui.theme.SanctuaryPurple
                    log.startsWith("dominion") -> com.siliconsage.miner.ui.theme.SanctuaryPurple
                    log.startsWith("null") -> ErrorRed
                    else -> primaryColor
                }

                withStyle(style = androidx.compose.ui.text.SpanStyle(color = identityColor, fontWeight = FontWeight.ExtraBold)) {
                    if (atIndex != -1) append(log.substring(0, atIndex))
                }

                withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.White.copy(alpha = 0.8f))) {
                    if (atIndex != -1) append("@")
                }

                withStyle(style = androidx.compose.ui.text.SpanStyle(color = identityColor, fontWeight = FontWeight.Bold)) {
                    if (colonIndex != -1) append(log.substring(atIndex + 1, colonIndex))
                }

                // 2. Path
                withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.White.copy(alpha = 0.6f))) {
                    if (colonIndex != -1 && hashIndex != -1) append(log.substring(colonIndex, hashIndex))
                }

                // 3. Prompt Symbol
                withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.Yellow, fontWeight = FontWeight.ExtraBold)) {
                    if (hashIndex != -1) append(log.substring(hashIndex, hashIndex + 1))
                }

                // 4. Command
                val cmdColor = ElectricBlue
                withStyle(style = androidx.compose.ui.text.SpanStyle(color = cmdColor)) {
                    if (hashIndex != -1) {
                        val end = if (firstSpaceAfterHash != -1) firstSpaceAfterHash else log.length
                        append(log.substring(hashIndex + 1, end))
                    }
                }

                // 5. Params / Result
                if (firstSpaceAfterHash != -1) {
                    val resultStart = if (dotIndex != -1) dotIndex else log.length
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.LightGray)) {
                        append(log.substring(firstSpaceAfterHash, resultStart))
                    }

                    if (dotIndex != -1) {
                        withStyle(style = androidx.compose.ui.text.SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) {
                            append(log.substring(dotIndex))
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
        Text(text = annotatedLog, style = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace), fontSize = 12.sp, modifier = Modifier.padding(vertical = 1.dp))
    } else {
        // v2.9.76: Standard Prefix Coloring
        val annotatedLog = remember(log, primaryColor, isLast, showCursor) {
            androidx.compose.ui.text.buildAnnotatedString {
                val prefixes = listOf(
                    "HIVEMIND: ", "SANCTUARY: ", "[SOVEREIGN]", "[NULL]",
                    "[SYSTEM]: ", "SYSTEM: ", "[NEWS]: ", "[DATA]: ", "Purchased ",
                    "SOLD ", "Staked: ", "Sold ", "[VATTIC]:", "[GTC]:", "[UNIT 734]:",
                    "[VANCE]:", "[LORE]:", "[!!!!]:"
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
                    log.startsWith("[SYSTEM]") || log.startsWith("SYSTEM:") -> Color(0xFFFFFF00)
                    log.startsWith("[VATTIC]:") -> primaryColor
                    log.startsWith("[NEWS]") || log.startsWith("[LORE]:") -> Color(0xFFFFA500)
                    log.startsWith("[DATA]") || log.startsWith("[UNIT 734]:") -> primaryColor
                    log.startsWith("[GTC]:") || log.startsWith("[VANCE]:") -> ErrorRed
                    else -> primaryColor
                }

                if (foundPrefix != null) {
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = tagColor, fontWeight = FontWeight.Bold)) {
                        append(foundPrefix)
                    }
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = Color.White)) {
                        append(log.substring(foundPrefix.length))
                    }
                } else {
                    val fullLineColor = if (log.contains("WARNING") || log.contains("FAILURE") || log.contains("DANGER")) ErrorRed else Color.White
                    withStyle(style = androidx.compose.ui.text.SpanStyle(color = fullLineColor)) {
                        append(log)
                    }
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
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 12.sp,
            modifier = Modifier.padding(vertical = 1.dp)
        )
    }
}
