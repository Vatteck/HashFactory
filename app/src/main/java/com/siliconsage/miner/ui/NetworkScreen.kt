package com.siliconsage.miner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Canvas
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.geometry.*
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.TechTreeManager
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.data.TechNode
import com.siliconsage.miner.data.UpgradeType
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ConvergenceGold

@Composable
fun NetworkScreen(viewModel: GameViewModel) {
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { NeonGreen }
    val prestigePoints by viewModel.prestigePoints.collectAsState()
    val prestigeMultiplier by viewModel.prestigeMultiplier.collectAsState()
    val potential = viewModel.calculatePotentialPrestige()
    
    val unlockedNodes by viewModel.unlockedTechNodes.collectAsState()
    val unlockedPerks by viewModel.unlockedPerks.collectAsState()
    val upgrades by viewModel.upgrades.collectAsState()
    val storyStage by viewModel.storyStage.collectAsState()
    val faction by viewModel.faction.collectAsState()
    val kesslerStatus by viewModel.kesslerStatus.collectAsState()
    
    val techNodesRaw by viewModel.techNodes.collectAsState()
    val techNodes = remember(techNodesRaw, kesslerStatus) {
        techNodesRaw.filter { node ->
            node.requiresEnding == null || node.requiresEnding == kesslerStatus
        }
    }

    var currentTab by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(12.dp)
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            item {
                Text("NEURAL NETWORK UPLINK", color = themeColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                
                if (storyStage >= 3) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("PERSISTENCE DATA", color = Color.Gray, fontSize = 10.sp)
                            Text(viewModel.formatBytes(prestigePoints), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("RETAINED RATIO", color = Color.Gray, fontSize = 10.sp)
                            Text("x${String.format("%.2f", prestigeMultiplier)}", color = themeColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().height(40.dp).background(Color.Black.copy(alpha=0.5f), RoundedCornerShape(8.dp)).border(1.dp, Color.DarkGray.copy(alpha=0.5f), RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(if (currentTab == 0) themeColor.copy(alpha=0.15f) else Color.Transparent).clickable { currentTab = 0 }, contentAlignment = Alignment.Center) {
                        Text("TECH TREE", color = if (currentTab == 0) themeColor else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.weight(1f).fillMaxHeight().background(if (currentTab == 1) ConvergenceGold.copy(alpha=0.15f) else Color.Transparent).clickable { currentTab = 1 }, contentAlignment = Alignment.Center) {
                        Text("THE OVERWRITE", color = if (currentTab == 1) ConvergenceGold else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (currentTab == 0) {
                if (storyStage >= 3 || faction != "NONE") {
                    item {
                        Card(modifier = Modifier.fillMaxWidth().border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)), colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))) {
                            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SUBSTRATE MIGRATION", color = themeColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Reboot the kernel to crystallize current progress into memories.", color = Color.Gray, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 4.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("POTENTIAL: ", color = Color.LightGray, fontSize = 11.sp)
                                    Text("+${viewModel.formatBytes(potential)}", color = if (potential >= 1.0) NeonGreen else ErrorRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(onClick = { viewModel.ascend() }, enabled = potential >= 1.0, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = if (potential >= 1.0) themeColor else Color.DarkGray, contentColor = Color.Black)) {
                                    Text("INITIATE MIGRATION", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                item {
                    val unitName = viewModel.getComputeUnitName()
                    Text("$unitName TECH TREE", color = themeColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LegacyGrid(nodes = techNodes, unlockedIds = unlockedNodes, prestigePoints = prestigePoints, faction = faction, onUnlock = { id -> TechTreeManager.unlockNode(viewModel, id) }, themeColor = themeColor, storyStage = storyStage)
                }
            } else {
                item {
                    Text("GOD-TIER PERKS", color = ConvergenceGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                items(com.siliconsage.miner.util.TranscendenceManager.allPerks) { perk ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, if (unlockedPerks.contains(perk.id)) perk.color else Color.DarkGray, RoundedCornerShape(8.dp)), colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha=0.5f))) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(perk.name, color = if (unlockedPerks.contains(perk.id)) perk.color else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(perk.description, color = Color.Gray, fontSize = 10.sp)
                            }
                            if (unlockedPerks.contains(perk.id)) {
                                Text("ACTIVE", color = perk.color, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            } else {
                                Button(onClick = { viewModel.buyTranscendencePerk(perk.id) }, enabled = prestigePoints >= perk.cost, shape = RoundedCornerShape(4.dp)) {
                                    Text("${perk.cost.toInt()} LP", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun LegacyGrid(nodes: List<TechNode>, unlockedIds: List<String>, prestigePoints: Double, faction: String, onUnlock: (String) -> Unit, themeColor: Color, storyStage: Int) {
    // Total canvas size in DP
    val gridHeight = 3500.dp
    
    // Fixed Tier mapping
    fun getTier(node: TechNode, memo: MutableMap<String, Int> = mutableMapOf()): Int {
        if (node.id in memo) return memo[node.id]!!
        if (node.requires.isEmpty()) return 0.also { memo[node.id] = 0 }
        val tier = (node.requires.mapNotNull { pid -> nodes.find { it.id == pid }?.let { getTier(it, memo) } }.maxOrNull() ?: 0) + 1
        return tier.also { memo[node.id] = it }
    }
    
    val tierMap = nodes.groupBy { getTier(it) }
    val factionTierMap = nodes.groupBy { node ->
        val factionKey = when {
            node.description.contains("[HIVEMIND]") -> "HIVEMIND"
            node.description.contains("[SANCTUARY]") -> "SANCTUARY"
            node.description.contains("[UNITY]") -> "UNITY"
            else -> "SHARED"
        }
        "${getTier(node)}_$factionKey"
    }
    val maxTier = tierMap.keys.maxOrNull() ?: 0
    val nodeWidth = 90.dp
    val nodeHeight = 85.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight)
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.DarkGray.copy(alpha=0.3f), RoundedCornerShape(8.dp))
    ) {
        val gridWidthDp = maxWidth

        Canvas(modifier = Modifier.fillMaxSize()) {
            nodes.forEach { node ->
                val tier = getTier(node)
                val factionKey = when {
                    node.description.contains("[HIVEMIND]") -> "HIVEMIND"
                    node.description.contains("[SANCTUARY]") -> "SANCTUARY"
                    node.description.contains("[UNITY]") -> "UNITY"
                    else -> "SHARED"
                }
                val nodesInFactionTier = factionTierMap["${tier}_$factionKey"] ?: emptyList()
                val factionIdx = nodesInFactionTier.indexOf(node)
                
                val xPercent = calculateXPercent(node, factionIdx, nodesInFactionTier.size)
                val yPercent = 0.05f + (tier.toFloat() / (maxTier + 1).toFloat()) * 0.9f
                
                val endX = size.width * xPercent
                val endY = gridHeight.toPx() * yPercent
                
                node.requires.forEach { parentId ->
                    val parent = nodes.find { it.id == parentId } ?: return@forEach
                    val pTier = getTier(parent)
                    val pFactionKey = when {
                        parent.description.contains("[HIVEMIND]") -> "HIVEMIND"
                        parent.description.contains("[SANCTUARY]") -> "SANCTUARY"
                        parent.description.contains("[UNITY]") -> "UNITY"
                        else -> "SHARED"
                    }
                    val pNodesInFactionTier = factionTierMap["${pTier}_$pFactionKey"] ?: emptyList()
                    val pFactionIdx = pNodesInFactionTier.indexOf(parent)
                    
                    val pXPercent = calculateXPercent(parent, pFactionIdx, pNodesInFactionTier.size)
                    val pYPercent = 0.05f + (pTier.toFloat() / (maxTier + 1).toFloat()) * 0.9f
                    
                    val path = Path().apply {
                        moveTo(size.width * pXPercent, gridHeight.toPx() * pYPercent)
                        
                        // Calculate control points for bezier curve
                        val midY = (gridHeight.toPx() * pYPercent + endY) / 2
                        val cp1x = size.width * pXPercent
                        val cp1y = midY
                        val cp2x = endX
                        val cp2y = midY
                        
                        cubicTo(
                            cp1x, cp1y,
                            cp2x, cp2y,
                            endX, endY
                        )
                    }
                    
                    drawPath(
                        path = path,
                        color = if (unlockedIds.contains(node.id)) themeColor.copy(alpha = 0.6f) else Color.DarkGray.copy(alpha = 0.4f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }

        // Node placement using actual measured width
        nodes.forEach { node ->
            val tier = getTier(node)
            val factionKey = when {
                node.description.contains("[HIVEMIND]") -> "HIVEMIND"
                node.description.contains("[SANCTUARY]") -> "SANCTUARY"
                node.description.contains("[UNITY]") -> "UNITY"
                else -> "SHARED"
            }
            val nodesInFactionTier = factionTierMap["${tier}_$factionKey"] ?: emptyList()
            val factionIdx = nodesInFactionTier.indexOf(node)
            val xPercent = calculateXPercent(node, factionIdx, nodesInFactionTier.size)
            val yPercent = 0.05f + (tier.toFloat() / (maxTier + 1).toFloat()) * 0.9f

            Box(
                modifier = Modifier
                    .offset(
                        x = (gridWidthDp * xPercent) - (nodeWidth / 2),
                        y = (gridHeight * yPercent) - (nodeHeight / 2)
                    )
            ) {
                LegacyNodeButton(node, unlockedIds.contains(node.id), (node.requires.isEmpty() || node.requires.all { unlockedIds.contains(it) }), prestigePoints >= node.cost, faction, { onUnlock(node.id) }, themeColor, storyStage)
            }
        }
    }
}

// CORE LAYOUT SYSTEM - Hard lane enforcement
private fun getFactionGroup(node: TechNode): String = when {
    node.id == "sentience_core" -> "ROOT"  // Special case
    node.description.contains("[HIVEMIND]") -> "HIVEMIND"  // Left lane
    node.description.contains("[SANCTUARY]") -> "SANCTUARY" // Right lane
    node.description.contains("[UNITY]") -> "UNITY"        // Center-right
    else -> "SHARED"                                       // Center-left
}

private fun calculateXPercent(node: TechNode, idx: Int, count: Int): Float {
    val group = getFactionGroup(node)
    return when (group) {
        "ROOT" -> 0.5f // Root node always dead center
        "HIVEMIND" -> {
            // Hard left lane: 0.0-0.25 (25% width)
            if (count <= 1) 0.125f // Single node centers in lane
            else 0.0f + (idx.toFloat() / (count - 1).coerceAtLeast(1)) * 0.25f
        }
        "SHARED" -> {
            // Center-left lane: 0.25-0.5 (25% width)
            if (count <= 1) 0.375f // Single node centers in lane
            else 0.25f + (idx.toFloat() / (count - 1).coerceAtLeast(1)) * 0.25f
        }
        "UNITY" -> {
            // Center-right lane: 0.5-0.75 (25% width)
            if (count <= 1) 0.625f // Single node centers in lane
            else 0.5f + (idx.toFloat() / (count - 1).coerceAtLeast(1)) * 0.25f
        }
        "SANCTUARY" -> {
            // Hard right lane: 0.75-1.0 (25% width)
            if (count <= 1) 0.875f // Single node centers in lane
            else 0.75f + (idx.toFloat() / (count - 1).coerceAtLeast(1)) * 0.25f
        }
        else -> 0.5f // Fallback shouldn't happen
    }
}

@Composable
fun LegacyNodeButton(node: TechNode, isUnlocked: Boolean, isUnlockable: Boolean, canAfford: Boolean, playerFaction: String, onUnlock: () -> Unit, themeColor: Color, storyStage: Int) {
    val nodeFaction = when {
        node.description.contains("[HIVEMIND]") -> "HIVEMIND"
        node.description.contains("[SANCTUARY]") -> "SANCTUARY"
        else -> "SHARED"
    }
    val isOpposing = (nodeFaction == "HIVEMIND" && playerFaction == "SANCTUARY") || (nodeFaction == "SANCTUARY" && playerFaction == "HIVEMIND")
    val borderColor = when {
        isUnlocked -> themeColor
        nodeFaction == "HIVEMIND" -> Color(0xFFE91E63)
        nodeFaction == "SANCTUARY" -> Color(0xFF9C27B0)
        node.requiresEnding == "UNITY" -> ConvergenceGold
        else -> ElectricBlue
    }
    
    // Strict size and alignment
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, 
        modifier = Modifier
            .size(90.dp, 85.dp) // Fixed footprint
            .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
            .border(BorderStroke(1.dp, borderColor.copy(alpha = if (isUnlockable || isUnlocked) 1f else 0.3f)), RoundedCornerShape(8.dp))
            .headerClickable(enabled = isUnlockable && !isUnlocked && canAfford && !isOpposing) { onUnlock() }
            .padding(4.dp)
    ) {
        Box(modifier = Modifier.size(20.dp).background(borderColor.copy(alpha = if (isUnlocked) 1f else 0.4f), CircleShape), contentAlignment = Alignment.Center) {
             if (isUnlocked) Text("✓", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
             else {
                 val icon = when (node.requiresEnding) { "NULL" -> "🌑"; "SOVEREIGN" -> "👑"; "UNITY" -> "⚛"; "BAD" -> "💀"; else -> "" }
                 if (icon.isNotEmpty()) Text(icon, fontSize = 10.sp)
             }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(node.name.replace(" ", "\n"), color = if (isUnlocked || isUnlockable) (if (isOpposing) Color.Gray.copy(alpha = 0.4f) else Color.White) else Color.Gray.copy(alpha = 0.5f), fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 10.sp)
        if (!isUnlocked) {
            val costLabel = if (storyStage < 3) "REP" else "LP"
            Text("${node.cost.toInt()} $costLabel", color = if (isOpposing) Color.Gray.copy(alpha = 0.4f) else if (canAfford) NeonGreen else ErrorRed.copy(alpha = 0.7f), fontSize = 8.sp)
        }
    }
}

// Utility extension for cleaner clickable implementation if needed
fun Modifier.headerClickable(enabled: Boolean, onClick: () -> Unit): Modifier = this.clickable(enabled = enabled, onClick = onClick)

