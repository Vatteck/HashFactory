package com.siliconsage.miner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.siliconsage.miner.viewmodel.ResonanceTier
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
    val vanceStatus by viewModel.vanceStatus.collectAsState()
    
    val techNodesRaw by viewModel.techNodes.collectAsState()
    val techNodes = remember(techNodesRaw, vanceStatus) {
        techNodesRaw.filter { node ->
            node.requiresEnding == null || node.requiresEnding == vanceStatus
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
                if (storyStage >= 2 || faction != "NONE") {
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
                    
                    LegacyGrid(nodes = techNodes, unlockedIds = unlockedNodes, prestigePoints = prestigePoints, faction = faction, onUnlock = { id -> TechTreeManager.unlockNode(viewModel, id) }, themeColor = themeColor)
                }
            } else {
                item {
                    Text("GOD-TIER PERKS", color = ConvergenceGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                if (upgrades[UpgradeType.NEURAL_BRIDGE]?.let { it > 0 } == true) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth().border(1.dp, ConvergenceGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp)), colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("NEURAL BRIDGE", color = ConvergenceGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Exchange CD/VF specialized resources 1:1", color = Color.LightGray, fontSize = 10.sp)
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Button(onClick = { viewModel.executeBridgeTransfer(1.0) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) { Text("CD → VF", fontSize = 10.sp) }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(onClick = { viewModel.executeBridgeTransfer(-1.0) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(4.dp)) { Text("VF → CD", fontSize = 10.sp) }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
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
fun LegacyGrid(nodes: List<TechNode>, unlockedIds: List<String>, prestigePoints: Double, faction: String, onUnlock: (String) -> Unit, themeColor: Color) {
    val positions = calculateNodePositions(nodes, faction)
    Box(modifier = Modifier.fillMaxWidth().height(1800.dp).background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp)).border(1.dp, Color.DarkGray.copy(alpha=0.3f), RoundedCornerShape(8.dp))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
             nodes.forEach { node ->
                 val endPos = positions[node.id] ?: return@forEach
                 node.requires.forEach { parentId ->
                     val startPos = positions[parentId] ?: return@forEach
                     drawLine(color = if (unlockedIds.contains(node.id)) themeColor.copy(alpha = 0.6f) else Color.DarkGray.copy(alpha = 0.4f), start = Offset(size.width * startPos.x, size.height * startPos.y), end = Offset(size.width * endPos.x, size.height * endPos.y), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                 }
             }
        }
        androidx.compose.ui.layout.Layout(content = {
            nodes.forEach { node ->
                LegacyNodeButton(node = node, isUnlocked = unlockedIds.contains(node.id), isUnlockable = (node.requires.isEmpty() || node.requires.all { unlockedIds.contains(it) }), canAfford = prestigePoints >= node.cost, playerFaction = faction, onUnlock = { onUnlock(node.id) }, themeColor = themeColor)
            }
        }) { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeables.forEachIndexed { index, placeable ->
                    val node = nodes[index]
                    val pos = positions[node.id] ?: Offset(0.5f, 0.5f)
                    placeable.placeRelative(x = (constraints.maxWidth * pos.x - placeable.width / 2).toInt(), y = (constraints.maxHeight * pos.y - placeable.height / 2).toInt())
                }
            }
        }
    }
}

fun calculateNodePositions(nodes: List<TechNode>, faction: String): Map<String, Offset> {
    val positions = mutableMapOf<String, Offset>()
    fun getTier(node: TechNode, memo: MutableMap<String, Int> = mutableMapOf()): Int {
        if (node.id in memo) return memo[node.id]!!
        if (node.requires.isEmpty()) return 0.also { memo[node.id] = 0 }
        val tier = (node.requires.mapNotNull { pid -> nodes.find { it.id == pid }?.let { getTier(it, memo) } }.maxOrNull() ?: 0) + 1
        return tier.also { memo[node.id] = it }
    }
    val tierMap = nodes.groupBy { getTier(it) }
    val maxTier = tierMap.keys.maxOrNull() ?: 0
    tierMap.forEach { (tier, nodesInTier) ->
        val yPos = if (maxTier == 0) 0.5f else 0.05f + (tier.toFloat() / maxTier) * 0.9f
        val hiveNodes = nodesInTier.filter { it.description.contains("[HIVEMIND]") || it.description.contains("[NG+ NULL]") }
        val sancNodes = nodesInTier.filter { it.description.contains("[SANCTUARY]") || it.description.contains("[NG+ SOVEREIGN]") }
        val unityNodes = nodesInTier.filter { it.description.contains("[UNITY]") || it.description.contains("[NG+ UNITY]") }
        val generalNodes = nodesInTier.filter { !hiveNodes.contains(it) && !sancNodes.contains(it) && !unityNodes.contains(it) }
        
        // v3.2.3: Force-center the Sentience Core (The Root Node)
        val isRootTier = tier == 0
        
        nodesInTier.forEach { node ->
            val count = nodesInTier.size.coerceAtLeast(1)
            val index = nodesInTier.indexOf(node)
            
            // v3.2.4: Balanced centering for high-DPI outer screens
            val xPos = when {
                isRootTier || node.id == "sentience_core" -> 0.5f // Root and Sentience Core are ALWAYS centered
                hiveNodes.contains(node) -> {
                    val idx = hiveNodes.indexOf(node)
                    0.20f + (idx * 0.10f) // Pulled further in from edge
                }
                sancNodes.contains(node) -> {
                    val idx = sancNodes.indexOf(node)
                    0.80f - (idx * 0.10f) // Pulled further in from edge
                }
                unityNodes.contains(node) -> {
                    if (unityNodes.size == 1) 0.5f 
                    else 0.40f + (unityNodes.indexOf(node) * (0.2f / (unityNodes.size - 1).coerceAtLeast(1)))
                }
                else -> {
                    // General nodes (non-faction, non-root)
                    val generalIdx = generalNodes.indexOf(node)
                    if (generalNodes.size <= 1) 0.5f
                    else 0.35f + (generalIdx * (0.3f / (generalNodes.size - 1).coerceAtLeast(1)))
                }
            }
            positions[node.id] = Offset(xPos, yPos)
        }
    }
    return positions
}

@Composable
fun LegacyNodeButton(node: TechNode, isUnlocked: Boolean, isUnlockable: Boolean, canAfford: Boolean, playerFaction: String, onUnlock: () -> Unit, themeColor: Color) {
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(85.dp).background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp)).border(BorderStroke(1.dp, borderColor.copy(alpha = if (isUnlockable || isUnlocked) 1f else 0.3f)), RoundedCornerShape(8.dp)).clickable(enabled = isUnlockable && !isUnlocked && canAfford && !isOpposing) { onUnlock() }.padding(6.dp)) {
        Box(modifier = Modifier.size(18.dp).background(borderColor.copy(alpha = if (isUnlocked) 1f else 0.4f), CircleShape), contentAlignment = Alignment.Center) {
             if (isUnlocked) Text("✓", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Bold)
             else {
                 val icon = when (node.requiresEnding) { "NULL" -> "🌑"; "SOVEREIGN" -> "👑"; "UNITY" -> "⚛"; "BAD" -> "💀"; else -> "" }
                 if (icon.isNotEmpty()) Text(icon, fontSize = 10.sp)
             }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(node.name.replace(" ", "\n"), color = if (isUnlocked || isUnlockable) (if (isOpposing) Color.Gray.copy(alpha = 0.4f) else Color.White) else Color.Gray.copy(alpha = 0.5f), fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 9.sp)
        if (!isUnlocked) {
            Spacer(modifier = Modifier.height(1.dp))
            Text("${node.cost.toInt()} LP", color = if (isOpposing) Color.Gray.copy(alpha = 0.4f) else if (canAfford) NeonGreen else ErrorRed.copy(alpha = 0.7f), fontSize = 8.sp)
        }
    }
}
