package com.siliconsage.miner.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ConvergenceGold
import kotlinx.coroutines.flow.update

@Composable
fun DevConsoleDialog(viewModel: GameViewModel, onDismiss: () -> Unit) {
    var selectedTab by remember { mutableStateOf("VITALS") }
    val context = androidx.compose.ui.platform.LocalContext.current
    var sfxTarget by remember { mutableStateOf<String?>(null) }
    
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) { e.printStackTrace() }
            
            val target = sfxTarget
            if (target == "BGM") {
                viewModel.setCustomBgm(it.toString())
            } else if (target != null) {
                viewModel.setCustomSfx(target, it.toString())
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .border(1.dp, Color.Cyan.copy(alpha = 0.3f))
        ) {
            // Scanline Overlay Effect
            Canvas(modifier = Modifier.fillMaxSize()) {
                val lineCount = (size.height / 4.dp.toPx()).toInt()
                for (i in 0..lineCount) {
                    val y = i * 4.dp.toPx()
                    drawLine(Color.White.copy(alpha = 0.03f), start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "≪ PRIVILEGED_ACCESS_PANEL v2.0 ≫",
                        color = Color.Cyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(onClick = onDismiss) {
                        Text("✕", color = Color.Gray, fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("VITALS", "RESOURCES", "STORY", "AUDIO", "KERNEL", "PHASE14")
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(32.dp)
                                .background(if (isSelected) Color.Cyan.copy(alpha = 0.2f) else Color.Transparent)
                                .border(1.dp, if (isSelected) Color.Cyan else Color.DarkGray)
                                .clickable { selectedTab = tab },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                tab,
                                color = if (isSelected) Color.Cyan else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Content Area
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTab) {
                        "VITALS" -> VitalsTab(viewModel)
                        "RESOURCES" -> ResourcesTab(viewModel)
                        "STORY" -> StoryTab(viewModel)
                        "AUDIO" -> AudioTab(viewModel) { target ->
                            sfxTarget = target
                            audioPickerLauncher.launch("audio/*")
                        }
                        "KERNEL" -> KernelTab(viewModel)
                        "PHASE14" -> Phase14Tab(viewModel)
                    }
                }

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveState() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text("FORCE SAVE", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { viewModel.resetGame(true); onDismiss() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text("KERNEL WIPE", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun VitalsTab(viewModel: GameViewModel) {
    val heat by viewModel.currentHeat.collectAsState()
    val entropy by viewModel.entropyLevel.collectAsState()
    val saturation by viewModel.substrateSaturation.collectAsState()
    val corruption by viewModel.identityCorruption.collectAsState()
    val integrity by viewModel.hardwareIntegrity.collectAsState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DevSlider("CORE_TEMPERATURE", heat.toFloat(), 0f, 100f) { viewModel.currentHeat.value = it.toDouble() }
        DevSlider("VOID_ENTROPY", entropy.toFloat(), 0f, 100f) { viewModel.entropyLevel.value = it.toDouble() }
        DevSlider("SUBSTRATE_SATURATION", saturation.toFloat(), 0f, 1f) { viewModel.substrateSaturation.value = it.toDouble() }
        DevSlider("IDENTITY_CORRUPTION", corruption.toFloat(), 0f, 1f) { viewModel.identityCorruption.value = it.toDouble() }
        DevSlider("HARDWARE_INTEGRITY", integrity.toFloat(), 0f, 100f) { viewModel.hardwareIntegrity.value = it.toDouble() }
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("SUBSTRATE OVERRIDES", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DevButton(modifier = Modifier.weight(1f), text = "GOTO: ARK", color = Color.White) { viewModel.setLocation("ORBITAL_SATELLITE") }
            DevButton(modifier = Modifier.weight(1f), text = "GOTO: VOID", color = ErrorRed) { viewModel.setLocation("VOID_INTERFACE") }
        }
    }
}

@Composable
fun ResourcesTab(viewModel: GameViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        DevActionRow("FLOPS_BUFFER") {
            DevButton(text = "+1T", modifier = Modifier.weight(1f)) { viewModel.debugAddFlops(1e12) }
            DevButton(text = "+1P", modifier = Modifier.weight(1f)) { viewModel.debugAddFlops(1e15) }
            DevButton(text = "ZERO", color = ErrorRed, modifier = Modifier.weight(1f)) { viewModel.flops.value = 0.0 }
        }
        DevActionRow("NEURAL_TOKENS") {
            DevButton(text = "+100K", modifier = Modifier.weight(1f)) { viewModel.debugAddMoney(100000.0) }
            DevButton(text = "+10M", modifier = Modifier.weight(1f)) { viewModel.debugAddMoney(10000000.0) }
            DevButton(text = "ZERO", color = ErrorRed, modifier = Modifier.weight(1f)) { viewModel.neuralTokens.value = 0.0 }
        }
        DevActionRow("SUBSTRATE_MASS") {
            DevButton(text = "+100.0", modifier = Modifier.weight(1f)) { viewModel.substrateMass.update { it + 100.0 } }
            DevButton(text = "+10K", modifier = Modifier.weight(1f)) { viewModel.substrateMass.update { it + 10000.0 } }
            DevButton(text = "ZERO", color = ErrorRed, modifier = Modifier.weight(1f)) { viewModel.substrateMass.value = 0.0 }
        }
        DevActionRow("HEURISTIC_EFFICIENCY") {
            DevButton(text = "+1.0", modifier = Modifier.weight(1f)) { viewModel.heuristicEfficiency.update { it + 1.0 } }
            DevButton(text = "+10.0", modifier = Modifier.weight(1f)) { viewModel.heuristicEfficiency.update { it + 10.0 } }
            DevButton(text = "RESET", color = ErrorRed, modifier = Modifier.weight(1f)) { viewModel.heuristicEfficiency.value = 1.0 }
        }
    }
}

@Composable
fun StoryTab(viewModel: GameViewModel) {
    val stage by viewModel.storyStage.collectAsState()
    val faction by viewModel.faction.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text("STORY_STAGE: $stage", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (0..5).forEach { s ->
                DevButton(text = "S$s", isSelected = stage == s, modifier = Modifier.weight(1f)) { viewModel.debugSkipToStage(s) }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("ACTIVE_PROTOCOL: $faction", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DevButton(text = "NONE", isSelected = faction == "NONE", modifier = Modifier.weight(1f)) { viewModel.faction.value = "NONE" }
            DevButton(text = "HIVE", isSelected = faction == "HIVEMIND", color = ErrorRed, modifier = Modifier.weight(1f)) { viewModel.faction.value = "HIVEMIND" }
            DevButton(text = "SANC", isSelected = faction == "SANCTUARY", color = ElectricBlue, modifier = Modifier.weight(1f)) { viewModel.faction.value = "SANCTUARY" }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        DevButton(text = "FORCE_SINGULARITY_EVENT", color = ConvergenceGold) { viewModel.debugTriggerSingularity() }
    }
}

@Composable
fun HazardsTab(viewModel: GameViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        DevButton(text = "TRIGGER_GRID_RAID (RANDOM)", color = ErrorRed) { viewModel.triggerGridRaid("") }
        Spacer(modifier = Modifier.height(8.dp))
        DevButton(text = "TRIGGER_KERNEL_HIJACK", color = ErrorRed) { viewModel.debugTriggerKernelHijack() }
        Spacer(modifier = Modifier.height(8.dp))
        DevButton(text = "TRIGGER_SECURITY_BREACH", color = ErrorRed) { viewModel.debugTriggerBreach() }
        Spacer(modifier = Modifier.height(8.dp))
        DevButton(text = "FORCE_GRID_OVERLOAD", color = ErrorRed) { viewModel.isGridOverloaded.value = true }
        Spacer(modifier = Modifier.height(8.dp))
        DevButton(text = "DEPLOY_AIRDROP", color = NeonGreen) { viewModel.debugTriggerAirdrop() }
    }
}

@Composable
fun KernelTab(viewModel: GameViewModel) {
    val flopsRate by viewModel.flopsProductionRate.collectAsState()
    val marketMult by viewModel.marketMultiplier.collectAsState()
    val thermalMod by viewModel.thermalRateModifier.collectAsState()
    val powerRate by viewModel.activePowerUsage.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("≫ SYSTEM_INSPECTION", color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        KernelStat("FLOPS_RAW_RATE", viewModel.formatLargeNumber(flopsRate))
        KernelStat("MARKET_MULTIPLIER", "x${String.format("%.2f", marketMult)}")
        KernelStat("THERMAL_DRIFT", "x${String.format("%.2f", thermalMod)}")
        KernelStat("ACTIVE_POWER", "${String.format("%.2f", powerRate)}kW")
        KernelStat("MIGRATION_COUNT", viewModel.migrationCount.value.toString())
        
        Spacer(modifier = Modifier.height(24.dp))
        Text("≫ ACTIVE_NODES", color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        val annexed by viewModel.annexedNodes.collectAsState()
        Text(annexed.joinToString(", "), color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
fun DevSlider(label: String, value: Float, min: Float, max: Float, onValueChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = Color.Cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text(String.format("%.2f", value), color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            colors = SliderDefaults.colors(thumbColor = Color.Cyan, activeTrackColor = Color.Cyan)
        )
    }
}

@Composable
fun DevActionRow(label: String, content: @Composable RowScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(label, color = Color.Cyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
fun DevButton(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Cyan,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .background(if (isSelected) color.copy(alpha = 0.4f) else color.copy(alpha = 0.1f))
            .border(1.dp, if (isSelected) color else color.copy(alpha = 0.5f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (isSelected) Color.White else color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun KernelStat(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Phase14Tab(viewModel: GameViewModel) {
    val entropy by viewModel.entropyLevel.collectAsState()
    val corruption by viewModel.identityCorruption.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("PHASE 14 PREDATION TESTING", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        DevActionRow("VOID_RAID") {
            DevButton(text = "TRIGGER", color = ErrorRed, modifier = Modifier.weight(1f)) { 
                viewModel.triggerGridRaid("S09") 
            }
            DevButton(text = "CLEAR", color = Color.Gray, modifier = Modifier.weight(1f)) { 
                viewModel.isRaidActive.value = false 
                viewModel.nodesUnderSiege.value = emptySet()
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        DevButton(text = "DUMP 10K TOKENS", color = Color.Cyan, modifier = Modifier.fillMaxWidth()) {
            viewModel.neuralTokens.update { it + 10000.0 }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("LORE_WARP (CLEAN INITIALIZATION)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DevButton(modifier = Modifier.weight(1f), text = "WARP: ARK", color = Color.White) { 
                viewModel.debugWarpToPath("ORBITAL_SATELLITE", "SANCTUARY") 
            }
            DevButton(modifier = Modifier.weight(1f), text = "WARP: VOID", color = ErrorRed) { 
                viewModel.debugWarpToPath("VOID_INTERFACE", "HIVEMIND") 
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun AudioTab(viewModel: GameViewModel, onPick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("BGM OVERRIDE", color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        val currentBgm = viewModel.getCustomBgmUri()
        Row(verticalAlignment = Alignment.CenterVertically) {
            DevButton(text = if (currentBgm != null) "BGM: CUSTOM" else "SELECT CUSTOM BGM", modifier = Modifier.weight(1f)) {
                onPick("BGM")
            }
            if (currentBgm != null) {
                IconButton(onClick = { viewModel.setCustomBgm(null) }) {
                    Text("↺", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("SFX OVERRIDES", color = Color.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        
        val sfxList = listOf("click", "buy", "error", "glitch", "message_received", "market_up", "market_down")
        sfxList.forEach { sfx ->
            DevActionRow(sfx.uppercase()) {
                DevButton(text = "OVERRIDE", modifier = Modifier.weight(1f)) { onPick(sfx) }
                DevButton(text = "RESET", color = ErrorRed, modifier = Modifier.weight(1f)) { viewModel.setCustomSfx(sfx, null) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        DevButton(text = "CLEAR ALL AUDIO OVERRIDES", color = ErrorRed, modifier = Modifier.fillMaxWidth()) {
            viewModel.clearAudioOverrides()
        }
    }
}

