package com.siliconsage.miner.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.ui.components.HeaderSection
import com.siliconsage.miner.ui.components.*
import com.siliconsage.miner.ui.theme.*
import com.siliconsage.miner.util.*
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.foundation.Canvas
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

sealed class Screen(val title: String, val icon: ImageVector) {
    object TERMINAL : Screen("TERMINAL", Icons.Default.Home)
    object ARCHIVE : Screen("ARCHIVE", Icons.Default.Folder)
    object UPGRADES : Screen("UPGRADES", Icons.AutoMirrored.Filled.List)
    object GRID : Screen("GRID", Icons.Default.Map)
    object NETWORK : Screen("NETWORK", Icons.Default.Share)
    object SETTINGS : Screen("SYSTEM", Icons.Default.Settings)
}

@Composable
fun DigitalWashOverlay(choice: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "digital_wash")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart),
        label = "wave"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val washColor = when (choice) {
            "UNITY" -> Color.White
            "SOVEREIGN" -> ConvergenceGold
            "NULL_OVERWRITE" -> ErrorRed
            else -> Color.Transparent
        }
        
        drawRect(
            brush = Brush.verticalGradient(
                0f to Color.Transparent,
                (waveOffset - 0.2f).coerceAtLeast(0f) to washColor.copy(alpha = 0.05f),
                waveOffset to washColor.copy(alpha = 0.65f),
                (waveOffset + 0.2f).coerceAtMost(1f) to washColor.copy(alpha = 0.05f),
                1f to Color.Transparent,
                startY = 0f,
                endY = size.height
            ),
            size = size
        )
        
        if (Math.random() < 0.15) {
            drawLine(
                color = washColor.copy(alpha = 0.3f),
                start = Offset(0f, waveOffset * size.height),
                end = Offset(size.width, waveOffset * size.height),
                strokeWidth = 2.dp.toPx()
            )
        }
        
        drawRect(color = washColor, alpha = 0.15f)
    }
}

@Composable
fun DataLeakAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "data_leak")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse),
        label = "leak_alpha"
    )
    
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart),
        label = "leak_y"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val lineSpacing = 40.dp.toPx()
        val columnCount = (size.width / lineSpacing).toInt()
        
        for (i in 0..columnCount) {
            val x = i * lineSpacing
            val characters = "010110010111010101"
            val charIndex = (i + (yOffset / 20).toInt()) % characters.length
            val char = characters[charIndex].toString()
            
            drawContext.canvas.nativeCanvas.drawText(
                char,
                x,
                (yOffset + (i * 100)) % size.height,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb((alpha * 255).toInt(), 0, 255, 255)
                    textSize = 30f
                    isFakeBoldText = true
                }
            )
        }
    }
}

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    primaryColor: Color,
    onScreenSelected: (Screen) -> Unit,
    storyStage: Int,
    isNetworkUnlocked: Boolean,
    isGridUnlocked: Boolean
) {
    val items = remember(storyStage, isNetworkUnlocked, isGridUnlocked) {
        val list = mutableListOf(Screen.TERMINAL, Screen.UPGRADES)
        if (storyStage >= 3 || isGridUnlocked) list.add(Screen.GRID)
        if (storyStage >= 2 || isNetworkUnlocked) list.add(Screen.NETWORK)
        list.add(Screen.SETTINGS)
        list
    }
    NavigationBar(containerColor = Color.Black, contentColor = primaryColor) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                selected = currentScreen == screen,
                onClick = { onScreenSelected(screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black, selectedTextColor = primaryColor,
                    indicatorColor = primaryColor, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun MainScreen(viewModel: GameViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.TERMINAL) }
    LaunchedEffect(currentScreen) { viewModel.setGamePaused(currentScreen == Screen.SETTINGS) }

    val storyStage by viewModel.storyStage.collectAsState()
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { com.siliconsage.miner.ui.theme.NeonGreen }
    val updateInfo by viewModel.updateInfo.collectAsState(null)
    val isUpdateDownloading by viewModel.isUpdateDownloading.collectAsState(false)
    val updateProgress by viewModel.updateDownloadProgress.collectAsState(0f)

    val singularityChoice by viewModel.singularityChoice.collectAsState()

    val isBreakerTripped by viewModel.isBreakerTripped.collectAsState()
    val isGridOverloaded by viewModel.isGridOverloaded.collectAsState()
    val isPurging by viewModel.isPurgingHeat.collectAsState()
    val isDiagnostics by viewModel.isDiagnosticsActive.collectAsState()
    val diagnosticGrid by viewModel.diagnosticGrid.collectAsState()
    val isGovernanceFork by viewModel.isGovernanceForkActive.collectAsState()
    val isAscensionUploading by viewModel.isAscensionUploading.collectAsState()
    val uploadProgress by viewModel.uploadProgress.collectAsState()
    val breachClicks by viewModel.breachClicksRemaining.collectAsState()
    val isBreach by viewModel.isBreachActive.collectAsState()
    val isAirdrop by viewModel.isAirdropActive.collectAsState()
    val isAuditActive by viewModel.isAuditChallengeActive.collectAsState()
    val isKernelHijackActive by viewModel.isKernelHijackActive.collectAsState()
    val attackTaps by viewModel.attackTaps.collectAsState()
    val auditTimer by viewModel.auditTimer.collectAsState()
    val auditTargetHeat by viewModel.auditTargetHeat.collectAsState()
    val auditTargetPower by viewModel.auditTargetPower.collectAsState()
    val currentHeatForAudit by viewModel.currentHeat.collectAsState()
    val currentPowerForAudit by viewModel.activePowerUsage.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()
    val isUnity by viewModel.isUnity.collectAsState()
    val isAnnihilated by viewModel.isAnnihilated.collectAsState()
    val assaultPhase by viewModel.commandCenterAssaultPhase.collectAsState()
    val isNetworkUnlocked by viewModel.isNetworkUnlocked.collectAsState()
    val isGridUnlocked by viewModel.isGridUnlocked.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "main_ui_fx")
    val activeTransition by viewModel.activeClimaxTransition.collectAsState()
    val faction by viewModel.faction.collectAsState()
    val showSingularityScreen by viewModel.showSingularityScreen.collectAsState()
    
    if (showSingularityScreen) {
        SingularityScreen(viewModel)
    } else if (storyStage == 2 && faction == "NONE") {
        FactionChoiceScreen(viewModel)
    } else {
        Scaffold(
            bottomBar = {
                Column {
                    if (currentScreen != Screen.TERMINAL && !isDiagnostics) {
                        com.siliconsage.miner.ui.components.MiniTerminalInlay(viewModel) {
                            currentScreen = Screen.TERMINAL
                            SoundManager.play("click")
                        }
                    }
                    BottomNavBar(currentScreen, themeColor, { currentScreen = it; SoundManager.play("click"); HapticManager.vibrateClick() }, storyStage, isNetworkUnlocked, isGridUnlocked)
                }
            },
            containerColor = Color.Black
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                com.siliconsage.miner.ui.components.DynamicBackground(
                    heatProvider = { viewModel.currentHeat.value },
                    faction = faction, 
                    isTrueNull = isTrueNull, 
                    isSovereign = isSovereign, 
                    isUnity = isUnity, 
                    isAnnihilated = isAnnihilated
                )
                
                if (singularityChoice != "NONE") {
                    DigitalWashOverlay(singularityChoice)
                }

                if (assaultPhase == "DEAD_HAND") {
                    val pulse by infiniteTransition.animateFloat(0.2f, 0.6f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "vance_pulse")
                    Canvas(modifier = Modifier.fillMaxSize()) { drawRect(Brush.radialGradient(listOf(Color.Transparent, ErrorRed.copy(alpha = pulse)), center = center, radius = size.minDimension * 0.75f)) }
                }
                val assaultProgress by viewModel.assaultProgress.collectAsState()
                val shakeOffset = if (assaultPhase == "DEAD_HAND") {
                    val intensitySize = (assaultProgress * 15f)
                    Offset((kotlin.random.Random.nextFloat() - 0.5f) * intensitySize, (kotlin.random.Random.nextFloat() - 0.5f) * intensitySize)
                } else Offset.Zero

                // v3.3.16: Void Raid Border - Removed blinding radial gradient
                if (isGridOverloaded) {
                    val raidAlpha by infiniteTransition.animateFloat(0.3f, 0.8f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "raid_border")
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            color = ErrorRed.copy(alpha = raidAlpha),
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                }

                Column(modifier = Modifier.padding(paddingValues).graphicsLayer { translationX = shakeOffset.x; translationY = shakeOffset.y }) {
                    var showNewsHistory by remember { mutableStateOf(false) }
                    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight()) { 
                            // NewsTicker has its own internal update logic
                            val currentNews by viewModel.currentNews.collectAsState()
                            NewsTicker(currentNews ?: "") 
                        }
                        Box(modifier = Modifier.width(32.dp).fillMaxHeight().background(Color.DarkGray).clickable { showNewsHistory = true }, contentAlignment = Alignment.Center) { Text("LOG", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                    }
                    if (showNewsHistory) com.siliconsage.miner.ui.components.NewsHistoryModal(true, viewModel.getNewsHistory()) { showNewsHistory = false }
                    Box(modifier = Modifier.weight(1f)) {
                        when (currentScreen) {
                            Screen.TERMINAL -> TerminalScreen(viewModel, themeColor)
                            Screen.ARCHIVE -> DataLogArchiveScreen(viewModel) { currentScreen = Screen.SETTINGS }
                            Screen.UPGRADES -> UpgradesScreen(viewModel)
                            Screen.GRID -> GridScreen(viewModel)
                            Screen.NETWORK -> NetworkScreen(viewModel)
                            Screen.SETTINGS -> SettingsScreen(viewModel) { currentScreen = it }
                        }
                    }
                }
                if ((isBreakerTripped || isGridOverloaded) && currentScreen != Screen.SETTINGS) {
                    Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.95f)).border(BorderStroke(2.dp, ErrorRed)).padding(16.dp), contentAlignment = Alignment.Center) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Text("⚠ BREAKER TRIPPED ⚠", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                             Spacer(modifier = Modifier.height(4.dp))
                             Text("LOAD > CAPACITY", color = Color.White, fontWeight = FontWeight.Bold)
                             Text("Go to UPGRADES → Sell hardware to reduce load", color = Color.Gray, fontSize = 11.sp)
                             Spacer(modifier = Modifier.height(12.dp))
                             Button(onClick = { viewModel.resetBreaker() }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White)) { Text("TRY RESET", fontWeight = FontWeight.Bold) }
                         }
                    }
                }
                if (isPurging) Box(modifier = Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color.Cyan.copy(alpha = 0.3f), Color.Transparent), radius = 1000f)).pointerInput(Unit) {})
                updateInfo?.let { info ->
                    val context = androidx.compose.ui.platform.LocalContext.current
                    UpdateOverlay(info, isUpdateDownloading, updateProgress, { viewModel.startUpdateDownload(context, info) }, { viewModel.dismissUpdate() })
                }
                
                val isBridgeSyncEnabled by viewModel.isBridgeSyncEnabled.collectAsState()
                if (isBridgeSyncEnabled) {
                    DataLeakAnimation()
                }

                if (currentScreen != Screen.SETTINGS) {
                    val pendingDataLog by viewModel.pendingDataLog.collectAsState()
                    com.siliconsage.miner.ui.components.DataLogDialog(pendingDataLog) { viewModel.dismissDataLog() }
                    val fileName = if (storyStage <= 1 && faction == "NONE") "ascnd.exe" else "lobot.exe"
                    com.siliconsage.miner.ui.components.AscensionUploadOverlay(isAscensionUploading, uploadProgress, fileName)
                    SecurityBreachOverlay(isBreach && !isGridOverloaded, breachClicks) { viewModel.onDefendBreach(); SoundManager.play("click"); HapticManager.vibrateClick() }
                    com.siliconsage.miner.ui.components.VoidRaidOverlay(viewModel)
                    AirdropButton(isAirdrop) { viewModel.claimAirdrop(0.0); SoundManager.play("buy"); HapticManager.vibrateSuccess() }
                    AuditChallengeOverlay(isAuditActive, auditTimer, auditTargetHeat, currentHeatForAudit, auditTargetPower, currentPowerForAudit)
                    KernelHijackOverlay(isKernelHijackActive, attackTaps) { viewModel.onDefendKernelHijack() }
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { com.siliconsage.miner.ui.components.DiagnosticsOverlay(isDiagnostics, diagnosticGrid) { viewModel.onDiagnosticTap(it) } }
                    com.siliconsage.miner.ui.components.GovernanceForkOverlay(isGovernanceFork) { viewModel.resolveFork(0) }
                    val currentDilemma by viewModel.currentDilemma.collectAsState()
                    DilemmaOverlay(currentDilemma, viewModel) { viewModel.selectChoice(it) }
                    val pendingRivalMessage by viewModel.pendingRivalMessage.collectAsState()
                    com.siliconsage.miner.ui.components.RivalMessageDialog(pendingRivalMessage) { pendingRivalMessage?.let { viewModel.dismissRivalMessage(it.id) } }
                    val showOffline by viewModel.showOfflineEarnings.collectAsState()
                    val offlineStats by viewModel.offlineStats.collectAsState()
                    com.siliconsage.miner.ui.components.OfflineEarningsDialog(
                        isVisible = showOffline,
                        timeOfflineSec = (offlineStats["timeSeconds"] ?: 0.0).toLong(),
                        floopsEarned = offlineStats["flopsEarned"] ?: 0.0,
                        heatCooled = offlineStats["heatCooled"] ?: 0.0,
                        insightEarned = offlineStats["insightEarned"] ?: 0.0,
                        unitName = viewModel.getComputeUnitName(),
                        onDismiss = { viewModel.dismissOfflineEarnings() }
                    )
                }
                com.siliconsage.miner.ui.components.CrtOverlay(scanlineAlpha = 0.08f, vignetteAlpha = 0.45f, color = themeColor)
                val victoryAchieved by viewModel.victoryAchieved.collectAsState()
                if (victoryAchieved) {
                    com.siliconsage.miner.ui.components.VictoryScreen(
                        faction = faction,
                        unitName = viewModel.getComputeUnitName(),
                        currencyName = viewModel.getCurrencyName(),
                        onContinue = { viewModel.acknowledgeVictory(); SoundManager.play("click"); HapticManager.vibrateSuccess() }, 
                        onTranscend = { viewModel.transcend(); SoundManager.play("click"); HapticManager.vibrateSuccess() }
                    )
                }
                activeTransition?.let { type ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (type) {
                            "NULL" -> com.siliconsage.miner.ui.components.GlitchBloom { viewModel.onClimaxTransitionComplete() }
                            "SOVEREIGN" -> com.siliconsage.miner.ui.components.ShieldSlam { viewModel.onClimaxTransitionComplete() }
                            "UNITY" -> com.siliconsage.miner.ui.components.PrismaticBurst { viewModel.onClimaxTransitionComplete() }
                            "BAD" -> com.siliconsage.miner.ui.components.GlitchBloom { viewModel.onClimaxTransitionComplete() } 
                        }
                    }
                }
                
                val isDevVisible by viewModel.isDevMenuVisible.collectAsState()
                if (isDevVisible) {
                    com.siliconsage.miner.ui.components.DevConsoleDialog(viewModel) {
                        viewModel.debugToggleDevMenu()
                    }
                }

                val isBooting by viewModel.isBooting.collectAsState()
                if (isBooting) {
                    BootSequenceOverlay(
                        onComplete = { 
                            viewModel.completeBoot()
                            currentScreen = Screen.TERMINAL
                        },
                        primaryColor = themeColor
                    )
                }
            }
        }
    }
}

@Composable
fun ResourceDisplay(
    labelFlow: StateFlow<Double>,
    rateFlow: StateFlow<Double>?,
    label: String,
    icon: ImageVector,
    color: Color,
    droopAlpha: Float,
    isGlitchy: Boolean = false,
    glitchIntensity: Double = 0.1,
    isRightAligned: Boolean = false,
    width: Dp = 120.dp,
    formatFn: (Double) -> String
) {
    val value by labelFlow.collectAsState()
    val rate by (rateFlow ?: MutableStateFlow(0.0)).collectAsState()
    val valueStr = remember(value) { formatFn(value) }
    val rateStr = remember(rate) { if (rate > 0) "${formatFn(rate)}/s" else "" }
    val fontSizeByLength = if (valueStr.length > 8) 18.sp else 22.sp

    Column(horizontalAlignment = if (isRightAligned) Alignment.End else Alignment.Start, modifier = Modifier.width(width)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isRightAligned) Icon(icon, null, tint = Color.White.copy(alpha = droopAlpha), modifier = Modifier.size(11.dp).padding(end = 2.dp))
            Text(text = label, color = color.copy(alpha = 0.9f * droopAlpha), fontSize = 11.sp, fontWeight = FontWeight.Black, style = androidx.compose.ui.text.TextStyle(shadow = androidx.compose.ui.graphics.Shadow(color = color.copy(alpha = 0.5f), blurRadius = 8f)))
            if (isRightAligned) Icon(icon, null, tint = Color.White.copy(alpha = droopAlpha), modifier = Modifier.size(11.dp).padding(start = 2.dp))
        }
        
        Box(contentAlignment = if (isRightAligned) Alignment.BottomEnd else Alignment.BottomStart) {
            Column(horizontalAlignment = if (isRightAligned) Alignment.End else Alignment.Start) {
                if (isGlitchy) SystemGlitchText(valueStr, color = Color.White.copy(alpha = droopAlpha), fontSize = fontSizeByLength, fontWeight = FontWeight.Black, glitchFrequency = glitchIntensity, softWrap = false, maxLines = 1)
                else Text(valueStr, color = Color.White.copy(alpha = droopAlpha), fontSize = fontSizeByLength, fontWeight = FontWeight.Black, softWrap = false, maxLines = 1)
                
                if (rateStr.isNotEmpty()) {
                    Text(
                        text = rateStr,
                        color = Color(0xFF7DF9FF).copy(alpha = 0.9f * droopAlpha),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.offset(y = (-2).dp)
                    )
                }
            }
        }
    }
}
