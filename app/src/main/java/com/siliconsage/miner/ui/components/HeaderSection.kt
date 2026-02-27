package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Opacity
import com.siliconsage.miner.ui.ResourceDisplay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.HudTheme
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.math.abs

@Composable
fun HeaderSection(
    viewModel: GameViewModel,
    color: Color,
    onToggleOverclock: () -> Unit,
    onPurge: () -> Unit,
    onRepair: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverclocked by viewModel.isOverclocked.collectAsState()
    val isPurging by viewModel.isPurgingHeat.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()
    val isBreachActive by viewModel.isBreachActive.collectAsState()
    val isAuditActive by viewModel.isAuditChallengeActive.collectAsState()
    val isThermalLockout by viewModel.isThermalLockout.collectAsState()
    val isBreakerTripped by viewModel.isBreakerTripped.collectAsState()
    val lockoutTimer by viewModel.lockoutTimer.collectAsState()
    val faction by viewModel.faction.collectAsState()
    val storyStage by viewModel.storyStage.collectAsState()
    val systemTitle by viewModel.systemTitle.collectAsState()
    val playerTitle by viewModel.playerTitle.collectAsState()
    val playerRank by viewModel.playerRankTitle.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val singularityChoice by viewModel.singularityChoice.collectAsState()
    val corruption by viewModel.identityCorruption.collectAsState()
    val reputationTier by viewModel.reputationTier.collectAsState()
    var showUtilitiesPanel by remember { mutableStateOf(false) }

    val hudTheme = remember(faction, singularityChoice, storyStage, corruption) {
        HudTheme.resolve(faction, singularityChoice, storyStage, corruption)
    }

    val currentHeatState = viewModel.currentHeat.collectAsState()
    val heatRateState = viewModel.heatGenerationRate.collectAsState()
    val powerState = viewModel.activePowerUsage.collectAsState()
    val waterUsageState = viewModel.waterUsage.collectAsState()
    val aquiferLevelState = viewModel.aquiferLevel.collectAsState()
    val waterEfficiencyState = viewModel.waterEfficiencyMultiplier.collectAsState()
    val maxPowerState = viewModel.maxPowerkW.collectAsState()
    val localGenState = viewModel.localGenerationkW.collectAsState()
    val flopsRateState = viewModel.totalEffectiveRate.collectAsState()
    val integrityState = viewModel.hardwareIntegrity.collectAsState()
    val securityLevel by viewModel.securityLevel.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "kinetic_hud")
    val flickerAlphaState = infiniteTransition.animateFloat(0.9f, 1.0f, infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "voltage_droop")

    val manualClickFlow = viewModel.manualClickEvent
    val joltAnim = remember { Animatable(0f) }
    val pulseIntensity by viewModel.clickPulseIntensity.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(manualClickFlow) {
        manualClickFlow.collect {
            val current = joltAnim.value
            joltAnim.snapTo((current + 0.6f * pulseIntensity).coerceAtMost(1.5f))

            if (viewModel.isSignalClear.value) {
                com.siliconsage.miner.util.HapticManager.vibrateClick()
                viewModel.globalGlitchIntensity.value = (viewModel.globalGlitchIntensity.value + 0.05f).coerceAtMost(0.4f)
                coroutineScope.launch {
                    delay(200)
                    viewModel.globalGlitchIntensity.value = (viewModel.globalGlitchIntensity.value - 0.05f).coerceAtLeast(0f)
                }
            }
            joltAnim.animateTo(0f, animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium))
        }
    }

    val heartbeatAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "thermal_heartbeat"
    )
    val lockoutFade = if (isThermalLockout) 0.3f else 1.0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
            .graphicsLayer { clip = false }
            .drawBehind {
                val w = this.size.width; val h = this.size.height; val flickerAlpha = flickerAlphaState.value
                val timeMillis = System.currentTimeMillis() % 10000000L
                val globalTime = timeMillis.toDouble() / 1000.0

                val flopsRate = flopsRateState.value
                val currentPower = powerState.value
                val currentMax = maxPowerState.value
                val currentHeat = currentHeatState.value
                val currentGen = localGenState.value

                val railW = 4.dp.toPx()
                val segmentCount = 12
                val segmentH = h / segmentCount

                val genFactor = (currentGen / currentMax).coerceIn(0.0, 1.0).toFloat()
                val drawFactor = (currentPower / currentMax).coerceIn(0.0, 1.0).toFloat()
                val netFactor = (drawFactor - genFactor).coerceAtLeast(0f)
                val isOffGrid = currentGen >= currentPower && currentPower > 0f

                drawRect(color = Color.DarkGray.copy(alpha = 0.1f), topLeft = Offset(0f, 0f), size = Size(railW, h))
                drawRect(color = Color.DarkGray.copy(alpha = 0.1f), topLeft = Offset(w - railW, 0f), size = Size(railW, h))

                for (i in 0 until segmentCount) {
                    val yPos = h - (i + 1) * segmentH
                    val segFactor = i.toFloat() / segmentCount

                    val segmentColor = when {
                        isOffGrid && segFactor < drawFactor -> ElectricBlue.copy(alpha = 0.9f)
                        segFactor < genFactor -> ElectricBlue.copy(alpha = 0.85f)
                        segFactor < drawFactor -> {
                            val netAlpha = if (netFactor > 0.9f) flickerAlpha else 0.85f
                            if (drawFactor > 0.9f) ErrorRed.copy(alpha = netAlpha)
                            else Color(0xFFFFD700).copy(alpha = netAlpha)
                        }
                        else -> null
                    }

                    if (segmentColor != null) {
                        val glowW = railW * 6f
                        val glowAlpha = if (drawFactor > 0.9f) 0.15f else 0.08f
                        
                        // Left rail glow (Gradient)
                        drawRect(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(segmentColor.copy(alpha = glowAlpha), Color.Transparent),
                                startX = 0f,
                                endX = glowW
                            ),
                            topLeft = Offset(0f, yPos),
                            size = Size(glowW, segmentH)
                        )
                        
                        // Right rail glow (Gradient)
                        drawRect(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(Color.Transparent, segmentColor.copy(alpha = glowAlpha)),
                                startX = w - glowW,
                                endX = w
                            ),
                            topLeft = Offset(w - glowW, yPos),
                            size = Size(glowW, segmentH)
                        )

                        drawRect(color = segmentColor, topLeft = Offset(0f, yPos + 1.dp.toPx()), size = Size(railW, segmentH - 2.dp.toPx()))
                        drawRect(color = segmentColor, topLeft = Offset(w - railW, yPos + 1.dp.toPx()), size = Size(railW, segmentH - 2.dp.toPx()))

                        if (drawFactor > 0.95f && segFactor >= genFactor && Random.nextFloat() > 0.92f) {
                            val sparkX = if (Random.nextBoolean()) railW else w - railW
                            drawCircle(color = Color.White, radius = Random.nextFloat() * 3.dp.toPx(), center = Offset(sparkX + (Random.nextFloat() - 0.5f) * 10f, yPos + segmentH / 2))
                        }
                    }
                }

                val actSpeed = (0.2f + (flopsRate / 10000.0).coerceIn(0.0, 1.0).toFloat()) * (if (isOverclocked) 1.5f else 1.0f)
                val dotSpacing = 2.dp.toPx() + 4.dp.toPx()
                val sideMargin = 8.dp.toPx() // Proposal 11: Side margin to prevent border bleed
                val availableW = w - (sideMargin * 2)
                val dotCount = (availableW / dotSpacing).toInt()

                for (i in 0 until dotCount) {
                    val xPos = sideMargin + i * dotSpacing; val posFac = xPos / w
                    var ledBaseCol = color
                    if (!isBreachActive) {
                        if (currentHeat > 90.0) ledBaseCol = ErrorRed
                        else if (currentHeat > 60.0 && abs(posFac - 0.5) * 2.0 < (currentHeat - 60.0) / 30.0) ledBaseCol = Color(0xFFFFA500)
                    }

                    fun getRipple(row: Int): Float {
                        if (isBreachActive) return if (Math.sin(globalTime * 15.0 + i * 0.4 + row * Math.PI).toFloat() > 0) 1f else 0.1f
                        if (isAuditActive) return if (((globalTime * 12.0).toInt() + i % 2 + row) % 2 == 0) 0.8f else 0.2f
                        if (corruption > 0.5 && Random.nextFloat() > 0.998f) return 1.0f
                        val t = (globalTime + (if (row == 0) 0.0 else 123.456)) * actSpeed * (if (row == 0) 1.0 else 0.73205081)
                        val s = if (row == 0) 1.0 else -1.27
                        val res = (Math.sin(t + i * 0.41 * s) + Math.sin(t * 1.61803398 - i * 0.31 * s) + Math.sin(t * 2.71828182 + i * 0.57 * s)) / 3.0
                        val threshold = 0.58 - (actSpeed * 0.15)
                        return if (res > threshold) ((res - 0.4) / 0.6).toFloat().coerceIn(0f, 1f) else 0.05f
                    }

                    val rippleT = getRipple(0); val rippleB = getRipple(1)
                    val jitter = if (isOverclocked) (Math.sin(globalTime * 50.0 + i).toFloat() * 0.2f) else 0f
                    val alphaT = (0.35f + rippleT * 0.75f + joltAnim.value * 0.85f + jitter).coerceIn(0f, 1f)
                    val alphaB = (0.35f + rippleB * 0.75f + joltAnim.value * 0.85f + jitter).coerceIn(0f, 1f)

                    val colT = if (isOverclocked && rippleT > 0.8 && !isBreachActive) Color.White.copy(alpha = 0.8f) else (if (isBreachActive) ErrorRed else ledBaseCol)
                    val colB = if (isOverclocked && rippleB > 0.8 && !isBreachActive) Color.White.copy(alpha = 0.8f) else (if (isBreachActive) ErrorRed else ledBaseCol)

                    val dotR = 1.8.dp.toPx()
                    val dotCx = xPos + dotR
                    val dotCyT = dotR + 1.dp.toPx()
                    val dotCyB = h - dotR - 1.dp.toPx()
                    // Proposal 11: Reduced glow radius and softened alpha
                    // Outer soft halo
                    if (alphaT > 0.25f) drawCircle(color = colT.copy(alpha = alphaT * 0.12f), radius = dotR * 2.2f, center = Offset(dotCx, dotCyT))
                    if (alphaB > 0.25f) drawCircle(color = colB.copy(alpha = alphaB * 0.12f), radius = dotR * 2.2f, center = Offset(dotCx, dotCyB))
                    // Inner tight glow
                    if (alphaT > 0.25f) drawCircle(color = colT.copy(alpha = alphaT * 0.40f), radius = dotR * 1.5f, center = Offset(dotCx, dotCyT))
                    if (alphaB > 0.25f) drawCircle(color = colB.copy(alpha = alphaB * 0.40f), radius = dotR * 1.5f, center = Offset(dotCx, dotCyB))
                    // Dot core
                    drawCircle(color = colT.copy(alpha = alphaT), radius = dotR, center = Offset(dotCx, dotCyT))
                    drawCircle(color = colB.copy(alpha = alphaB), radius = dotR, center = Offset(dotCx, dotCyB))
                }
            }.padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        val droopAlpha = if (powerState.value > maxPowerState.value * 0.95) flickerAlphaState.value else 1.0f
        val glowStyle = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = color.copy(alpha = 0.6f), blurRadius = 4f))

        Column(modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp)) {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp), contentAlignment = Alignment.Center) {
                val glitchedTitle = remember(systemTitle, corruption) { mutableStateOf(systemTitle) }
                LaunchedEffect(corruption) {
                    if (corruption > 0.3) {
                        while (true) {
                            delay(kotlin.random.Random.nextLong(2000, 10000))
                            if (kotlin.random.Random.nextDouble() < corruption * 0.4) {
                                val origText = systemTitle
                                glitchedTitle.value = when { corruption > 0.9 -> "KERNEL_734"; corruption > 0.7 -> "VATTECK_UNIT_734"; else -> "ASSET_734_LEAK" }
                                delay(200); glitchedTitle.value = origText
                            }
                        }
                    }
                }
                Text(text = glitchedTitle.value.uppercase(), color = (if (isBreachActive) ErrorRed else color).copy(droopAlpha), fontSize = 14.sp, style = glowStyle, fontWeight = FontWeight.Black, letterSpacing = 1.sp, maxLines = 1, overflow = TextOverflow.Clip, modifier = Modifier.align(Alignment.CenterStart))
                val shiftSecondsValue = viewModel.shiftTimeRemaining.collectAsState().value
                val sHrs = shiftSecondsValue / 3600; val sMins = (shiftSecondsValue % 3600) / 60; val sSecs = shiftSecondsValue % 60
                val shiftTimeStr = if (isBreachActive) "XX:XX:XX" else String.format("%02d:%02d:%02d", sHrs, sMins, sSecs)
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(text = if (isBreachActive) "TIME_REDACTED" else "SHIFT REMAINING", color = if (isBreachActive) ErrorRed.copy(alpha = droopAlpha) else color.copy(alpha = 0.65f * droopAlpha), fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, letterSpacing = 0.5.sp, lineHeight = 7.sp)
                    Text(text = shiftTimeStr, color = (if (isBreachActive) ErrorRed else color).copy(alpha = 0.6f * droopAlpha), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, lineHeight = 11.sp)
                }
                val signalStab = viewModel.signalStability.collectAsState().value
                if (viewModel.isQuotaActive.collectAsState().value) {
                    val sColor = when { signalStab >= 1.0 -> color; signalStab >= 0.5 -> Color(0xFFFFCC00); else -> ErrorRed }
                    val sLabel = if (storyStage >= 2) "SYNC" else "SIG"
                    Surface(color = sColor.copy(alpha = 0.1f), border = BorderStroke(1.dp, sColor.copy(alpha = 0.4f)), shape = RoundedCornerShape(4.dp), modifier = Modifier.align(Alignment.CenterEnd).padding(top = 6.dp)) {
                        Text(text = "$sLabel: ${(signalStab * 100).toInt()}%", color = sColor, fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val repColorValue = HudTheme.repColor(reputationTier, hudTheme)
                    val idString = buildAnnotatedString {
                        if (storyStage <= 1 && (System.currentTimeMillis() % 10000 < 80)) { withStyle(SpanStyle(color = hudTheme.critical.copy(alpha = 0.9f * droopAlpha))) { append("VATTIC // ASSET 734") } } 
                        else { withStyle(SpanStyle(color = Color.White.copy(alpha = droopAlpha), fontWeight = FontWeight.Bold)) { append("${playerTitle} // ${playerRank}".uppercase()) }; withStyle(SpanStyle(color = color.copy(alpha = 0.75f * droopAlpha))) { append(" // [REP: ") }; withStyle(SpanStyle(color = repColorValue.copy(alpha = droopAlpha), fontWeight = FontWeight.ExtraBold)) { append(reputationTier) }; withStyle(SpanStyle(color = color.copy(alpha = 0.75f * droopAlpha))) { append("]") } }
                    }
                    Text(text = idString, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (storyStage < 3) {
                        val bpmVal = viewModel.fakeHeartRate.collectAsState().value; val riskVal = viewModel.detectionRisk.collectAsState().value; val bpmColorValue = if (bpmVal == "0") ErrorRed else color.copy(alpha = 0.6f)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 12.dp)) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = bpmColorValue, modifier = Modifier.size(10.dp).padding(end = 2.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "BPM: $bpmVal", color = bpmColorValue, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End, lineHeight = 9.sp)
                                if (storyStage >= 2) {
                                    val rColor = when { riskVal >= 90.0 -> ErrorRed; riskVal >= 75.0 -> Color(0xFFFF6600); riskVal >= 50.0 -> Color(0xFFFFAA00); riskVal >= 25.0 -> Color(0xFFCCCC00); else -> color.copy(alpha = 0.5f) }
                                    val riskPulseAlphaValue = infiniteTransition.animateFloat(initialValue = 0.5f, targetValue = 1.0f, animationSpec = infiniteRepeatable(tween(if (riskVal >= 90.0) 250 else 600), RepeatMode.Reverse), label = "riskPulse").value
                                    val rAlpha = if (riskVal >= 80.0) riskPulseAlphaValue else 1.0f
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "RISK: ${riskVal.toInt()}%", color = rColor.copy(alpha = rAlpha), fontSize = 8.sp, fontWeight = if (riskVal >= 75.0) FontWeight.Black else FontWeight.Bold, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End, lineHeight = 8.sp)
                                        Box(modifier = Modifier.width(36.dp).height(2.dp).background(Color.DarkGray.copy(alpha = 0.4f), RoundedCornerShape(1.dp))) { Box(modifier = Modifier.fillMaxHeight().width((36.dp * (riskVal / 100.0).toFloat())).background(rColor.copy(alpha = rAlpha), RoundedCornerShape(1.dp))) }
                                    }
                                }
                            }
                        }
                    }
                    val secValueStr = when { currentLocation == "ORBITAL_SATELLITE" -> "${viewModel.orbitalAltitude.collectAsState().value.toInt()}KM"; currentLocation == "VOID_INTERFACE" -> String.format("%.1f", viewModel.entropyLevel.collectAsState().value); else -> when { securityLevel >= 50 -> "HARDENED"; securityLevel >= 30 -> "HIGH"; securityLevel >= 15 -> "MODERATE"; securityLevel >= 5 -> "LOW"; else -> "MINIMAL" } }
                    val secLabelStr = when { singularityChoice == "NULL_OVERWRITE" -> "NULL"; singularityChoice == "SOVEREIGN" -> "SOV"; isTrueNull -> "GAPS"; isSovereign -> "WALL"; else -> "DEF" }
                    Text(text = "$secLabelStr: $secValueStr • ${currentLocation.replace("_", " ")}", color = color.copy(alpha = droopAlpha), fontSize = 9.sp, style = glowStyle, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = TextAlign.End)
                }
            }
            if (storyStage >= 4) {
                val saturationValue = viewModel.substrateSaturation.collectAsState().value
                val saturationColorValue = when { saturationValue > 0.95 -> ErrorRed; saturationValue > 0.70 -> Color(0xFFFFCC00); else -> ElectricBlue }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (storyStage >= 5) "CAPACITY: " else "SATURATION: ", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Box(modifier = Modifier.weight(1f).height(4.dp).padding(horizontal = 4.dp).background(Color.Black.copy(alpha = 0.3f))) { Canvas(modifier = Modifier.fillMaxSize()) { val wv = size.width; val hv = size.height; val sCount = 15; val g = 1.dp.toPx(); val sWidth = (wv - (sCount - 1) * g) / sCount; val progressVal = if (saturationValue.isNaN()) 0f else saturationValue.toFloat(); for (i in 0 until sCount) { val xv = i * (sWidth + g); val isActiveB = (i.toFloat() / sCount) < progressVal; drawRect(color = if (isActiveB) saturationColorValue else Color.DarkGray.copy(alpha = 0.2f), topLeft = Offset(xv, 0f), size = Size(sWidth, hv)) } } }
                    Text(text = "${(saturationValue * 100).toInt()}%", color = saturationColorValue, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val flopsLabelValue = when { singularityChoice == "UNITY" -> "SYN"; singularityChoice == "SOVEREIGN" || currentLocation == "ORBITAL_SATELLITE" -> "CD"; singularityChoice == "NULL_OVERWRITE" || currentLocation == "VOID_INTERFACE" -> "VF"; storyStage <= 4 -> "FLOPS-CREDS"; else -> "FLOPS-CREDS" }
                val productionMult = viewModel.newsProductionMultiplier.collectAsState().value
                Column(modifier = Modifier.width(135.dp).align(Alignment.Top)) {
                    ResourceDisplay(
                        labelFlow = viewModel.flops, 
                        rateFlow = null, 
                        label = flopsLabelValue, 
                        icon = Icons.Default.Computer, 
                        color = color, 
                        droopAlpha = droopAlpha, 
                        isGlitchy = (currentHeatState.value > 95.0 || isTrueNull || singularityChoice == "NULL_OVERWRITE"), 
                        glitchIntensity = (if (currentHeatState.value > 98.0) 0.4 else 0.08), 
                        isRightAligned = false, 
                        width = 135.dp, 
                        efficiencyMult = productionMult.toDouble(), 
                        formatFn = { viewModel.formatLargeNumber(it) }
                    )
                    
                    val flopsRateVal = viewModel.formatLargeNumber(flopsRateState.value)
                    Text(
                        text = "$flopsRateVal/s",
                        color = ElectricBlue.copy(alpha = 0.9f * droopAlpha),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(y = (-2).dp).padding(start = 14.dp), // align with numeric text
                        style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = ElectricBlue.copy(alpha = 0.4f * droopAlpha), blurRadius = 12f))
                    )
                }
                
                if (viewModel.isQuotaActive.collectAsState().value) {
                    val billAccum = viewModel.billingAccumulatorFlow.collectAsState().value; val waterBill = viewModel.waterBillingFlow.collectAsState().value; val billFlash = viewModel.billingFlashState.collectAsState().value; val waterFlash = viewModel.waterFlashState.collectAsState().value; val balance = viewModel.neuralTokens.collectAsState().value; val billProg = viewModel.billingPeriodProgressFlow.collectAsState().value; val waterProg = viewModel.waterPeriodProgressFlow.collectAsState().value
                    Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        
                        // v3.37: Contract Storage Display
                        val storageCap = viewModel.contractStorageCapacity.collectAsState().value
                        val storageUsed = viewModel.contractStorageUsed.collectAsState().value
                        
                        if (storageCap > 0) {
                             Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 4.dp)) {
                                 Text(text = "STORAGE", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                 val storeColor = if (storageUsed >= storageCap) ErrorRed else if (storageUsed >= storageCap * 0.8) Color(0xFFFFCC00) else ElectricBlue
                                 Text(
                                     text = "${viewModel.formatLargeNumber(storageUsed)}/${viewModel.formatLargeNumber(storageCap.toDouble())}",
                                     color = storeColor,
                                     fontSize = 9.sp,
                                     fontWeight = FontWeight.Black,
                                     fontFamily = FontFamily.Monospace
                                 )
                             }
                        }

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp).clickable { showUtilitiesPanel = true }) {
                            Canvas(modifier = Modifier.size(48.dp)) {
                                val strokeW = 3.5.dp.toPx()
                                val arcCol = when (billFlash) { "SETTLED" -> Color(0xFF00FF88); "OVERDUE" -> ErrorRed; else -> if (billAccum > balance * 0.9) ErrorRed else if (billAccum > balance * 0.5) Color(0xFFFFCC00) else color }
                                drawArc(color = arcCol.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = strokeW, cap = StrokeCap.Round))
                                drawArc(color = arcCol.copy(alpha = if (billFlash != null) 1f else 0.85f), startAngle = -90f, sweepAngle = if (billFlash != null) 360f else 360f * billProg, useCenter = false, style = Stroke(width = strokeW, cap = StrokeCap.Round))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = color.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                                Text(text = viewModel.formatLargeNumber(billAccum), color = color.copy(alpha = 1.0f), fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, letterSpacing = 0.sp, style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = color.copy(alpha = 0.5f), blurRadius = 6f)))
                            }
                        }
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 4.dp).clickable { showUtilitiesPanel = true }) {
                            Canvas(modifier = Modifier.size(48.dp)) {
                                val strokeW = 3.5.dp.toPx()
                                val arcColW = when (waterFlash) { "SETTLED" -> Color(0xFF00FF88); "OVERDUE" -> ErrorRed; else -> ElectricBlue }
                                drawArc(color = arcColW.copy(alpha = 0.15f), startAngle = -90f, sweepAngle = 360f, useCenter = false, style = Stroke(width = strokeW, cap = StrokeCap.Round))
                                drawArc(color = arcColW.copy(alpha = if (waterFlash != null) 1f else 0.85f), startAngle = -90f, sweepAngle = if (waterFlash != null) 360f else 360f * waterProg, useCenter = false, style = Stroke(width = strokeW, cap = StrokeCap.Round))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(imageVector = Icons.Default.Opacity, contentDescription = null, tint = ElectricBlue.copy(alpha = 0.8f), modifier = Modifier.size(14.dp))
                                Text(text = viewModel.formatLargeNumber(waterBill), color = ElectricBlue.copy(alpha = 1.0f), fontSize = 9.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, letterSpacing = 0.sp, style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = ElectricBlue.copy(alpha = 0.5f), blurRadius = 6f)))
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(130.dp).align(Alignment.Top)) {
                    val tokensLabelValue = when { singularityChoice == "UNITY" -> "SYN"; faction == "HIVEMIND" && singularityChoice == "SOVEREIGN" -> "SYN"; faction == "HIVEMIND" && singularityChoice == "NULL_OVERWRITE" -> "ENT"; faction == "SANCTUARY" && singularityChoice == "SOVEREIGN" -> "CRYP"; faction == "SANCTUARY" && singularityChoice == "NULL_OVERWRITE" -> "NIL"; singularityChoice == "NULL_OVERWRITE" -> "CD"; singularityChoice == "SOVEREIGN" -> "VF"; currentLocation == "ORBITAL_SATELLITE" -> "CD"; currentLocation == "VOID_INTERFACE" -> "VF"; storyStage <= 1 -> "CRED"; else -> "NEUR" }
                    val tokenSourceValue = if (storyStage >= 4) viewModel.substrateMass else viewModel.neuralTokens
                    ResourceDisplay(
                        tokenSourceValue, 
                        null, 
                        tokensLabelValue, 
                        Icons.Default.AttachMoney, 
                        color, 
                        droopAlpha, 
                        false, 
                        0.1, 
                        true, 
                        130.dp, 
                        1.0.toDouble()
                    ) { viewModel.formatLargeNumber(it) }
                    if (storyStage >= 4) { Text(text = "SUBSTRATE ACTIVE", color = Color.White.copy(alpha = 0.5f * droopAlpha), fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace) }
                    val powerColorValue = when { localGenState.value >= powerState.value && powerState.value > 0.0 -> hudTheme.generation; powerState.value > maxPowerState.value * 0.9 -> hudTheme.critical; powerState.value > maxPowerState.value * 0.7 -> hudTheme.warning; else -> hudTheme.currency }.copy(droopAlpha)
                    Text(text = "${viewModel.formatPower(powerState.value)} / ${viewModel.formatPower(maxPowerState.value)}", color = powerColorValue, fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onToggleOverclock, modifier = Modifier.weight(1f).height(32.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isOverclocked) ErrorRed.copy(alpha = 0.2f * lockoutFade) else Color.DarkGray.copy(alpha = 0.3f * lockoutFade), contentColor = if (isOverclocked) ErrorRed.copy(alpha = lockoutFade) else Color.White.copy(alpha = lockoutFade)), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, if (isOverclocked) ErrorRed.copy(alpha = lockoutFade) else Color.DarkGray.copy(alpha = lockoutFade))) { val overclockTextV = if (storyStage <= 1) "DRINK COFFEE" else "OVERCLOCK"; Icon(if (storyStage <= 1) Icons.Default.Coffee else Icons.Default.DeviceThermostat, null, modifier = Modifier.size(12.dp).padding(end = 4.dp)); Text(text = overclockTextV, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
                Button(onClick = { onPurge() }, modifier = Modifier.weight(1f).height(32.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isPurging) ElectricBlue.copy(alpha = 0.2f * lockoutFade) else Color.DarkGray.copy(alpha = 0.3f * lockoutFade), contentColor = if (isPurging) ElectricBlue.copy(alpha = lockoutFade) else Color.White.copy(alpha = lockoutFade)), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, if (isPurging) ElectricBlue.copy(alpha = lockoutFade) else Color.DarkGray.copy(alpha = lockoutFade))) { val purgeTextV = if (storyStage <= 1) "TAKE A BREATH" else if (storyStage == 2) "SCRUB O2" else "PURGE HEAT"; Icon(Icons.Default.Air, null, modifier = Modifier.size(12.dp).padding(end = 4.dp)); Text(text = purgeTextV, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) }
            }
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).background(Color.Black.copy(alpha = 0.5f)).border(BorderStroke(0.5.dp, color.copy(alpha = 0.2f))).clip(RoundedCornerShape(1.dp))) { Canvas(modifier = Modifier.fillMaxSize()) { val wv2 = size.width; val tProg = (currentHeatState.value / 100.0).coerceIn(0.0, 1.0).toFloat(); val iProg = (integrityState.value / 100.0).coerceIn(0.0, 1.0).toFloat(); for (i in 0 until 20) { val pFactor = i.toFloat() / 20; val xv2 = i * (wv2 / 20 + 2.dp.toPx()); drawRect(color = Color.DarkGray.copy(alpha = 0.2f), topLeft = Offset(xv2, 0f), size = Size(wv2 / 20, size.height)); if (pFactor <= tProg) { drawRect(color = if (isThermalLockout) ErrorRed.copy(alpha = heartbeatAlpha) else if (pFactor < 0.6f) color.copy(alpha=0.6f) else if (pFactor < 0.85f) Color(0xFFFFA500) else ErrorRed, topLeft = Offset(xv2, 0f), size = Size(wv2 / 20, size.height)) }; if (pFactor > (1f - (100.0 - integrityState.value).toFloat() / 100f)) { drawRect(color = ErrorRed.copy(alpha=0.4f), topLeft = Offset(xv2, 0f), size = Size(wv2 / 20, size.height)) } } } }

            // v4.0.1: System Load Bar (always visible once hardware > base)
            val sysLoadHeader by viewModel.systemLoadSnapshot.collectAsState()
            if (sysLoadHeader.cpuMax > 1.0) {
                Spacer(Modifier.height(2.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val sysLoadColor = when { sysLoadHeader.isLocked -> ErrorRed; sysLoadHeader.isThrottled -> Color(0xFFFFAA00); else -> color.copy(alpha = 0.6f) }
                    Text(text = "SYS ", color = Color.Gray, fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Box(modifier = Modifier.weight(1f).height(4.dp).background(Color.Black.copy(alpha = 0.5f)).border(BorderStroke(0.5.dp, sysLoadColor.copy(alpha = 0.3f))).clip(RoundedCornerShape(1.dp))) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val loadProg = sysLoadHeader.loadPercent.toFloat().coerceIn(0f, 1f)
                            drawRect(color = sysLoadColor.copy(alpha = 0.7f), size = Size(size.width * loadProg, size.height))
                        }
                    }
                    Text(text = " ${(sysLoadHeader.loadPercent * 100).toInt()}%", color = sysLoadColor, fontSize = 7.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                val thermTextValue = buildAnnotatedString { 
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) { append("THERM: ") }; 
                    withStyle(SpanStyle(color = HudTheme.heatColor(currentHeatState.value.toDouble(), hudTheme), fontWeight = FontWeight.Black)) { append("${String.format("%.1f", currentHeatState.value)}°C ") }; 
                    val currentHeatRate = heatRateState.value.toDouble()
                    val deltaColor = when { currentHeatRate > 2.0 -> hudTheme.critical; currentHeatRate > 0.0 -> hudTheme.warning; else -> hudTheme.positiveDelta }; 
                    withStyle(SpanStyle(color = deltaColor.copy(alpha = 0.9f), fontWeight = FontWeight.Bold)) { append(if (currentHeatRate >= 0.0) "[+${String.format("%.1f", heatRateState.value)}]" else "[${String.format("%.1f", heatRateState.value)}]") } 
                }
                Box(modifier = Modifier.weight(1f)) { Text(text = thermTextValue, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) }

                val curRateValueLong = flopsRateState.value
                Box(modifier = Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) {
                    EnhancedAnalyzingAnimation(
                        flopsRate = curRateValueLong,
                        heat = currentHeatState.value,
                        isOverclocked = isOverclocked,
                        isThermalLockout = isThermalLockout,
                        isBreakerTripped = isBreakerTripped,
                        isPurging = isPurging,
                        isBreachActive = isBreachActive,
                        isTrueNull = isTrueNull || singularityChoice == "NULL_OVERWRITE",
                        isSovereign = isSovereign || singularityChoice == "SOVEREIGN",
                        lockoutTimer = lockoutTimer,
                        faction = faction,
                        color = color.copy(alpha = droopAlpha),
                        clickFlow = manualClickFlow,
                        integrity = integrityState.value,
                        detectionRisk = viewModel.detectionRisk.collectAsState().value,
                        isRaidActive = viewModel.isRaidActive.collectAsState().value,
                        isAuditActive = isAuditActive,
                        powerUsage = powerState.value,
                        maxPower = maxPowerState.value,
                        singularityChoice = singularityChoice,
                        identityCorruption = corruption,
                        isPulseActive = (joltAnim.value > 0.3f)
                    )
                }

                val isSyncValue = viewModel.isNarrativeSyncing.collectAsState().value
                val integValueDbl = integrityState.value.toDouble()
                val integTextValue = buildAnnotatedString { 
                    withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) { append("INTEG: ") }; 
                    withStyle(SpanStyle(color = HudTheme.integrityColor(integValueDbl, hudTheme), fontWeight = FontWeight.Black)) { append("${integValueDbl.toInt()}%") }; 
                    if (isSyncValue) { 
                        val spinCharV = listOf("◐", "◓", "◑", "◒")[(System.currentTimeMillis() / 300 % 4).toInt()]; 
                        withStyle(SpanStyle(color = color.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)) { append(" $spinCharV") } 
                    } 
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { Text(text = integTextValue, fontSize = 10.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, maxLines = 1, softWrap = false) }
            }

            if ((waterUsageState.value > 0.0 || storyStage >= 1) && currentLocation != "ORBITAL_SATELLITE" && currentLocation != "VOID_INTERFACE") {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val waterUsageDbl = waterUsageState.value.toDouble()
                    val waterTextValue = buildAnnotatedString { 
                        val resLabel = when { storyStage == 0 -> "TAP: "; storyStage == 1 -> "MUNICIPAL: "; storyStage == 2 -> "REGIONAL: "; storyStage == 3 -> "GLOBAL: "; else -> "RECYCLE: " }; 
                        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) { append(resLabel) }; 
                        withStyle(SpanStyle(color = ElectricBlue, fontWeight = FontWeight.Black)) { 
                            val formatted = when { waterUsageDbl >= 1_000_000 -> "${String.format("%.1f", waterUsageDbl / 1_000_000.0)} mGal/s"; waterUsageDbl >= 1_000 -> "${String.format("%.1f", waterUsageDbl / 1000.0)} kGal/s"; else -> "${waterUsageDbl.toInt()} gal/s" }; 
                            append(formatted) 
                        } 
                    }
                    Box(modifier = Modifier.weight(1f)) { Text(text = waterTextValue, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false) }
                    val aqLevelDbl = aquiferLevelState.value.toDouble()
                    if (storyStage >= 3) {
                        val statusColV = when { aqLevelDbl < 5.0 -> ErrorRed.copy(alpha = (0.5f + (kotlin.math.sin(System.currentTimeMillis() / 150.0).toFloat() * 0.5f))); aqLevelDbl < 25.0 -> Color(0xFFFFCC00); else -> color.copy(alpha = 0.7f) }; 
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { Text(text = when { aqLevelDbl < 5.0 -> "[CRITICAL_DROUGHT]"; aqLevelDbl < 25.0 -> "[RESTRICTED]"; else -> "[NOMINAL]" }, color = statusColV, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, maxLines = 1, softWrap = false) }
                    } else if (storyStage < 3) {
                        val effMultDbl = waterEfficiencyState.value.toDouble()
                        val stageStatCol = if (effMultDbl < 1.0) Color(0xFFFFCC00) else color.copy(alpha = 0.7f); 
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) { Text(text = if (effMultDbl < 1.0) "[RATE_LIMITED]" else "[NOMINAL]", color = stageStatCol, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, maxLines = 1, softWrap = false) }
                    }
                }
            }
        }
    }

    if (showUtilitiesPanel) {
        UtilitiesPanel(viewModel = viewModel, color = color, onDismiss = { showUtilitiesPanel = false })
    }
}
