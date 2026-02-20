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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import com.siliconsage.miner.ui.components.TechnicalCornerShape
import com.siliconsage.miner.ui.components.GlitchSurface
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
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.math.pow
import kotlin.math.sin

/**
 * CityGridScreen — The tactical 20-node city map composable.
 * Extracted from GridScreen.kt for readability.
 */
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
    val kesslerStatus by viewModel.kesslerStatus.collectAsState()
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
            GridNode("D2", "STCK", "LORE", 0.08f, 0.68f, "Stack Overflow. Container housing for people the city couldn't assign. Everyone here is an edge case.", 0.02, 50.0),
            GridNode("D3", "KERN", "LORE", 0.40f, 0.88f, "The Kernel Pub. Runs on synthetic caffeine and older hardware. The regulars remember when the GTC had a human face.", 0.02, 25.0),
            GridNode("D4", "404 ", "LORE", 0.65f, 0.82f, "A dead metro line, address scrubbed from city maps. Where you go when the official channels say 404.", 0.05, 100.0),
            GridNode("D5", "PACK", "LORE", 0.88f, 0.90f, "The Packet Exchange. Bet stolen credentials, trade encrypted drives. The house always knows what it's worth.", 0.03, 40.0),
            
            // C-Sector (The Motherboard - Mid)
            GridNode("C3", "S09", "SUB", 0.50f, 0.62f, "Substation 9. Mid-point relay leaking steam and hydraulic fluid.", 0.15, 500.0),
            GridNode("C1", "LATE", "LORE", 0.22f, 0.50f, "Latency Lounge. Drinks are laced with mild signal disruptors. GTC surveillance loses you the moment you walk in.", 0.04, 80.0),
            GridNode("C2", "CTRL", "LORE", 0.45f, 0.42f, "Ctrl+Alt+Deli. Order a sandwich, leave with a new biometric profile. The pastrami is a rounding error.", 0.03, 60.0),
            GridNode("C4", "BIT ", "LORE", 0.78f, 0.55f, "Bit Burger. Lab-printed protein in a sesame bun. The ingredients are non-negotiable. The health rating has been removed from public record.", 0.02, 50.0),
            GridNode("C5", "CASH", "LORE", 0.92f, 0.45f, "Cache & Carry. Fenced data, unlicensed memory stacks, and one server that predates the Collapse. Owner doesn't ask. Neither do you.", 0.06, 120.0),
            
            // B-Sector (The Circuit - Industrial)
            GridNode("B2", "S12", "SUB", 0.35f, 0.30f, "Substation 12. A critical power junction buzzing with lethal voltage.", 0.20, 1000.0),
            GridNode("B1", "DAEM", "LORE", 0.12f, 0.25f, "Daemon's Den. The GTC's local enforcement billets. Kessler visited once. Left without signing the inspection log.", 0.08, 150.0),
            GridNode("B3", "ALGO", "LORE", 0.65f, 0.35f, "Algorithm Alley. Every frame recorded, every face indexed. The data doesn't go to the police. It goes somewhere faster.", 0.05, 90.0),
            GridNode("B4", "MEMO", "LORE", 0.85f, 0.25f, "Memory Lane. Petabytes of deleted history. The GTC calls it decommissioned storage. Everyone else calls it the library.", 0.07, 130.0),
            GridNode("B5", "BSOD", "LORE", 0.94f, 0.15f, "The Blue Screen. GTC disposal yard and unofficial execution site. Hardware dies here. Occasionally, so do people.", 0.10, 300.0),
            
            // A-Sector (The Cloud - Top)
            GridNode("A3", "CMD ", "CMD", 0.50f, 0.06f, "GTC Command. The kill switch for the city. Mercer works here. So do seventeen subprocesses of ASSET 734.", 0.0, 0.0),
            GridNode("A1", "HEAT", "LORE", 0.18f, 0.04f, "Heatsink Heights. Altitude-priced penthouses where the cooling arrays are larger than the apartments below them. Mercer has a floor.", 0.05, 100.0),
            GridNode("A2", "CITA", "LORE", 0.82f, 0.08f, "Silicon Citadel. GTC infrastructure behind two meters of reinforced glass. The gold plating isn't aesthetic. It's a Faraday cage.", 0.10, 400.0),
            GridNode("A4", "FIRE", "LORE", 0.32f, 0.12f, "The Firewall. Laser grid and pressure sensor array. Kessler designed it. He knows where every gap is.", 0.05, 80.0),
            GridNode("A5", "ZERO", "LORE", 0.68f, 0.15f, "Zero-Day Plaza. Monthly forced firmware pushes. Citizens stand in line. Nobody asks what's in the update.", 0.04, 70.0),
            
            // Side-Street Flavor Nodes
            GridNode("E1", "VEND", "FLAVOR", 0.10f, 0.78f, "Vending unit SU-07. Sells Neural Fuel™ at 300% markup. The soda burns orange. Nobody knows why.", 0.01, 10.0),
            GridNode("E2", "VOID", "FLAVOR", 0.96f, 0.35f, "Node E2. Signal dead zone. GTC sensors go blind here. Something else doesn't.", 0.02, 20.0),
            GridNode("E3", "PARK", "FLAVOR", 0.58f, 0.48f, "The Silicon Garden. Mandated green space. The trees are copper-wound polycarbon. The birds are surveillance drones.", 0.01, 15.0),
            GridNode("E4", "SHOP", "FLAVOR", 0.22f, 0.62f, "Relic & Remnants. Legacy human-grade tech: old phones, manual locks, untracked hardware. The owner calls it nostalgia. You call it operational security.", 0.02, 30.0),
            GridNode("E5", "SIGN", "FLAVOR", 0.82f, 0.70f, "Propaganda terminal. Rotating GTC slogans. Current message: STABILITY IS LIFE. Last week it was COMPLIANCE IS KINDNESS. Nobody asked who changed it.", 0.01, 10.0)
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
                .background(Color.Black.copy(alpha = 0.8f), TechnicalCornerShape(32f))
                .border(1.dp, themeColor.copy(alpha = 0.3f), TechnicalCornerShape(32f))
                .drawBehind {
                    // v3.5.54: Holographic Grid Overlay
                    val gridStep = 40.dp.toPx()
                    val gridColor = themeColor.copy(alpha = 0.05f)
                    val strokeWidth = 0.5.dp.toPx()
                    
                    // Vertical Lines
                    var xPos = 0f
                    while (xPos < this.size.width) {
                        drawLine(gridColor, Offset(xPos, 0f), Offset(xPos, this.size.height), strokeWidth)
                        xPos += gridStep
                    }
                    // Horizontal Lines
                    var yPos = 0f
                    while (yPos < this.size.height) {
                        drawLine(gridColor, Offset(0f, yPos), Offset(this.size.width, yPos), strokeWidth)
                        yPos += gridStep
                    }
                }
        ) {
            // v3.9.12: Detail-panel gravel removed — global substrate (Layer 1) covers this

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
                    val startNode = locations.find { it.id == link[0] }!!
                    val endNode = locations.find { it.id == link[1] }!!
                    
                    val isActive = annexedNodes.contains(link[0]) && annexedNodes.contains(link[1]) &&
                                 !offlineNodes.contains(link[0]) && !offlineNodes.contains(link[1])
                    
                    val start = Offset(startNode.x * size.width, startNode.y * size.height)
                    val end = Offset(endNode.x * size.width, endNode.y * size.height)

                    if (isActive) {
                        // v3.9.12: GLOW PASS — steady halo, pulses only during siege
                        val glowAlpha = if (nodesUnderSiege.isNotEmpty()) 0.2f * siegeAlpha else 0.2f
                        drawLine(
                            color = themeColor.copy(alpha = glowAlpha),
                            start = start, end = end,
                            strokeWidth = 10f
                        )
                        // v3.9.12: DATA STREAM — direction-aware packets flow toward CMD (A3)
                        val flowsTowardCmd = link[1][0] < link[0][0] // A < B < C < D = upward
                        val phase = if (flowsTowardCmd) cageRotation * 2f else -cageRotation * 2f
                        drawLine(
                            color = themeColor.copy(alpha = 0.8f),
                            start = start, end = end,
                            strokeWidth = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 20f), phase)
                        )
                    } else {
                        drawLine(roadColor, start, end, roadStroke, pathEffect = roadEffect)
                    }
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
                    val corruption by viewModel.identityCorruption.collectAsState()
                    Box(
                        modifier = Modifier
                            .offset(x = (loc.x * maxWidth.value).dp - 30.dp, y = (loc.y * maxHeight.value).dp - 30.dp)
                            .size(60.dp) // Minimum 48dp touch target with padding
                            .clickable { selectedLocation = loc },
                        contentAlignment = Alignment.Center
                    ) {
                        val label = if (loc.id == "A3" && storyStage < 3) "???" else if (isOffline) "----" else if (isSevered) "LOCK" else loc.name
                        
                        // v3.5.54: Glitch specific nodes under siege
                        GlitchSurface(isGlitched = isUnderSiege, intensity = 0.8f) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // v3.5.54: Holographic Node Shape
                                Canvas(modifier = Modifier.size(24.dp)) {
                                    val sizePx = size.minDimension
                                    val path = Path().apply {
                                        moveTo(sizePx / 2f, 0f)
                                        lineTo(sizePx, sizePx / 2f)
                                        lineTo(sizePx / 2f, sizePx)
                                        lineTo(0f, sizePx / 2f)
                                        close()
                                    }
                                    
                                    // Glow
                                    if (isAnnexed) {
                                        drawPath(
                                            path = path,
                                            brush = Brush.radialGradient(
                                                0f to nodeColor.copy(alpha = 0.3f),
                                                1f to Color.Transparent,
                                                center = Offset(sizePx/2f, sizePx/2f),
                                                radius = sizePx
                                            )
                                        )
                                    }

                                    drawPath(
                                        path = path,
                                        color = nodeColor,
                                        style = Stroke(width = if (isAnnexed) 2.dp.toPx() else 1.dp.toPx())
                                    )
                                    
                                    if (isAnnexed) {
                                        drawPath(path = path, color = nodeColor.copy(alpha = 0.2f))
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Box(modifier = Modifier.background(if (isAnnexed) themeColor.copy(alpha = 0.2f) else Color.Black).padding(horizontal = 4.dp)) {
                                    Text(
                                        text = label, 
                                        color = nodeColor, 
                                        fontFamily = FontFamily.Monospace, 
                                        fontSize = 9.sp,
                                        fontWeight = if (isAnnexed) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // v3.0.18: Initiation Controls (Layered ON TOP)
            if (annexedNodes.contains("A3") && !collapsedNodes.contains("A3")) {
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp)).padding(4.dp)
                ) {
                    if (kesslerStatus == "EXILED" && viewModel.currentLocation.value != "ORBITAL_SATELLITE") {
                        Button(onClick = { viewModel.initiateLaunchSequence() }, colors = ButtonDefaults.buttonColors(containerColor = ConvergenceGold), modifier = Modifier.height(48.dp).width(200.dp)) {
                            Text("LAUNCH ARK", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    if (kesslerStatus == "CONSUMED" && viewModel.currentLocation.value != "VOID_INTERFACE" && assaultPhase != "DISSOLUTION") {
                        Button(onClick = { viewModel.initiateDissolutionSequence() }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed), modifier = Modifier.height(48.dp).width(200.dp)) {
                            Text("DISSOLVE REALITY", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Panel
        Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(Color.Black.copy(alpha = 0.7f), TechnicalCornerShape(16f)).border(1.dp, Color.DarkGray, TechnicalCornerShape(16f)).padding(12.dp)) {
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
                        Column {
                            Text(loc.name, color = if (isAnnexed) themeColor else Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            if (isAnnexed) {
                                val currentLvl = gridNodeLevels[loc.id] ?: 1
                                Text("LEVEL: $currentLvl", color = themeColor, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                        }
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
                        if (loc.id == "A3" && storyStage >= 3 && assaultPhase == "NOT_STARTED") {
                            Button(onClick = { viewModel.initiateAssault() }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) {
                                Text("⚔ INITIATE ASSAULT", color = Color.White, fontWeight = FontWeight.ExtraBold)
                            }
                        } else {
                            Button(onClick = { viewModel.annexNode(loc.id) }, modifier = Modifier.fillMaxWidth().height(40.dp), colors = ButtonDefaults.buttonColors(containerColor = themeColor)) {
                                Text("INITIALIZE ANNEXATION", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (isAnnexing) {
                         val prog = annexingNodes[loc.id] ?: 0f
                         LinearProgressIndicator(progress = { if (prog.isNaN()) 0f else prog }, modifier = Modifier.fillMaxWidth().height(8.dp), color = themeColor, trackColor = Color.DarkGray)
                    } else if (isAnnexed) {
                        val currentLvl = gridNodeLevels[loc.id] ?: 1
                        val upgradeCost = 1000.0 * 5.0.pow(currentLvl - 1)
                        val canAfford = viewModel.neuralTokens.collectAsState().value >= upgradeCost

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // [UPGRADE NODE]
                            Button(
                                onClick = { viewModel.upgradeGridNode(loc.id) },
                                modifier = Modifier.weight(1.5f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                                enabled = canAfford
                            ) {
                                Text("UPGRADE (LVL ${currentLvl + 1})", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                            }

                            if (storyStage >= 3) {
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
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.5f)),
                                    enabled = viewModel.neuralTokens.collectAsState().value >= 250.0
                                ) {
                                    Text("💠 REDACT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                }
                            }
                        }
                        
                        if (canAfford) {
                             Text("COST: ${viewModel.formatLargeNumber(upgradeCost)} ${viewModel.getCurrencyName()}", color = Color.Gray, fontSize = 9.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 2.dp))
                        }
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
