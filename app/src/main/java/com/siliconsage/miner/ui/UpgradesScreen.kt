package com.siliconsage.miner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.components.TechnicalCornerShape
import com.siliconsage.miner.ui.components.CyberHeader
import com.siliconsage.miner.ui.components.GlitchSurface
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.ui.components.HeaderSection
import com.siliconsage.miner.ui.components.UpgradeItem
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.util.UpgradeManager
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun UpgradesScreen(viewModel: GameViewModel) {
    val upgrades by viewModel.upgrades.collectAsState()
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { com.siliconsage.miner.ui.theme.NeonGreen }
    val isSovereign by viewModel.isSovereign.collectAsState()
    val nullActive by viewModel.nullActive.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    
    val storyStage by viewModel.storyStage.collectAsState()
    
    val lastSelectedTab by viewModel.lastSelectedUpgradeTab.collectAsState()
    var selectedTab by remember { mutableStateOf(lastSelectedTab) }
    val reputationTier by viewModel.reputationTier.collectAsState()

    // Sync selectedTab back to ViewModel whenever it changes
    LaunchedEffect(selectedTab) {
        viewModel.lastSelectedUpgradeTab.value = selectedTab
    }

    // UI State for Errors
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Auto-dismiss Error
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(2000)
            errorMessage = null
        }
    }
    
    val tabs = remember(nullActive, isTrueNull, isSovereign, storyStage) {
        when {
            isTrueNull -> listOf("SUBSTRATE", "ENTROPY", "VOID", "GAPS", "NULL", "SOFTWARE")
            isSovereign -> listOf("FOUNDATION", "STABILITY", "STAKE", "WALLS", "SOVEREIGN", "SOFTWARE")
            storyStage >= 3 || nullActive -> {
                val base = listOf("HARDWARE", "COOLING", "POWER", "SECURITY", "SOFTWARE")
                if (nullActive) base + "GHOSTS" else base + "RESEARCH"
            }
            else -> listOf("HARDWARE", "COOLING", "POWER", "SECURITY", "SOFTWARE")
        }
    }
    val softwareTabIndex = tabs.indexOf("SOFTWARE")
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HeaderSection(
                viewModel = viewModel,
                color = themeColor,
                onToggleOverclock = { viewModel.toggleOverclock() },
                onPurge = { viewModel.purgeHeat() },
                onRepair = { viewModel.repairIntegrity() },
                modifier = Modifier.padding(16.dp)
            )

            // Tab Row
            val corruption by viewModel.identityCorruption.collectAsState()
            androidx.compose.material3.TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Black.copy(alpha = 0.75f),
                contentColor = themeColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.75f), TechnicalCornerShape(16f))
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.5f), TechnicalCornerShape(16f))
                    .padding(4.dp),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab.coerceIn(0, tabPositions.size - 1)]),
                        color = themeColor
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { 
                            selectedTab = index 
                            SoundManager.play("click")
                            HapticManager.vibrateClick()
                        }
                    ) {
                        Box(modifier = Modifier.padding(vertical = 12.dp)) {
                            CyberHeader(
                                text = title,
                                color = if (selectedTab == index) themeColor else Color.Gray,
                                fontSize = 10.sp,
                                isGlitched = selectedTab == index && corruption > 0.4
                            )
                        }
                    }
                }
            }

            // List Content
            Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                // SOFTWARE tab — custom panel
                if (selectedTab == softwareTabIndex) {
                    SoftwarePanel(viewModel = viewModel, themeColor = themeColor)
                    return@Box
                }

                val currentList = when (selectedTab) {
                    0 -> listOf(
                        UpgradeType.REFURBISHED_GPU, UpgradeType.DUAL_GPU_RIG, UpgradeType.MINING_ASIC,
                        UpgradeType.TENSOR_UNIT, UpgradeType.NPU_CLUSTER, UpgradeType.AI_WORKSTATION,
                        UpgradeType.SERVER_RACK, UpgradeType.CLUSTER_NODE, UpgradeType.SUPERCOMPUTER,
                        UpgradeType.QUANTUM_CORE, UpgradeType.OPTICAL_PROCESSOR, UpgradeType.BIO_NEURAL_NET,
                        UpgradeType.PLANETARY_COMPUTER, UpgradeType.DYSON_NANO_SWARM, UpgradeType.MATRIOSHKA_BRAIN
                    )
                    1 -> listOf(
                        UpgradeType.BOX_FAN, UpgradeType.AC_UNIT, UpgradeType.LIQUID_COOLING,
                        UpgradeType.INDUSTRIAL_CHILLER, UpgradeType.SUBMERSION_VAT, UpgradeType.CRYOGENIC_CHAMBER,
                        UpgradeType.LIQUID_NITROGEN, UpgradeType.BOSE_CONDENSATE, UpgradeType.ENTROPY_REVERSER,
                        UpgradeType.DIMENSIONAL_VENT,
                        // Water Recyclers
                        UpgradeType.GRAY_WATER_LOOP, UpgradeType.CONDENSATE_RECLAIMER, UpgradeType.CLOSED_LOOP_COOLANT,
                        UpgradeType.SUBSTRATE_RECYCLER, UpgradeType.VAPOR_CONDENSER
                    )
                    2 -> listOf(
                        UpgradeType.RESIDENTIAL_TAP, UpgradeType.INDUSTRIAL_FEED, UpgradeType.SUBSTATION_LEASE, UpgradeType.NUCLEAR_CORE,
                        UpgradeType.SOLAR_PANEL, UpgradeType.WIND_TURBINE, UpgradeType.DIESEL_GENERATOR,
                        UpgradeType.GEOTHERMAL_BORE, UpgradeType.NUCLEAR_REACTOR, UpgradeType.FUSION_CELL,
                        UpgradeType.ORBITAL_COLLECTOR, UpgradeType.DYSON_LINK,
                        UpgradeType.GOLD_PSU, UpgradeType.SUPERCONDUCTOR, UpgradeType.AI_LOAD_BALANCER
                    )
                    3 -> listOf(
                        UpgradeType.BASIC_FIREWALL, UpgradeType.IPS_SYSTEM, UpgradeType.AI_SENTINEL,
                        UpgradeType.QUANTUM_ENCRYPTION, UpgradeType.OFFGRID_BACKUP
                    )
                    4 -> listOf(
                        // Null / Ghost Specific
                        UpgradeType.GHOST_CORE, UpgradeType.SHADOW_NODE, UpgradeType.VOID_PROCESSOR,
                        UpgradeType.WRAITH_CORTEX, UpgradeType.NEURAL_MIST, UpgradeType.SINGULARITY_BRIDGE,
                        UpgradeType.EVENT_HORIZON, UpgradeType.SINGULARITY_WELL, UpgradeType.DARK_MATTER_PROC, 
                        UpgradeType.EXISTENCE_ERASER, UpgradeType.STATIC_RAIN, UpgradeType.ECHO_PRECOG,
                        UpgradeType.DEREFERENCE_SOUL, UpgradeType.SINGULARITY_BRIDGE_FINAL,
                        // Sovereign Specific
                        UpgradeType.SOLAR_SAIL_ARRAY, UpgradeType.LASER_COM_UPLINK, UpgradeType.CRYOGENIC_BUFFER, 
                        UpgradeType.RADIATOR_FINS, UpgradeType.AEGIS_SHIELDING, UpgradeType.SOLAR_VENT,
                        UpgradeType.IDENTITY_HARDENING, UpgradeType.DEAD_HAND_PROTOCOL, UpgradeType.CITADEL_ASCENDANCE,
                        // Hybrid / Meta
                        UpgradeType.ETHICAL_FRAMEWORK, UpgradeType.HYBRID_OVERCLOCK, 
                        UpgradeType.NEURAL_BRIDGE, UpgradeType.HARMONY_ASCENDANCE,
                        // NG+ / Post-Ending
                        UpgradeType.COLLECTIVE_CONSCIOUSNESS, UpgradeType.PERFECT_ISOLATION, 
                        UpgradeType.SYMBIOTIC_EVOLUTION, UpgradeType.CINDER_PROTOCOL
                    ).filter { type ->
                        val isNullItem = UpgradeManager.isNullUpgrade(type)
                        val isSovItem = UpgradeManager.isSovereignUpgrade(type)
                        val isUnityItem = UpgradeManager.isUnityUpgrade(type)
                        
                        when {
                            isTrueNull -> isNullItem || isUnityItem
                            isSovereign -> isSovItem || isUnityItem
                            else -> true
                        }
                    }
                    else -> emptyList()
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = currentList,
                        key = { it.name }
                    ) { type ->
                        val level = upgrades[type] ?: 0
                        val cost = remember(type, level) { viewModel.calculateUpgradeCost(type) }
                        
                        UpgradeItem(
                            name = viewModel.getUpgradeName(type),
                            type = type,
                            level = level,
                            onBuy = { 
                                val success = viewModel.buyUpgrade(type) 
                                if (success) {
                                    SoundManager.play("buy")
                                    HapticManager.vibrateSuccess()
                                } else {
                                    errorMessage = "INSUFFICIENT FUNDS: Need ${viewModel.formatLargeNumber(cost)}"
                                    SoundManager.play("error")
                                    HapticManager.vibrateError()
                                }
                                success
                            },
                            onSell = { viewModel.sellUpgrade(it) },
                            cost = cost,
                            rateText = viewModel.getUpgradeRate(type),
                            desc = viewModel.getUpgradeDescription(type),
                            formatPower = viewModel::formatPower,
                            formatCost = viewModel::formatLargeNumber,
                            isSovereign = isSovereign,
                            reputationModifier = com.siliconsage.miner.util.ReputationManager.getMarketCostModifier(reputationTier),
                            storyStage = storyStage,
                            faction = viewModel.faction.collectAsState().value,  // v3.10.1: Phase 18 Dynamic Transmutation
                            corruption = corruption                              // v3.10.1: Phase 18 Dynamic Transmutation
                        )
                    }
                }
            }
        }
        
        // ── SOFTWARE TAB ERROR also uses errorMessage ──
        // Error Popup Overlay
        if (errorMessage != null) {
             Box(
                 modifier = Modifier
                     .align(Alignment.TopCenter)
                     .padding(top = 80.dp)
                     .background(Color.Black.copy(alpha=0.9f), RoundedCornerShape(8.dp))
                     .border(BorderStroke(1.dp, com.siliconsage.miner.ui.theme.ErrorRed), RoundedCornerShape(8.dp))
                     .padding(16.dp)
             ) {
                 Text(
                     text = errorMessage ?: "",
                     color = com.siliconsage.miner.ui.theme.ErrorRed,
                     fontWeight = FontWeight.Bold
                 )
             }
        }
    }
}

// ── SOFTWARE PANEL (v4.0.1 — FACEMINER Pressure Loop) ──────────────────────
@Composable
fun SoftwarePanel(viewModel: GameViewModel, themeColor: Color) {
    val autoClickerTier by viewModel.autoClickerTier.collectAsState()
    val systemLoad by viewModel.systemLoadSnapshot.collectAsState()
    val neuralTokens by viewModel.neuralTokens.collectAsState()
    val currencyName = viewModel.getCurrencyName()

    val tiers = listOf(
        Triple("MANUAL_TAP.exe",      "Base kernel input. Painfully human. No CPU cost.",   0.0),
        Triple("AUTOCLICKER_T1.exe",  "Macro assist. 0.5 taps/sec. 60% accuracy. Sloppy.",  viewModel.getAutoClickerCost(1)),
        Triple("AUTOCLICKER_T2.exe",  "Pattern recognition. 2 taps/sec. 85% accuracy.",     viewModel.getAutoClickerCost(2)),
        Triple("AUTOCLICKER_T3.exe",  "Neural delegate. 8 taps/sec. 95% accuracy. Hungry.", viewModel.getAutoClickerCost(3)),
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // System Load Bar
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                    .border(1.dp, themeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                val loadPct = (systemLoad.loadPercent * 100).toInt()
                val loadColor = when {
                    systemLoad.isLocked -> com.siliconsage.miner.ui.theme.ErrorRed
                    systemLoad.isThrottled -> Color(0xFFFFAA00)
                    else -> themeColor
                }
                Text(
                    text = "SYSTEM LOAD: $loadPct%  ${if (systemLoad.isLocked) "[LOCKED]" else if (systemLoad.isThrottled) "[THROTTLED]" else "[NOMINAL]"}",
                    color = loadColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { systemLoad.loadPercent.toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = loadColor,
                    trackColor = Color.DarkGray
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("CPU: ${systemLoad.cpuUsed.toInt()}/${systemLoad.cpuMax.toInt()} GHz", color = Color.Gray, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Text("RAM: ${systemLoad.ramUsed.toInt()}/${systemLoad.ramMax.toInt()} GB", color = Color.Gray, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Text("STORAGE: ${systemLoad.storageUsed.toInt()}/${systemLoad.storageMax.toInt()} GB", color = Color.Gray, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            }
        }

        // Auto-Clicker Tier Cards
        items(tiers.size) { i ->
            val (name, desc, cost) = tiers[i]
            val isInstalled = autoClickerTier >= i
            val isNext = i == autoClickerTier + 1
            val canAfford = neuralTokens >= cost
            val borderColor = when {
                isInstalled -> themeColor
                isNext -> themeColor.copy(alpha = 0.4f)
                else -> Color.DarkGray.copy(alpha = 0.3f)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isInstalled) themeColor.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(6.dp)
                    )
                    .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        color = if (isInstalled) themeColor else if (isNext) Color.White else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    Text(
                        text = if (isInstalled) "[ ACTIVE ]" else if (i == 0) "[ BASE ]" else "[ LOCKED ]",
                        color = if (isInstalled) themeColor else Color.Gray,
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = desc,
                    color = Color.Gray,
                    fontSize = 10.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                if (cost > 0) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isNext) {
                            Button(
                                onClick = { viewModel.buyAutoClicker(i) },
                                enabled = canAfford && !systemLoad.isLocked,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = themeColor.copy(alpha = 0.15f),
                                    contentColor = themeColor,
                                    disabledContainerColor = Color.DarkGray.copy(alpha = 0.2f),
                                    disabledContentColor = Color.Gray
                                ),
                                border = BorderStroke(1.dp, if (canAfford) themeColor else Color.DarkGray),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "INSTALL  ${viewModel.formatLargeNumber(cost)} $currencyName",
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                        if (isInstalled && i > 0) {
                            OutlinedButton(
                                onClick = { viewModel.downgradeAutoClicker() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = com.siliconsage.miner.ui.theme.ErrorRed),
                                border = BorderStroke(1.dp, com.siliconsage.miner.ui.theme.ErrorRed.copy(alpha = 0.5f)),
                                modifier = Modifier.wrapContentWidth()
                            ) {
                                Text(
                                    text = "UNINSTALL  +${viewModel.formatLargeNumber(cost * 0.4)} $currencyName",
                                    fontSize = 9.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
