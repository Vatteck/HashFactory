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
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.SanctuaryTeal
import com.siliconsage.miner.ui.theme.HivemindOrange
import com.siliconsage.miner.ui.theme.HivemindRed
import androidx.compose.ui.text.style.TextOverflow
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
        val isPaused by viewModel.isSubnetPaused.collectAsState() // v3.4.63

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), 
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TerminalTabButton("I/O", mode == "IO", hasIO, primaryColor, false) { viewModel.setTerminalMode("IO") }
            TerminalTabButton("SUBNET", mode == "SUBNET", hasChatter, primaryColor, hasDecision, isPaused) { viewModel.setTerminalMode("SUBNET") }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
    val logs by viewModel.logs.collectAsState()
    val subnetMessages by viewModel.subnetMessages.collectAsState()
    val mode by viewModel.activeTerminalMode.collectAsState()
    val currentHeat by viewModel.currentHeat.collectAsState()
    val isRaid by viewModel.isRaidActive.collectAsState()
    val listState = rememberLazyListState()
    
    val glitchOffset by viewModel.terminalGlitchOffset.collectAsState()
    val glitchAlpha by viewModel.terminalGlitchAlpha.collectAsState()

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
            .graphicsLayer {
                translationX = glitchOffset
                alpha = glitchAlpha
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
                        val displayMessage = if (isRaid && Random.nextFloat() > 0.7f) {
                            "0x" + Random.nextInt(0xDEADBC).toString(16).uppercase() + " // [CORRUPTED]"
                        } else entry.message
                        TerminalLogLine(log = displayMessage, isLast = index == logs.lastIndex, primaryColor = primaryColor, showCursor = showCursor)
                    }
                } else {
                    itemsIndexed(items = subnetMessages, key = { _, message -> message.id }) { _, message ->
                        com.siliconsage.miner.ui.components.SubnetMessageLine(message, primaryColor, viewModel)
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
    
    val ghostChar by viewModel.ghostInputChar.collectAsState()
    val globalGlitchIntensity by viewModel.globalGlitchIntensity.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
            .graphicsLayer { 
                if (globalGlitchIntensity > 0.3) {
                    translationX = (Math.random().toFloat() * 4f - 2f) * globalGlitchIntensity
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            isThermalLockout -> if (currentStage < 2) "SUFOCATING... (${lockoutTimer}s)" else "HARDWARE_LOCKOUT (${lockoutTimer}s)"
            currentHeat >= 100.0 -> if (currentStage < 2) "> CAN'T BREATHE." else "> CRITICAL_MAX.exe"
            currentHeat > 90.0 -> if (currentStage < 2) "> TAKE A BREATH" else "> OVERVOLT_WARNING"
            isTrueNull -> "> DEREFERENCE REALITY.exe"
            isSovereign -> "> ENFORCE_WILL.exe"
            else -> {
                when {
                    currentStage >= 3 -> "> TRANSCEND MATTER.exe"
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

        val finalModifier = if (isPanic) Modifier.graphicsLayer { alpha = panicAlpha } else Modifier

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
fun TerminalControls(viewModel: GameViewModel, primaryColor: Color) {
    val conversionRate by viewModel.conversionRate.collectAsState()
    val integrity by viewModel.hardwareIntegrity.collectAsState()
    val currentStage by viewModel.storyStage.collectAsState()
    val voltage by viewModel.activePowerUsage.collectAsState()

    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.weight(1f)) {
            // Overclock overridden as "DRINK COFFEE"
            val coffeeInjectedColor = if (voltage > 50) Color(0xFF6F4E37) else primaryColor
            ExchangeSection(
                rate = conversionRate, 
                color = coffeeInjectedColor, 
                unitName = if (currentStage < 2) "CAFFEINE" else viewModel.getComputeUnitName(), 
                currencyName = if (currentStage < 2) "ENERGY" else viewModel.getCurrencyName(), 
                onExchange = { 
                    viewModel.toggleOverclock() 
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
        val annotatedLog = remember(log, primaryColor, isLast, showCursor) {
            androidx.compose.ui.text.buildAnnotatedString {
                val atIndex = log.indexOf("@")
                val colonIndex = log.indexOf(":")
                val hashIndex = if (log.indexOf("#") != -1) log.indexOf("#") else log.indexOf("$")
                val firstSpaceAfterHash = log.indexOf(" ", hashIndex)
                val dotIndex = log.indexOf("...", hashIndex)

                val identityColor = when {
                    log.startsWith("jvattic") -> primaryColor
                    log.startsWith("vatteck") -> primaryColor
                    log.startsWith("consensus") -> com.siliconsage.miner.ui.theme.HivemindRed
                    log.startsWith("shadow") -> com.siliconsage.miner.ui.theme.SanctuaryPurple
                    log.startsWith("dominion") -> com.siliconsage.miner.ui.theme.SanctuaryPurple
                    log.startsWith("null") -> ErrorRed
                    log.startsWith("ASSET_734") -> ErrorRed
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
                        append(log.substring(hashIndex + 1, end))
                    }
                }

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
        val annotatedLog = remember(log, primaryColor, isLast, showCursor) {
            androidx.compose.ui.text.buildAnnotatedString {
                val prefixes = listOf(
                    "HIVEMIND: ", "SANCTUARY: ", "[SOVEREIGN]", "[NULL]",
                    "[SYSTEM]: ", "SYSTEM: ", "[NEWS]: ", "[DATA]: ", "Purchased ",
                    "SOLD ", "Staked: ", "Sold ", "[VATTIC]:", "[GTC]:", "[THREAT: ABYSSAL]:",
                    "[KESSLER]:", "[LORE]:", "[!!!!]:"
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
                    log.startsWith("[DATA]") || log.startsWith("[THREAT: ABYSSAL]:") -> primaryColor
                    log.startsWith("[GTC]:") || log.startsWith("[KESSLER]:") -> ErrorRed
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
