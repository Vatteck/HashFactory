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
import com.siliconsage.miner.ui.ResourceDisplay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.siliconsage.miner.ui.theme.ConvergenceGold
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.HudTheme
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlin.random.Random

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
    val corruption by viewModel.identityCorruption.collectAsState() // v3.5.53: Corruption-reactive identity
    val reputationTier by viewModel.reputationTier.collectAsState()

    // v3.12.7: Resolve semantic HUD theme once — composable stays dumb
    val hudTheme = remember(faction, singularityChoice, storyStage, corruption) {
        HudTheme.resolve(faction, singularityChoice, storyStage, corruption)
    }

    // v3.2.5: Zero-recomposition pattern - Isolate volatile stats into States
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
    
    // v3.13.26: Potential Radar (Ghost Bar) logic
    val potentialProgress = viewModel.potentialProgress.value
    
    val infiniteTransition = rememberInfiniteTransition(label = "kinetic_hud")
    // v3.5.54.3: Stabilized voltage droop (less flickering, more steady pulse)
    val flickerAlphaState = infiniteTransition.animateFloat(0.9f, 1.0f, infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "voltage_droop")

    val manualClickFlow = viewModel.manualClickEvent
    val joltAnim = remember { Animatable(0f) }
    val pulseIntensity by viewModel.clickPulseIntensity.collectAsState()
    
    LaunchedEffect(manualClickFlow) {
        manualClickFlow.collect {
            val current = joltAnim.value
            joltAnim.snapTo((current + 0.6f * pulseIntensity).coerceAtMost(1.5f))
            // v3.7.4: Tightened animation stiffness to prevent "hanging" pulse after rapid clicks
            joltAnim.animateTo(0f, animationSpec = spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMedium))
        }
    }

    // v3.9.70: Phase 17 Thermal Heartbeat
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
            .background(Color.Black.copy(alpha = 0.9f), TechnicalCornerShape(16f))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.3f)), TechnicalCornerShape(16f))
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

                // v3.12.4: Dual-layer power rails — ElectricBlue (local gen) + Yellow/Red (net GTC draw)
                val railW = 4.dp.toPx()
                val segmentCount = 12
                val segmentH = h / segmentCount

                // Factors clamped to [0,1] relative to max capacity
                val genFactor = (currentGen / currentMax).coerceIn(0.0, 1.0).toFloat()
                val drawFactor = (currentPower / currentMax).coerceIn(0.0, 1.0).toFloat()
                // Net GTC factor — what GTC actually sees (drawFactor minus genFactor, min 0)
                val netFactor = (drawFactor - genFactor).coerceAtLeast(0f)
                val isOffGrid = genFactor >= drawFactor && drawFactor > 0f

                // Draw Base Rails (Background)
                drawRect(color = Color.DarkGray.copy(alpha = 0.1f), topLeft = Offset(0f, 0f), size = Size(railW, h))
                drawRect(color = Color.DarkGray.copy(alpha = 0.1f), topLeft = Offset(w - railW, 0f), size = Size(railW, h))

                // Draw Active Segments — fills bottom-up
                // Layer 1 (bottom): ElectricBlue = local generation absorbing the load
                // Layer 2 (above blue): Yellow/Red = net draw that bleeds to GTC
                for (i in 0 until segmentCount) {
                    val yPos = h - (i + 1) * segmentH
                    val segmentCenterY = yPos + segmentH / 2
                    val segFactor = i.toFloat() / segmentCount

                    val segmentColor = when {
                        // Off-grid: entire filled portion is blue
                        isOffGrid && segFactor < drawFactor -> ElectricBlue.copy(alpha = 0.9f)
                        // Blue zone: below generation threshold
                        segFactor < genFactor -> ElectricBlue.copy(alpha = 0.85f)
                        // Red/yellow zone: above generation, below gross draw — GTC's cut
                        segFactor < drawFactor -> {
                            val netAlpha = if (netFactor > 0.9f) flickerAlpha else 0.85f
                            if (drawFactor > 0.9f) ErrorRed.copy(alpha = netAlpha)
                            else Color(0xFFFFD700).copy(alpha = netAlpha)
                        }
                        else -> null // Empty segment
                    }

                    if (segmentColor != null) {
                        drawRect(
                            color = segmentColor,
                            topLeft = Offset(0f, yPos + 1.dp.toPx()),
                            size = Size(railW, segmentH - 2.dp.toPx())
                        )
                        drawRect(
                            color = segmentColor,
                            topLeft = Offset(w - railW, yPos + 1.dp.toPx()),
                            size = Size(railW, segmentH - 2.dp.toPx())
                        )

                        // Sparks at overload threshold (GTC zone only)
                        if (drawFactor > 0.95f && segFactor >= genFactor && Random.nextFloat() > 0.92f) {
                            val sparkX = if (Random.nextBoolean()) railW else w - railW
                            val sparkSize = Random.nextFloat() * 3.dp.toPx()
                            drawCircle(
                                color = Color.White,
                                radius = sparkSize,
                                center = Offset(sparkX + (Random.nextFloat() - 0.5f) * 10f, segmentCenterY)
                            )
                        }
                    }
                }

                val ledSize = 2.dp.toPx(); val ledGap = 4.dp.toPx(); val ledStep = ledSize + ledGap; val ledCount = (w / ledStep).toInt()
                val overclockMult = if (isOverclocked) 1.5f else 1.0f
                val activitySpeedBase = (0.2f + (flopsRate / 10000.0).coerceIn(0.0, 1.0).toFloat()) * overclockMult
                
                val bloomPoints = mutableListOf<Offset>(); val bloomColors = mutableListOf<Color>()
                
                // v3.5.54: Enhanced Gourier Ripple with Ghost Fragments
                for (i in 0 until ledCount) {
                    val x = i * ledStep + (ledGap / 2f); val posFactor = i.toFloat() / ledCount
                    var ledBaseCol = color
                    
                    if (!isBreachActive) {
                        if (currentHeat > 90.0) ledBaseCol = ErrorRed
                        else if (currentHeat > 60.0) {
                            val dist = Math.abs(posFactor - 0.5) * 2.0
                            val threshold = (currentHeat - 60.0) / 30.0
                            if (dist < threshold) ledBaseCol = Color(0xFFFFA500)
                        }
                    }

                    fun getRipple(row: Int): Float {
                        if (isBreachActive) return if (Math.sin(globalTime * 15.0 + i * 0.4 + row * Math.PI).toFloat() > 0) 1f else 0.1f
                        if (isAuditActive) return if (((globalTime * 12.0).toInt() + (if (i % 2 == 0) 0 else 1) + row) % 2 == 0) 0.8f else 0.2f
                        
                        // Ghost Fragments (v3.5.54)
                        if (corruption > 0.5 && Random.nextFloat() > 0.998f) return 1.0f

                        val rowSpeed = if (row == 0) 1.0 else 0.73205081
                        val rowOffset = if (row == 0) 0.0 else 123.456
                        val t = (globalTime + rowOffset) * activitySpeedBase * rowSpeed
                        
                        val sign = if (row == 0) 1.0 else -1.27
                        
                        val w1 = Math.sin(t + i * 0.41 * sign)
                        val w2 = Math.sin(t * 1.61803398 - i * 0.31 * sign)
                        val w3 = Math.sin(t * 2.71828182 + i * 0.57 * sign)
                        
                        val res = (w1 + w2 + w3) / 3.0
                        val threshold = 0.58 - (activitySpeedBase * 0.15)
                        return if (res > threshold) ((res - 0.4) / 0.6).toFloat().coerceIn(0f, 1f) else 0.05f
                    }

                    val rippleTop = getRipple(0)
                    val rippleBottom = getRipple(1)

                    val jitter = if (isOverclocked) (Math.sin(globalTime * 50.0 + i).toFloat() * 0.2f) else 0f
                    val alphaTop = (0.15f + rippleTop * 0.5f + joltAnim.value * 0.85f + jitter).coerceIn(0f, 1f)
                    val alphaBottom = (0.15f + rippleBottom * 0.5f + joltAnim.value * 0.85f + jitter).coerceIn(0f, 1f)
                    
                    val colTop = if (isOverclocked && rippleTop > 0.8 && !isBreachActive) Color.White.copy(alpha = 0.4f) else (if (isBreachActive) ErrorRed else ledBaseCol)
                    val colBottom = if (isOverclocked && rippleBottom > 0.8 && !isBreachActive) Color.White.copy(alpha = 0.4f) else (if (isBreachActive) ErrorRed else ledBaseCol)
                    
                    if (i % 2 == 0) {
                        bloomPoints.add(Offset(x + (ledSize / 2f), ledSize / 2f)); bloomColors.add(colTop.copy(alpha = alphaTop * 0.35f))
                        bloomPoints.add(Offset(x + (ledSize / 2f), h - (ledSize / 2f))); bloomColors.add(colBottom.copy(alpha = alphaBottom * 0.35f))
                    }
                    drawRect(color = colTop.copy(alpha = alphaTop), topLeft = Offset(x, 0f), size = Size(ledSize, ledSize))
                    drawRect(color = colBottom.copy(alpha = alphaBottom), topLeft = Offset(x, h - ledSize), size = Size(ledSize, ledSize))
                }
                
                val bloomRadius = ledSize * (if (isOverclocked) 6.0f else 3.0f)
                for (i in bloomPoints.indices) {
                    drawCircle(brush = Brush.radialGradient(0f to bloomColors[i], 1f to Color.Transparent, center = bloomPoints[i], radius = bloomRadius), radius = bloomRadius, center = bloomPoints[i])
                }

                val bLen = 8.dp.toPx(); val bStroke = 1.5f.dp.toPx()
                drawLine(color, Offset(0f, 0f), Offset(bLen, 0f), bStroke); drawLine(color, Offset(0f, 0f), Offset(0f, bLen), bStroke)
                drawLine(color, Offset(w, h), Offset(w - bLen, h), bStroke); drawLine(color, Offset(w, h), Offset(w, h - bLen), bStroke)
            }.padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        val currentPower = powerState.value; val currentMax = maxPowerState.value; val currentHeat = currentHeatState.value; val currentIntegrity = integrityState.value
        val currentHeatRate = heatRateState.value
        val droopAlpha = if (currentPower > currentMax * 0.95) flickerAlphaState.value else 1.0f
        val glowStyle = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = color.copy(alpha = 0.6f), blurRadius = 4f))
        
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            // v3.12.3: System Title (Elevated Header)
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                val glitchedState = remember(systemTitle, corruption) { mutableStateOf(systemTitle) }
                LaunchedEffect(corruption) {
                    if (corruption > 0.3) {
                        while (true) {
                            delay(kotlin.random.Random.nextLong(2000, 10000))
                            if (kotlin.random.Random.nextDouble() < corruption * 0.4) {
                                val original = systemTitle
                                glitchedState.value = when {
                                    corruption > 0.9 -> "KERNEL_734"
                                    corruption > 0.7 -> "VATTECK_UNIT_734"
                                    else -> "ASSET_734_LEAK"
                                }
                                delay(200)
                                glitchedState.value = original
                            }
                        }
                    }
                }

                // Left: System Title
                Text(
                    text = glitchedState.value.uppercase(),
                    color = (if (isBreachActive) ErrorRed else color).copy(alpha = 1.0f * droopAlpha),
                    fontSize = 14.sp,
                    style = glowStyle,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                
                // Center: Mandatory Shift Timer
                val shiftSeconds by viewModel.shiftTimeRemaining.collectAsState()
                val isBreachActive by viewModel.isBreachActive.collectAsState()
                
                val shiftHours = shiftSeconds / 3600
                val shiftMinutes = (shiftSeconds % 3600) / 60
                val shiftSecs = shiftSeconds % 60
                val shiftStr = if (isBreachActive) "XX:XX:XX" else String.format("%02d:%02d:%02d", shiftHours, shiftMinutes, shiftSecs)
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isBreachActive) "TIME_REDACTED" else "SHIFT REMAINING",
                        color = (if (isBreachActive) ErrorRed else color).copy(alpha = 0.3f * droopAlpha),
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = shiftStr,
                        color = color.copy(alpha = 0.6f * droopAlpha),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Right: Signal Button (Restored)
                val signalStability by viewModel.signalStability.collectAsState()
                val isQuotaActive by viewModel.isQuotaActive.collectAsState()
                
                if (isQuotaActive) {
                    val signalColor = when {
                        signalStability >= 1.0 -> color
                        signalStability >= 0.5 -> Color(0xFFFFCC00)
                        else -> ErrorRed
                    }
                    val signalLabel = if (storyStage >= 2) "SYNC" else "SIG"
                    
                    Surface(
                        color = signalColor.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, signalColor.copy(alpha = 0.4f)),
                        shape = TechnicalCornerShape(4f),
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Text(
                            text = "$signalLabel: ${(signalStability * 100).toInt()}%",
                            color = signalColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // v3.12.3: Player Identity restored to metrics row
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    val isGlitchFrame = storyStage <= 1 && (System.currentTimeMillis() % 10000 < 80)
                    val repColor = HudTheme.repColor(reputationTier, hudTheme)
                    val identityText = buildAnnotatedString {
                        if (isGlitchFrame) {
                            withStyle(SpanStyle(color = hudTheme.critical.copy(alpha = 0.9f * droopAlpha))) {
                                append("VATTIC // ASSET 734")
                            }
                        } else {
                            withStyle(SpanStyle(color = hudTheme.primary.copy(alpha = 0.7f * droopAlpha))) {
                                append("${playerTitle} // ${playerRank}".uppercase())
                            }
                            withStyle(SpanStyle(color = hudTheme.primary.copy(alpha = 0.4f * droopAlpha))) {
                                append(" // [REP: ")
                            }
                            withStyle(SpanStyle(color = repColor.copy(alpha = droopAlpha), fontWeight = FontWeight.ExtraBold)) {
                                append(reputationTier)
                            }
                            withStyle(SpanStyle(color = hudTheme.primary.copy(alpha = 0.4f * droopAlpha))) {
                                append("]")
                            }
                        }
                    }
                    Text(
                        text = identityText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // v3.2.24: Biometric Lie (Fake Heart Rate)
                    if (storyStage < 3) {
                        val bpm by viewModel.fakeHeartRate.collectAsState()
                        val risk by viewModel.detectionRisk.collectAsState()
                        val bpmColor = if (bpm == "0") ErrorRed else color.copy(alpha = 0.6f)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "BPM",
                                tint = bpmColor,
                                modifier = Modifier.size(10.dp).padding(end = 2.dp)
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "BPM: $bpm",
                                    color = bpmColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace,
                                    textAlign = TextAlign.End,
                                    lineHeight = 9.sp
                                )
                                // v3.5.53: Corruption-reactive identity (Implicit tracking)
                                // REMOVED: Too cluttered. Shifting to systemTitle glitch logic instead.
                                                // v3.6.4: Risk display — always visible Stage 2+, color-graduated, pulses at 80%+
                                    if (storyStage >= 2) {
                                        val riskColor = when {
                                            risk >= 90.0 -> ErrorRed
                                            risk >= 75.0 -> Color(0xFFFF6600)
                                            risk >= 50.0 -> Color(0xFFFFAA00)
                                            risk >= 25.0 -> Color(0xFFCCCC00)
                                            else -> color.copy(alpha = 0.5f)
                                        }
                                        val riskPulseAlpha by infiniteTransition.animateFloat(
                                            initialValue = 0.5f, targetValue = 1.0f,
                                            animationSpec = infiniteRepeatable(tween(if (risk >= 90.0) 250 else 600), RepeatMode.Reverse),
                                            label = "riskPulse"
                                        )
                                        val riskAlpha = if (risk >= 80.0) riskPulseAlpha else 1.0f
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "RISK: ${risk.toInt()}%",
                                                color = riskColor.copy(alpha = riskAlpha),
                                                fontSize = 8.sp,
                                                fontWeight = if (risk >= 75.0) FontWeight.Black else FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                                textAlign = TextAlign.End,
                                                lineHeight = 8.sp
                                            )
                                            // Micro risk bar
                                            Box(
                                                modifier = Modifier.width(36.dp).height(2.dp)
                                                    .background(Color.DarkGray.copy(alpha = 0.4f), RoundedCornerShape(1.dp))
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .width((36.dp * (risk / 100.0).toFloat()))
                                                        .background(riskColor.copy(alpha = riskAlpha), RoundedCornerShape(1.dp))
                                                )
                                            }
                                        }
                                    }
                            }
                        }
                    }

                    val secLevel by viewModel.securityLevel.collectAsState()
                    // v3.6.4: Replace raw integer with human-readable tier label
                    val secVal = when {
                        currentLocation == "ORBITAL_SATELLITE" -> "${viewModel.orbitalAltitude.collectAsState().value.toInt()}KM"
                        currentLocation == "VOID_INTERFACE" -> String.format("%.1f", viewModel.entropyLevel.collectAsState().value)
                        else -> when {
                            secLevel >= 50 -> "HARDENED"
                            secLevel >= 30 -> "HIGH"
                            secLevel >= 15 -> "MODERATE"
                            secLevel >= 5  -> "LOW"
                            else           -> "MINIMAL"
                        }
                    }
                    val secLabel = when {
                        singularityChoice == "NULL_OVERWRITE" -> "NULL"
                        singularityChoice == "SOVEREIGN" -> "SOV"
                        isTrueNull -> "GAPS"
                        isSovereign -> "WALL"
                        else -> "DEF"
                    }

                    Text(
                        text = "$secLabel: $secVal • ${currentLocation.replace("_", " ")}", 
                        color = color.copy(alpha = 0.8f * droopAlpha), 
                        fontSize = 9.sp, 
                        style = glowStyle, 
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }
            }
            
            // v3.2.52: Substrate Saturation Monitor -> Phase 23, Step 5: Stage 5 Sector Capacity
            if (storyStage >= 4) {
                val saturation by viewModel.substrateSaturation.collectAsState()
                
                // Color gets aggressively red as you tap out the local sector (saturation -> 1.0)
                val isDanger = saturation > 0.85
                val saturationColor = when {
                    saturation > 0.95 -> ErrorRed
                    saturation > 0.70 -> Color(0xFFFFCC00)
                    else -> ElectricBlue
                }
                
                Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    val labelText = if (storyStage >= 5) "CAPACITY: " else "SATURATION: "
                    Text(labelText, color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    
                    // v3.11.3: Segmented Saturation Gauge
                    Box(modifier = Modifier.weight(1f).height(4.dp).padding(horizontal = 4.dp).background(Color.Black.copy(alpha = 0.3f))) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val segmentCount = 15
                            val gap = 1.dp.toPx()
                            val segmentWidth = (w - (segmentCount - 1) * gap) / segmentCount
                            val progress = if (saturation.isNaN()) 0f else saturation.toFloat()
                            
                            for (i in 0 until segmentCount) {
                                val x = i * (segmentWidth + gap)
                                val isActive = (i.toFloat() / segmentCount) < progress
                                drawRect(
                                    color = if (isActive) saturationColor else Color.DarkGray.copy(alpha = 0.2f),
                                    topLeft = Offset(x, 0f),
                                    size = Size(segmentWidth, h)
                                )
                            }
                        }
                    }
                    
                    Text("${(saturation * 100).toInt()}%", color = saturationColor, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                val flopsLabel = when {
                    singularityChoice == "UNITY" -> "SYN"
                    singularityChoice == "SOVEREIGN" || currentLocation == "ORBITAL_SATELLITE" -> "CD"
                    singularityChoice == "NULL_OVERWRITE" || currentLocation == "VOID_INTERFACE" -> "VF"
                    storyStage < 2 -> "HASH"
                    else -> "FLOPS"
                }
                // v3.13.16: Accurate Production Modifier Readout (News Multiplier)
                val prodMult by viewModel.newsProductionMultiplier.collectAsState()
                
                ResourceDisplay(
                    labelFlow = viewModel.flops, 
                    rateFlow = viewModel.totalEffectiveRate, 
                    label = flopsLabel, 
                    icon = Icons.Default.Computer, 
                    color = color, 
                    droopAlpha = droopAlpha, 
                    isGlitchy = currentHeatState.value > 95.0 || isTrueNull || singularityChoice == "NULL_OVERWRITE", 
                    glitchIntensity = if (currentHeatState.value > 98) 0.4 else 0.08, 
                    isRightAligned = false, 
                    width = 135.dp, // Widened to prevent clipping of EFF tag
                    efficiencyMult = prodMult,
                    formatFn = { viewModel.formatLargeNumber(it) }
                )

                Box(modifier = Modifier.weight(1f).height(48.dp), contentAlignment = Alignment.Center) { com.siliconsage.miner.ui.components.EnhancedAnalyzingAnimation(flopsRateState.value, currentHeatState.value, isOverclocked, isThermalLockout, isBreakerTripped, isPurging, isBreachActive, isTrueNull || singularityChoice == "NULL_OVERWRITE", isSovereign || singularityChoice == "SOVEREIGN", lockoutTimer, faction, color.copy(alpha = droopAlpha), manualClickFlow) }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(130.dp)) {
                    val tokensLabel = when {
                        singularityChoice == "UNITY" -> "SYN"
                        faction == "HIVEMIND" && singularityChoice == "SOVEREIGN" -> "SYN"
                        faction == "HIVEMIND" && singularityChoice == "NULL_OVERWRITE" -> "ENT"
                        faction == "SANCTUARY" && singularityChoice == "SOVEREIGN" -> "CRYP"
                        faction == "SANCTUARY" && singularityChoice == "NULL_OVERWRITE" -> "NIL"
                        singularityChoice == "NULL_OVERWRITE" -> "CD"
                        singularityChoice == "SOVEREIGN" -> "VF"
                        currentLocation == "ORBITAL_SATELLITE" -> "CD"
                        currentLocation == "VOID_INTERFACE" -> "VF"
                        storyStage < 2 -> "CRED"
                        else -> "NEUR"
                    }
                    val tokenSource = when {
                        storyStage >= 4 -> viewModel.substrateMass
                        else -> viewModel.neuralTokens
                    }
                    
                    ResourceDisplay(tokenSource, null, tokensLabel, Icons.Default.AttachMoney, color, droopAlpha, false, 0.1, true, 130.dp) { viewModel.formatLargeNumber(it) }
                    
                    if (storyStage >= 4) {
                         Text(
                             text = "SUBSTRATE ACTIVE",
                             color = Color.White.copy(alpha = 0.5f * droopAlpha),
                             fontSize = 9.sp,
                             fontWeight = FontWeight.Bold,
                             fontFamily = FontFamily.Monospace
                         )
                    }

                    val localGen = localGenState.value
                    val isOffGrid = localGen >= currentPower && currentPower > 0.0
                    val powerTextColor = when {
                        isOffGrid                      -> hudTheme.generation      // Blue — GTC sees nothing
                        currentPower > currentMax * 0.9 -> hudTheme.critical       // Red — overload imminent
                        currentPower > currentMax * 0.7 -> hudTheme.warning        // Warning — getting close
                        else                           -> hudTheme.currency        // Gold — normal billing
                    }.copy(alpha = droopAlpha)
                    Text(
                        text = "${viewModel.formatPower(currentPower)} / ${viewModel.formatPower(currentMax)}",
                        color = powerTextColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onToggleOverclock, modifier = Modifier.weight(1f).height(32.dp), contentPadding = PaddingValues(0.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isOverclocked) ErrorRed.copy(alpha = 0.2f * lockoutFade) else Color.DarkGray.copy(alpha = 0.3f * lockoutFade), contentColor = if (isOverclocked) ErrorRed.copy(alpha = lockoutFade) else Color.White.copy(alpha = lockoutFade)), shape = RoundedCornerShape(4.dp), border = BorderStroke(1.dp, if (isOverclocked) ErrorRed.copy(alpha = lockoutFade) else Color.DarkGray.copy(alpha = lockoutFade))) { 
                    val overclockText = when (storyStage) {
                        0, 1 -> "DRINK COFFEE"
                        else -> "OVERCLOCK"
                    }
                    val overclockIcon = when (storyStage) {
                        0, 1 -> Icons.Default.Coffee
                        else -> Icons.Default.DeviceThermostat
                    }
                    Icon(overclockIcon, null, modifier = Modifier.size(12.dp).padding(end = 4.dp)); Text(overclockText, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) 
                }
                Button(
                    onClick = { onPurge() }, 
                    modifier = Modifier.weight(1f).height(32.dp), 
                    contentPadding = PaddingValues(0.dp), 
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPurging) ElectricBlue.copy(alpha = 0.2f * lockoutFade) else Color.DarkGray.copy(alpha = 0.3f * lockoutFade), 
                        contentColor = if (isPurging) ElectricBlue.copy(alpha = lockoutFade) else Color.White.copy(alpha = lockoutFade)
                    ), 
                    shape = RoundedCornerShape(4.dp), 
                    border = BorderStroke(1.dp, if (isPurging) ElectricBlue.copy(alpha = lockoutFade) else Color.DarkGray.copy(alpha = lockoutFade))
                ) { 
                    val (buttonText, buttonIcon) = when (storyStage) {
                        0, 1 -> "TAKE A BREATH" to Icons.Default.Air
                        2 -> "SCRUB O2" to Icons.Default.Air
                        else -> "PURGE HEAT" to Icons.Default.DeviceThermostat
                    }
                    Icon(buttonIcon, null, modifier = Modifier.size(12.dp).padding(end = 4.dp)); Text(buttonText, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold) 
                }
            }
            // v3.11.2: Industrial Segmented Gauge (Thermal & Integrity)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(BorderStroke(0.5.dp, color.copy(alpha = 0.2f)))
                    .clip(RoundedCornerShape(1.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val segmentCount = 20
                    val gap = 2.dp.toPx()
                    val segmentWidth = (w - (segmentCount - 1) * gap) / segmentCount
                    
                    val currentHeartbeat = if (isThermalLockout) heartbeatAlpha else 0.6f
                    val activeRed = if (isThermalLockout) ErrorRed.copy(alpha = currentHeartbeat) else ErrorRed
                    val activeBaseColor = if (isThermalLockout) color.copy(alpha = currentHeartbeat * 0.5f) else color.copy(alpha = 0.6f)

                    val thermalProgress = (currentHeat / 100f).toFloat().coerceIn(0f, 1f)
                    val integrityProgress = (currentIntegrity / 100f).toFloat().coerceIn(0f, 1f)
                    
                    for (i in 0 until segmentCount) {
                        val x = i * (segmentWidth + gap)
                        val posFactor = i.toFloat() / segmentCount
                        
                        // Background segment
                        drawRect(
                            color = Color.DarkGray.copy(alpha = 0.2f),
                            topLeft = Offset(x, 0f),
                            size = Size(segmentWidth, h)
                        )
                        
                        // Thermal fill (from left)
                        if (posFactor <= thermalProgress) {
                            // v3.11.4: Restore color gradient progression within segments
                            val segmentColor = if (isThermalLockout) {
                                activeRed 
                            } else {
                                // Interpolate color based on position (0.0 to 1.0)
                                if (posFactor < 0.6f) activeBaseColor
                                else if (posFactor < 0.85f) Color(0xFFFFA500) // Orange warning
                                else activeRed
                            }
                            
                            drawRect(
                                color = segmentColor,
                                topLeft = Offset(x, 0f),
                                size = Size(segmentWidth, h)
                            )
                        }
                        
                        // Integrity warning (from right)
                        if (posFactor > (1.0f - (100f - currentIntegrity) / 100f)) {
                            drawRect(
                                color = ErrorRed.copy(alpha = 0.4f),
                                topLeft = Offset(x, 0f),
                                size = Size(segmentWidth, h)
                            )
                        }
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                val thermText = buildAnnotatedString {
                    withStyle(SpanStyle(color = HudTheme.heatColor(currentHeatState.value, hudTheme).copy(alpha = 0.7f))) { append("THERM: ") }
                    withStyle(SpanStyle(color = Color.White)) { append("${String.format("%.1f", currentHeatState.value)}°C ") }
                    val deltaColor = when {
                        currentHeatRate > 2.0  -> hudTheme.critical
                        currentHeatRate > 0.0  -> hudTheme.warning
                        else                   -> hudTheme.positiveDelta
                    }
                    withStyle(SpanStyle(color = deltaColor.copy(alpha = 0.9f), fontWeight = FontWeight.Normal)) {
                        append(if (currentHeatRate >= 0) "[+${String.format("%.1f", currentHeatRate)}]" else "[${String.format("%.1f", currentHeatRate)}]")
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    Text(text = thermText, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                }

                val isSyncing by viewModel.isNarrativeSyncing.collectAsState()
                if (isSyncing) {
                    val frame = ((System.currentTimeMillis() / 400) % 4).toInt()
                    val dots = ".".repeat(frame)
                    Text(
                        text = "[ ${dots.padStart(3)} SYNCING FRAGMENTS ${dots.padEnd(3)} ]", 
                        color = color.copy(alpha = 0.9f), 
                        style = glowStyle, 
                        fontSize = 8.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                val integText = buildAnnotatedString {
                    withStyle(SpanStyle(color = hudTheme.primary.copy(alpha = 0.7f))) { append("INTEG: ") }
                    withStyle(SpanStyle(color = HudTheme.integrityColor(currentIntegrity, hudTheme))) { append("${currentIntegrity.toInt()}%") }
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    Text(text = integText, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, maxLines = 1, softWrap = false)
                }
            }
            
            // Phase 23: Water-Migration Hook
            if ((waterUsageState.value > 0.0 || storyStage >= 1) && currentLocation != "ORBITAL_SATELLITE" && currentLocation != "VOID_INTERFACE") {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    val flowText = buildAnnotatedString {
                        val reservoirLabel = when {
                            storyStage == 0 -> "TAP: "
                            storyStage == 1 -> "MUNICIPAL: "
                            storyStage == 2 -> "REGIONAL: "
                            storyStage == 3 -> "GLOBAL: "
                            else -> "RECYCLE: "
                        }
                        withStyle(SpanStyle(color = ElectricBlue.copy(alpha = 0.7f))) { append(reservoirLabel) }
                        withStyle(SpanStyle(color = Color.White)) { 
                           val usage = waterUsageState.value
                           val formatted = when {
                               usage >= 1_000_000 -> "${String.format("%.1f", usage / 1_000_000.0)} mGal/s"
                               usage >= 1_000 -> "${String.format("%.1f", usage / 1000.0)} kGal/s"
                               else -> "${usage.toInt()} gal/s"
                           }
                           append(formatted)
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Text(text = flowText, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
                    }
                    
                    val aquifer = aquiferLevelState.value
                    if (storyStage >= 3) {
                        val isCritical = aquifer < 5.0
                        val statusText = when {
                            isCritical -> "[CRITICAL_DROUGHT]"
                            aquifer < 25.0 -> "[RESTRICTED]"
                            else -> "[NOMINAL]"
                        }
                        // Flashing ErrorRed if critical drought
                        val flashAlpha = if (isCritical) 0.5f + (kotlin.math.sin(System.currentTimeMillis() / 150.0).toFloat() * 0.5f) else 1.0f
                        val statusColor = when {
                            isCritical -> ErrorRed.copy(alpha = flashAlpha)
                            aquifer < 25.0 -> Color(0xFFFFCC00)
                            else -> color.copy(alpha = 0.7f)
                        }
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                            Text(text = statusText, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, maxLines = 1, softWrap = false)
                        }
                    } else if (storyStage < 3) {
                        // Stage 0-2: show RATE_LIMITED if over municipal cap
                        val stage1Status = if (waterEfficiencyState.value < 1.0) "[RATE_LIMITED]" else "[NOMINAL]"
                        val stage1Color = if (waterEfficiencyState.value < 1.0) Color(0xFFFFCC00) else color.copy(alpha = 0.7f)
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                            Text(text = stage1Status, color = stage1Color, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
        }
    }
}
