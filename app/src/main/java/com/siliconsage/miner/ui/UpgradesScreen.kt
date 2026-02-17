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
            isTrueNull -> listOf("SUBSTRATE", "ENTROPY", "VOID", "GAPS", "NULL")
            isSovereign -> listOf("FOUNDATION", "STABILITY", "STAKE", "WALLS", "SOVEREIGN")
            storyStage >= 3 || nullActive -> {
                val base = listOf("HARDWARE", "COOLING", "POWER", "SECURITY")
                if (nullActive) base + "GHOSTS" else base + "RESEARCH"
            }
            else -> listOf("HARDWARE", "COOLING", "POWER", "SECURITY")
        }
    }
    
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
                        UpgradeType.DIMENSIONAL_VENT
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
                            isSovereign = isSovereign
                        )
                    }
                }
            }
        }
        
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
