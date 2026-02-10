package com.siliconsage.miner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.ui.components.UpgradeItem
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun UpgradesScreen(viewModel: GameViewModel) {
    val upgrades by viewModel.upgrades.collectAsState()
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { com.siliconsage.miner.ui.theme.NeonGreen }
    val isSovereign by viewModel.isSovereign.collectAsState()
    val nullActive by viewModel.nullActive.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val techNodes by viewModel.techNodes.collectAsState()
    val unlockedTechNodes by viewModel.unlockedTechNodes.collectAsState()
    val prestigePoints by viewModel.prestigePoints.collectAsState()

    // UI State for Errors
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    fun canAffordInsight(current: Double, cost: Double) = current >= cost

    // Auto-dismiss Error
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            delay(2000)
            errorMessage = null
        }
    }
    
    val tabs = remember(nullActive, isTrueNull, isSovereign) {
        when {
            isTrueNull -> listOf("SUBSTRATE", "ENTROPY", "VOID", "GAPS", "RESEARCH")
            isSovereign -> listOf("FOUNDATION", "STABILITY", "STAKE", "WALLS", "RESEARCH")
            nullActive -> listOf("HARDWARE", "COOLING", "POWER", "SECURITY", "RESEARCH")
            else -> listOf("HARDWARE", "COOLING", "POWER", "SECURITY", "RESEARCH")
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
            // Balance Display isolated in HeaderSection
            HeaderSection(
                viewModel = viewModel,
                color = themeColor,
                onToggleOverclock = { viewModel.toggleOverclock() },
                onPurge = { viewModel.purgeHeat() },
                onRepair = { viewModel.repairIntegrity() },
                modifier = Modifier.padding(16.dp)
            )

            // Tab Row
            androidx.compose.material3.TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Black.copy(alpha = 0.75f), // Glass
                contentColor = themeColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
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
                        },
                        text = { 
                            Text(
                                title, 
                                fontSize = 10.sp, 
                                letterSpacing = (-0.5).sp,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
                                color = if (selectedTab == index) themeColor else Color.Gray
                            ) 
                        }
                    )
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
                        // Infrastructure
                        UpgradeType.RESIDENTIAL_TAP, UpgradeType.INDUSTRIAL_FEED, UpgradeType.SUBSTATION_LEASE, UpgradeType.NUCLEAR_CORE,
                        // Generators
                        UpgradeType.SOLAR_PANEL, UpgradeType.WIND_TURBINE, UpgradeType.DIESEL_GENERATOR,
                        UpgradeType.GEOTHERMAL_BORE, UpgradeType.NUCLEAR_REACTOR, UpgradeType.FUSION_CELL,
                        UpgradeType.ORBITAL_COLLECTOR, UpgradeType.DYSON_LINK,
                        // Efficiency
                        UpgradeType.GOLD_PSU, UpgradeType.SUPERCONDUCTOR, UpgradeType.AI_LOAD_BALANCER
                    )
                    3 -> listOf(
                        UpgradeType.BASIC_FIREWALL, UpgradeType.IPS_SYSTEM, UpgradeType.AI_SENTINEL,
                        UpgradeType.QUANTUM_ENCRYPTION, UpgradeType.OFFGRID_BACKUP
                    )
                    4 -> {
                        // RESEARCH TAB: Technical Upgrades
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(techNodes) { node ->
                                val isUnlocked = unlockedTechNodes.contains(node.id)
                                val (canUnlock, error) = com.siliconsage.miner.util.TechTreeManager.canUnlockNode(node, prestigePoints, unlockedTechNodes)
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, if (isUnlocked) themeColor else Color.DarkGray, RoundedCornerShape(4.dp))
                                        .clickable(enabled = !isUnlocked && canUnlock) { viewModel.unlockTechNode(node.id) },
                                    colors = CardDefaults.cardColors(containerColor = if (isUnlocked) themeColor.copy(alpha = 0.05f) else Color.Black)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(node.name, color = if (isUnlocked) themeColor else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                            Text(node.description, color = Color.Gray, fontSize = 10.sp)
                                        }
                                        if (isUnlocked) {
                                            Text("RESEARCHED", color = themeColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                        } else {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("${node.cost.toInt()} Insight", color = if (canAffordInsight(prestigePoints, node.cost)) Color.White else Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                if (error != null && !canAffordInsight(prestigePoints, node.cost)) {
                                                    Text(error, color = Color.Red.copy(alpha = 0.5f), fontSize = 8.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return@Box // Early return for special tab
                    }
                    else -> emptyList()
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = currentList,
                        key = { it.name } // Stable key
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
