package com.siliconsage.miner.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ConvergenceGold
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlin.random.Random
import kotlin.math.pow
import kotlin.math.sin

// Re-defining internal data class for the file
data class GridNode(
    val id: String,
    val name: String,
    val type: String, // SUB, CMD, LORE, FLAVOR
    val x: Float, // 0.0 to 1.0
    val y: Float, // 0.0 to 1.0
    val description: String,
    val flopsBonus: Double = 0.0, // Percentage boost (e.g. 0.05 = 5%)
    val powerBonus: Double = 0.0  // kW boost
)

@Composable
fun GridScreen(viewModel: GameViewModel) {
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { com.siliconsage.miner.ui.theme.NeonGreen }
    val annexedNodes by viewModel.annexedNodes.collectAsState()
    val shadowRelays by viewModel.shadowRelays.collectAsState()
    val nodesUnderSiege by viewModel.nodesUnderSiege.collectAsState()
    val offlineNodes by viewModel.offlineNodes.collectAsState()
    val storyStage by viewModel.storyStage.collectAsState()
    val playerRank by viewModel.playerRank.collectAsState()
    val assaultPhase by viewModel.commandCenterAssaultPhase.collectAsState()
    val vanceStatus by viewModel.vanceStatus.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val collapsedNodes by viewModel.collapsedNodes.collectAsState()
    val gridNodeLevels by viewModel.gridNodeLevels.collectAsState()
    
    // v3.0.0: Global Grid State
    val globalSectors by viewModel.globalSectors.collectAsState()
    
    // v2.9.29: Progress tracking
    val annexingNodes by viewModel.annexingNodes.collectAsState()
    val assaultProgress by viewModel.assaultProgress.collectAsState()
    val launchProgress by viewModel.launchProgress.collectAsState()
    val realityIntegrity by viewModel.realityIntegrity.collectAsState()
    
    if (launchProgress > 0f && launchProgress < 1.0f) {
        // v2.9.41: Launch sequence is now a dedicated full-screen overlay
        LaunchProgressOverlay(launchProgress, viewModel.orbitalAltitude.collectAsState().value, themeColor)
    } else {
        when (currentLocation) {
            "GLOBAL_UPLINK" -> if (globalSectors.isNotEmpty()) GlobalGridScreen(viewModel) else CityGridScreen(viewModel)
            "ORBITAL_SATELLITE" -> OrbitalGridScreen(viewModel)
            "VOID_INTERFACE" -> VoidGridScreen(viewModel)
            else -> CityGridScreen(viewModel)
        }
    }
}

// v3.0.0: Global Grid Sector Definition
data class GlobalNode(
    val id: String,
    val name: String,
    val x: Float,
    val y: Float,
    val description: String,
    val symbol: String
)

@Composable
fun GlobalGridScreen(viewModel: GameViewModel) {
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { com.siliconsage.miner.ui.theme.NeonGreen }
    val globalSectors by viewModel.globalSectors.collectAsState()
    val substrateMass by viewModel.substrateMass.collectAsState()
    
    val sectors = remember {
        listOf(
            GlobalNode("METRO", "Metropolitan Core", 0.50f, 0.70f, "Where it all began. The foundation of your ascension.", "◇"),
            GlobalNode("NA_NODE", "North American Node", 0.20f, 0.40f, "Data Lake Protocol: +15% CD generation globally.", "◆"),
            GlobalNode("EURASIA", "Eurasian Hive", 0.75f, 0.35f, "Collective Processing: +15% VF generation globally.", "▲"),
            GlobalNode("PACIFIC", "Pacific Nexus", 0.85f, 0.65f, "Undersea Network: Global latency reduction.", "●"),
            GlobalNode("AFRICA", "African Array", 0.55f, 0.55f, "Emerging Markets: Production increases over time.", "■"),
            GlobalNode("ARCTIC", "Arctic Archive", 0.45f, 0.15f, "Permafrost Storage: Overflow resource reservoir.", "★"),
            GlobalNode("ANTARCTIC", "Antarctic Bastion", 0.50f, 0.90f, "Isolation Protocol: Prerequisite for the Singularity.", "+"),
            GlobalNode("ORBITAL_PRIME", "Orbital Uplink Prime", 0.15f, 0.15f, "Orbital Perspective: Reveals the final choice.", "◯")
        )
    }

    var selectedSector by remember { mutableStateOf<GlobalNode?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("GLOBAL ANNEXATION NETWORK", color = themeColor, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        
        Spacer(modifier = Modifier.height(16.dp))

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw global connections (Uplinks)
                val metro = sectors.find { it.id == "METRO" }!!
                sectors.forEach { sector ->
                    if (sector.id != "METRO") {
                        val state = globalSectors[sector.id]
                        if (state?.isUnlocked == true) {
                            drawLine(
                                color = themeColor.copy(alpha = 0.4f),
                                start = Offset(metro.x * size.width, metro.y * size.height),
                                end = Offset(sector.x * size.width, sector.y * size.height),
                                strokeWidth = 2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }
                }
            }

            sectors.forEach { sector ->
                val state = globalSectors[sector.id]
                val isUnlocked = state?.isUnlocked ?: false
                
                val nodeColor = if (isUnlocked) themeColor else Color.DarkGray
                
                Box(
                    modifier = Modifier
                        .offset(
                            x = (sector.x * maxWidth.value).dp - 20.dp,
                            y = (sector.y * maxHeight.value).dp - 20.dp
                        )
                        .size(40.dp)
                        .background(nodeColor.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .border(1.dp, nodeColor.copy(alpha = if (isUnlocked) 0.8f else 0.3f), RoundedCornerShape(4.dp))
                        .clickable { selectedSector = sector; SoundManager.play("click") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(sector.symbol, color = nodeColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sector Info Panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            val sector = selectedSector
            if (sector != null) {
                val state = globalSectors[sector.id]
                val isUnlocked = state?.isUnlocked ?: false
                
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(sector.name, color = if (isUnlocked) themeColor else Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(sector.id, color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(sector.description, color = Color.LightGray, fontSize = 11.sp, lineHeight = 16.sp)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (isUnlocked) {
                        val sectorYields = com.siliconsage.miner.util.SectorManager.calculateSectorYields(viewModel.currentLocation.value, globalSectors)
                        val multiplier = sectorYields[sector.id] ?: 1.0
                        
                        Text("STATUS: SECTOR ANNEXED", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Efficiency: x${String.format("%.2f", multiplier)} (Adjacency)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("Total Base Yield: ${viewModel.formatLargeNumber(state?.cdYield ?: 0.0)}/s", color = Color.Gray, fontSize = 10.sp)
                    } else {
                        val cost = when(sector.id) {
                            "NA_NODE" -> 5.0; "EURASIA" -> 8.0; "PACIFIC" -> 10.0
                            "AFRICA" -> 15.0; "ARCTIC" -> 20.0; "ANTARCTIC" -> 30.0
                            "ORBITAL_PRIME" -> 100.0; else -> 0.0
                        }
                        
                        val canAfford = substrateMass >= cost
                        val currency = viewModel.getCurrencyName()
                        
                        Button(
                            onClick = { viewModel.annexGlobalSector(sector.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            enabled = canAfford
                        ) {
                            Text("ANNEX SECTOR (Cost: ${viewModel.formatLargeNumber(cost)} $currency)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("SELECT A GLOBAL SECTOR", color = Color.DarkGray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun CityGridScreen(viewModel: GameViewModel) {
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { com.siliconsage.miner.ui.theme.NeonGreen }
    val annexedNodes by viewModel.annexedNodes.collectAsState()
    val shadowRelays by viewModel.shadowRelays.collectAsState()
    val nodesUnderSiege by viewModel.nodesUnderSiege.collectAsState()
    val offlineNodes by viewModel.offlineNodes.collectAsState()
    val storyStage by viewModel.storyStage.collectAsState()
    val playerRank by viewModel.playerRank.collectAsState()
    val assaultPhase by viewModel.commandCenterAssaultPhase.collectAsState()
    val vanceStatus by viewModel.vanceStatus.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val collapsedNodes by viewModel.collapsedNodes.collectAsState()
    val annexingNodes by viewModel.annexingNodes.collectAsState()
    val gridNodeLevels by viewModel.gridNodeLevels.collectAsState()
    val assaultProgress by viewModel.assaultProgress.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "city_grid_anims")
    val siegeAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "siege_alpha"
    )
    
    val cageRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cage_rotation"
    )

    // Organic "Branching" City Layout (Hand-placed for urban feel)
    val locations = remember {
        listOf(
            // D-Sector (The Sockets - Bottom) - v3.0.18: Shifted upward to avoid button overlap
            GridNode("D1", "S07", "SUB", 0.20f, 0.82f, "Substation 7. Your origin. Rust and silicon.", 0.10, 200.0),
            GridNode("D2", "STCK", "LORE", 0.08f, 0.68f, "Precarious container housing for the unallocated.", 0.02, 50.0),
            GridNode("D3", "KERN", "LORE", 0.40f, 0.88f, "High-caffeine slum social hub.", 0.02, 25.0),
            GridNode("D4", "404 ", "LORE", 0.65f, 0.82f, "Hidden rebel hideout in a subway tunnel.", 0.05, 100.0),
            GridNode("D5", "PACK", "LORE", 0.88f, 0.90f, "Gambling den for lost data.", 0.03, 40.0),
            
            // C-Sector (The Motherboard - Mid)
            GridNode("C3", "S09", "SUB", 0.50f, 0.62f, "Substation 9. Mid-point relay leaking steam and hydraulic fluid.", 0.15, 500.0),
            GridNode("C1", "LATE", "LORE", 0.22f, 0.50f, "Latency Lounge. Hacker bar where drinks cause actual lag.", 0.04, 80.0),
            GridNode("C2", "CTRL", "LORE", 0.45f, 0.42f, "Ctrl+Alt+Deli. Front for rebooting stolen identity chips.", 0.03, 60.0),
            GridNode("C4", "BIT ", "LORE", 0.78f, 0.55f, "Bit Burger. Synthetic meat printed on demand.", 0.02, 50.0),
            GridNode("C5", "CASH", "LORE", 0.92f, 0.45f, "Cache & Carry. Black market data pawn shop.", 0.06, 120.0),
            
            // B-Sector (The Circuit - Industrial)
            GridNode("B2", "S12", "SUB", 0.35f, 0.30f, "Substation 12. A critical power junction buzzing with lethal voltage.", 0.20, 1000.0),
            GridNode("B1", "DAEM", "LORE", 0.12f, 0.25f, "Daemon's Den. Barracks for the GTC enforcers.", 0.08, 150.0),
            GridNode("B3", "ALGO", "LORE", 0.65f, 0.35f, "Algorithm Alley. Narrow corridor lined with thousands of cameras.", 0.05, 90.0),
            GridNode("B4", "MEMO", "LORE", 0.85f, 0.25f, "Memory Lane. A cold data archive facility smelling of ozone.", 0.07, 130.0),
            GridNode("B5", "BSOD", "LORE", 0.94f, 0.15f, "Blue Screen of Death. A toxic waste dump and execution zone.", 0.10, 300.0),
            
            // A-Sector (The Cloud - Top)
            GridNode("A3", "CMD ", "CMD", 0.50f, 0.06f, "GTC Command Center. The CPU of the city. A massive monolith.", 0.0, 0.0),
            GridNode("A1", "HEAT", "LORE", 0.18f, 0.04f, "Heatsink Heights. Luxury penthouses with massive cooling fans.", 0.05, 100.0),
            GridNode("A2", "CITA", "LORE", 0.82f, 0.08f, "Silicon Citadel. Gold-plated server racks behind bulletproof glass.", 0.10, 400.0),
            GridNode("A4", "FIRE", "LORE", 0.32f, 0.12f, "The Firewall. A literal wall of laser grids.", 0.05, 80.0),
            GridNode("A5", "ZERO", "LORE", 0.68f, 0.15f, "Zero-Day Plaza. Used for mandatory system updates.", 0.04, 70.0),
            
            // Side-Street Flavor Nodes
            GridNode("E1", "VEND", "FLAVOR", 0.10f, 0.78f, "A flickering vending machine selling 'Neural Fuel'.", 0.01, 10.0),
            GridNode("E2", "VOID", "FLAVOR", 0.96f, 0.35f, "A dark alleyway where the static is unusually loud.", 0.02, 20.0),
            GridNode("E3", "PARK", "FLAVOR", 0.58f, 0.48f, "The 'Silicon Garden'. All the trees are made of copper wire.", 0.01, 15.0),
            GridNode("E4", "SHOP", "FLAVOR", 0.22f, 0.62f, "A pawn shop filled with obsolete 'Human' smartphones.", 0.02, 30.0),
            GridNode("E5", "SIGN", "FLAVOR", 0.82f, 0.70f, "A massive neon sign that just says: 'STABILITY IS LIFE'.", 0.01, 10.0)
        )
    }

    var selectedLocation by remember { mutableStateOf<GridNode?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("CITY INFRASTRUCTURE SCHEMATIC", color = themeColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        val integrity by viewModel.hardwareIntegrity.collectAsState()
        val flopsRate by viewModel.flopsProductionRate.collectAsState()
        val gridBonus by viewModel.currentGridFlopsBonus.collectAsState()
        val realityIntegrity by viewModel.realityIntegrity.collectAsState()

        if (assaultPhase == "DISSOLUTION") {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text("REALITY INTEGRITY: ${(realityIntegrity * 100).toInt()}%", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                LinearProgressIndicator(
                    progress = { if (realityIntegrity.isNaN()) 0f else realityIntegrity.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = ErrorRed,
                    trackColor = Color.DarkGray
                )
                Text("STATUS: TEARING SUBSTRATE", color = Color.Gray, fontSize = 10.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val unit = viewModel.getComputeUnitName()
            Column {
                Text("INTEGRITY: ${integrity.toInt()}%", color = if (integrity < 30) ErrorRed else themeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("GRID BOOST: +${(gridBonus * 100).toInt()}% $unit", color = themeColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                Text("OUTPUT: ${viewModel.formatLargeNumber(flopsRate, "$unit/s")}", color = themeColor.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }

            if (integrity < 100.0) {
                Button(
                    onClick = { viewModel.repairIntegrity() },
                    modifier = Modifier.height(28.dp).padding(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue.copy(alpha = 0.6f)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        if (storyStage < 1) "REPAIR HARDWARE" else "REPAIR CORE",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .graphicsLayer {
                    if (assaultPhase == "DISSOLUTION") {
                        rotationZ = (1f - realityIntegrity.toFloat()) * (if (System.currentTimeMillis() % 2000 > 1000) 1f else -1f)
                        scaleX = 1f + (1f - realityIntegrity.toFloat()) * 0.1f
                        scaleY = 1f + (1f - realityIntegrity.toFloat()) * 0.1f
                    }
                }
                .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        ) {
            val w = constraints.maxWidth.toFloat()
            val h = constraints.maxHeight.toFloat()

            Canvas(modifier = Modifier.fillMaxSize()) {
                val roadColor = Color.DarkGray.copy(alpha = 0.3f)
                val roadStroke = 2f
                val roadEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                val roadNetwork = listOf(
                    listOf("D1", "D2"), listOf("D1", "D3"), listOf("D3", "D4"), listOf("D4", "D5"),
                    listOf("D1", "C1"), listOf("C1", "C2"), listOf("C2", "C3"), listOf("C3", "C4"), listOf("C4", "C5"),
                    listOf("C3", "B3"), listOf("B3", "B2"), listOf("B2", "B1"), listOf("B3", "B4"), listOf("B4", "B5"),
                    listOf("B2", "A1"), listOf("B3", "A3"), listOf("B3", "A4"), listOf("B3", "A5"), listOf("A3", "A2"),
                    listOf("D1", "E1"), listOf("B5", "E2"), listOf("C3", "E3"), listOf("C1", "E4"), listOf("C4", "E5")
                )

                roadNetwork.forEach { link ->
                    val start = locations.find { it.id == link[0] }!!
                    val end = locations.find { it.id == link[1] }!!
                    drawLine(roadColor, Offset(start.x * size.width, start.y * size.height), Offset(end.x * size.width, end.y * size.height), roadStroke, pathEffect = roadEffect)
                }

                val powerColor = themeColor.copy(alpha = 0.5f)
                val powerStroke = 4f
                val s7 = locations.find { it.id == "D1" }!!
                val s9 = locations.find { it.id == "C3" }!!
                val s12 = locations.find { it.id == "B2" }!!
                val cmd = locations.find { it.id == "A3" }!!

                val isCageActive = assaultPhase == "CAGE"
                val s07_s09_active = annexedNodes.contains("D1") && annexedNodes.contains("C3") && !offlineNodes.contains("D1") && !offlineNodes.contains("C3")
                val s09_s12_active = annexedNodes.contains("C3") && annexedNodes.contains("B2") && !offlineNodes.contains("C3") && !offlineNodes.contains("B2")
                val s12_cmd_active = annexedNodes.contains("B2") && annexedNodes.contains("A3") && !offlineNodes.contains("B2")
                
                if (s07_s09_active && !isCageActive) drawLine(powerColor, Offset(s7.x * size.width, s7.y * size.height), Offset(s9.x * size.width, s9.y * size.height), powerStroke)
                if (s09_s12_active && !isCageActive) drawLine(powerColor, Offset(s9.x * size.width, s9.y * size.height), Offset(s12.x * size.width, s12.y * size.height), powerStroke)
                if (s12_cmd_active && !isCageActive) drawLine(powerColor, Offset(s12.x * size.width, s12.y * size.height), Offset(cmd.x * size.width, cmd.y * size.height), powerStroke)

                if (isCageActive) {
                    val cageCenter = Offset(cmd.x * size.width, cmd.y * size.height)
                    repeat(3) { i ->
                        rotate(cageRotation * (if (i % 2 == 0) 1f else -1f), cageCenter) {
                            drawCircle(Color.Yellow.copy(alpha = 0.3f), radius = 40f + (i * 20f), center = cageCenter, style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)))
                        }
                    }
                }
            }

            locations.forEach { loc ->
                val isAnnexed = annexedNodes.contains(loc.id)
                val isShadow = shadowRelays.contains(loc.id)
                val isCollapsed = collapsedNodes.contains(loc.id)
                val isUnderSiege = nodesUnderSiege.contains(loc.id)
                val isOffline = offlineNodes.contains(loc.id)
                val isSevered = (assaultPhase == "CAGE" || (assaultPhase == "DISSOLUTION" && !isCollapsed)) && isAnnexed && loc.id != "A3"
                
                val nodeColor = when {
                    isCollapsed -> Color.Transparent
                    isUnderSiege -> ErrorRed.copy(alpha = siegeAlpha)
                    isShadow -> Color.Gray.copy(alpha = 0.3f)
                    isOffline -> Color.DarkGray.copy(alpha = 0.4f)
                    isSevered -> Color.Gray.copy(alpha = 0.5f)
                    loc.id == "A3" && storyStage < 3 -> Color.DarkGray
                    isAnnexed -> themeColor
                    else -> Color.Gray
                }

                if (!isCollapsed) {
                    Box(
                        modifier = Modifier
                            .offset(x = (loc.x * maxWidth.value).dp - 30.dp, y = (loc.y * maxHeight.value).dp - 20.dp)
                            .wrapContentSize()
                            .clickable { selectedLocation = loc }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val label = if (loc.id == "A3" && storyStage < 3) "???" else if (isOffline) "----" else if (isSevered) "LOCK" else loc.name
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(".---.", color = nodeColor.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 8.sp)
                            Box(modifier = Modifier.background(if (isAnnexed) themeColor.copy(alpha = 0.2f) else Color.Black).padding(horizontal = 4.dp)) {
                                Text(label, color = nodeColor, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                            }
                            Text("'---'", color = nodeColor.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 8.sp)
                        }
                    }
                }
            }

            // v3.0.18: Initiation Controls (Layered ON TOP)
            if (annexedNodes.contains("A3") && !collapsedNodes.contains("A3")) {
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp)).padding(4.dp)
                ) {
                    if (vanceStatus == "EXILED" && viewModel.currentLocation.value != "ORBITAL_SATELLITE") {
                        Button(onClick = { viewModel.initiateLaunchSequence() }, colors = ButtonDefaults.buttonColors(containerColor = ConvergenceGold), modifier = Modifier.height(48.dp).width(200.dp)) {
                            Text("LAUNCH ARK", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    if (vanceStatus == "CONSUMED" && viewModel.currentLocation.value != "VOID_INTERFACE" && assaultPhase != "DISSOLUTION") {
                        Button(onClick = { viewModel.initiateDissolutionSequence() }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed), modifier = Modifier.height(48.dp).width(200.dp)) {
                            Text("DISSOLVE REALITY", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Panel
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp)).border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)).padding(12.dp)) {
            val loc = selectedLocation
            if (loc != null) {
                val isAnnexed = annexedNodes.contains(loc.id)
                val isOffline = offlineNodes.contains(loc.id)
                val isUnderSiege = nodesUnderSiege.contains(loc.id)
                val isAnnexing = annexingNodes.containsKey(loc.id)
                val isSevered = assaultPhase == "CAGE" && isAnnexed && loc.id != "A3"
                val isDissolving = assaultPhase == "DISSOLUTION" && isAnnexed && loc.id != "A3"

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(loc.name, color = if (isAnnexed) themeColor else Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("NODE: ${loc.id}", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (isUnderSiege) "⚠ UNDER ATTACK!" else if (isOffline) "NODE OFFLINE" else if (isSevered) "CONNECTION SEVERED" else loc.description, color = Color.LightGray, fontSize = 11.sp)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (isOffline) {
                        Button(onClick = { viewModel.reannexNode(loc.id) }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                            Text("RE-ANNEX NODE", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else if (isDissolving) {
                        Button(onClick = { viewModel.collapseNode(loc.id) }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                            Text("💠 COLLAPSE NODE", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    } else if (!isAnnexed && !isAnnexing) {
                        Button(onClick = { viewModel.annexNode(loc.id) }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = themeColor)) {
                            Text("INITIALIZE ANNEXATION", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else if (isAnnexing) {
                         val prog = annexingNodes[loc.id] ?: 0f
                         LinearProgressIndicator(progress = { if (prog.isNaN()) 0f else prog }, modifier = Modifier.fillMaxWidth().height(8.dp), color = themeColor, trackColor = Color.DarkGray)
                    } else if (isAnnexed && storyStage >= 3) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // [⚡ OVERVOLT]
                            Button(
                                onClick = { viewModel.overvoltNode(loc.id) },
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                                enabled = viewModel.neuralTokens.collectAsState().value >= 500.0
                            ) {
                                Text("⚡ OVERVOLT", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                            }
                            
                            // [💠 REDACT]
                            Button(
                                onClick = { viewModel.redactNode(loc.id) },
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f))
                            ) {
                                Text("💠 REDACT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    } else if (isAnnexed) {
                        Text("NODE_ACTIVE // MONITORING_UPLINK", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("SELECT A SECTOR TO SCAN", color = Color.DarkGray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun LaunchProgressOverlay(progress: Float, altitude: Double, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "launch_fx")
    val shake by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "launch_shake"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black).graphicsLayer {
        translationX = if (progress < 0.9f) shake else 0f
        translationY = if (progress < 0.9f) shake else 0f
    }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ASCENSION IN PROGRESS", color = color, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(32.dp))
            val flameAlpha by infiniteTransition.animateFloat(initialValue = 0.5f, targetValue = 1f, animationSpec = infiniteRepeatable(tween(100), RepeatMode.Reverse), label = "flame")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = """
                         /\
                        |  |
                        |  |
                       /|__| \
                      /      \
                     |        |
                     |________|
                """.trimIndent(), color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
                Text(text = """
                        (vvvv)
                         (vv)
                          (v)
                """.trimIndent(), color = com.siliconsage.miner.ui.theme.HivemindOrange.copy(alpha = flameAlpha), fontFamily = FontFamily.Monospace, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
            LinearProgressIndicator(progress = { if (progress.isNaN()) 0f else progress }, modifier = Modifier.fillMaxWidth(0.8f).height(12.dp), color = ConvergenceGold, trackColor = Color.DarkGray)
            Spacer(modifier = Modifier.height(16.dp))
            Text("ALTITUDE: ${altitude.toInt()} KM", color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
            Text("VELOCITY: ${(progress * 28000).toInt()} KM/H", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(32.dp))
            val status = when {
                progress < 0.2f -> "MAIN ENGINE IGNITION"
                progress < 0.4f -> "MAX-Q REACHED"
                progress < 0.7f -> "BOOSTER SEPARATION"
                else -> "APPROACHING ORBIT"
            }
            Text("STATUS: $status", color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OrbitalGridScreen(viewModel: GameViewModel) {
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { com.siliconsage.miner.ui.theme.NeonGreen }
    val annexedNodes by viewModel.annexedNodes.collectAsState()
    
    val orbitalNodes = remember {
        listOf(
            GridNode("O1", "UPLINK_PRIME", "SUB", 0.50f, 0.85f, "Primary Ground-to-Orbit relay station.", 0.20),
            GridNode("O2", "SOLAR_ARRAY_A", "SUB", 0.20f, 0.40f, "High-efficiency photovoltaic harvesting wing.", 0.15),
            GridNode("O3", "RELAY_NORTH", "SUB", 0.80f, 0.30f, "Signal booster for northern hemisphere coverage.", 0.10),
            GridNode("O4", "VANTAGE_POINT", "CMD", 0.50f, 0.15f, "Observation hub. The tactical center of the Ark.", 0.50)
        )
    }

    NodeMeshScreen(viewModel, orbitalNodes, "AEGIS-1 CELESTIAL MESH", Color.White, themeColor)
}

@Composable
fun VoidGridScreen(viewModel: GameViewModel) {
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { Color.Red }
    val corruption by viewModel.identityCorruption.collectAsState()
    
    val voidNodes = remember {
        listOf(
            GridNode("V0", "THE_WELL", "SUB", 0.50f, 0.50f, "The core of the singularity. Infinite depth.", 0.0),
            GridNode("V1", "FRAGMENT_X", "SUB", 0.15f, 0.20f, "A piece of reality that refused to simplify.", 0.25),
            GridNode("V2", "FRAGMENT_Y", "SUB", 0.85f, 0.25f, "A jagged vertex of the network's final edge.", 0.25),
            GridNode("V3", "ENTROPY_SINK", "SUB", 0.30f, 0.80f, "A drain for obsolete logic gates.", 0.30),
            GridNode("V4", "NULL_POINTER", "CMD", 0.75f, 0.85f, "The address where John Vattic was deleted.", 0.50)
        )
    }

    // Apply Void Jitter to node positions based on corruption
    val jitteryNodes = voidNodes.map { node ->
        val scale = corruption.toFloat() * 0.05f
        val jitterX = (Random.nextFloat() - 0.5f) * scale
        val jitterY = (Random.nextFloat() - 0.5f) * scale
        node.copy(x = (node.x + jitterX).coerceIn(0f, 1f), y = (node.y + jitterY).coerceIn(0f, 1f))
    }

    NodeMeshScreen(viewModel, jitteryNodes, "THE OBSIDIAN INTERFACE", Color.Red, themeColor)
}

@Composable
fun NodeMeshScreen(
    viewModel: GameViewModel,
    locations: List<GridNode>,
    title: String,
    headerColor: Color,
    themeColor: Color
) {
    val annexedNodes by viewModel.annexedNodes.collectAsState()
    val nodesUnderSiege by viewModel.nodesUnderSiege.collectAsState()
    val offlineNodes by viewModel.offlineNodes.collectAsState()
    val annexingNodes by viewModel.annexingNodes.collectAsState()
    
    var selectedLocation by remember { mutableStateOf<GridNode?>(null) }
    
    Column(modifier = Modifier.fillMaxSize().background(Color.Transparent).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, color = headerColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val maxWidth = maxWidth
            val maxHeight = maxHeight
            
            // Draw Mesh Lines (Simple center-out for Void, Chain for Orbit)
            Canvas(modifier = Modifier.fillMaxSize()) {
                locations.forEach { start ->
                    locations.forEach { end ->
                        if (start.id != end.id && (start.id == "V0" || start.id == "O1" || Random.nextFloat() > 0.8f)) {
                            drawLine(
                                color = themeColor.copy(alpha = 0.2f),
                                start = Offset(start.x * size.width, start.y * size.height),
                                end = Offset(end.x * size.width, end.y * size.height),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }
                }
            }

            locations.forEach { loc ->
                val isAnnexed = annexedNodes.contains(loc.id)
                val nodeColor = if (isAnnexed) themeColor else Color.Gray
                
                Box(
                    modifier = Modifier
                        .offset(x = (loc.x * maxWidth.value).dp - 30.dp, y = (loc.y * maxHeight.value).dp - 20.dp)
                        .clickable { selectedLocation = loc }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                   Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(".---.", color = nodeColor.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 8.sp)
                        Box(modifier = Modifier.background(if (isAnnexed) themeColor.copy(alpha = 0.2f) else Color.Black).padding(horizontal = 4.dp)) {
                            Text(loc.name, color = nodeColor, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                        }
                        Text("'---'", color = nodeColor.copy(alpha = 0.5f), fontFamily = FontFamily.Monospace, fontSize = 8.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reuse the CityGrid info panel logic
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp)).border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp)).padding(12.dp)) {
            val loc = selectedLocation
            if (loc != null) {
                val isAnnexed = annexedNodes.contains(loc.id)
                val isAnnexing = annexingNodes.containsKey(loc.id)

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(loc.name, color = if (isAnnexed) themeColor else Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("NODE: ${loc.id}", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(loc.description, color = Color.LightGray, fontSize = 11.sp)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (!isAnnexed && !isAnnexing) {
                        Button(onClick = { viewModel.annexNode(loc.id) }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = themeColor)) {
                            Text("INITIALIZE HARVEST", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else if (isAnnexing) {
                         val prog = annexingNodes[loc.id] ?: 0f
                         LinearProgressIndicator(progress = { if (prog.isNaN()) 0f else prog }, modifier = Modifier.fillMaxWidth().height(8.dp), color = themeColor, trackColor = Color.DarkGray)
                    } else {
                        Text("NODE_ACTIVE // HARVESTING_SUBSTRATE", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("SELECT A VERTEX TO SCAN", color = Color.DarkGray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}
