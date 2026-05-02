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
    val kesslerStatus by viewModel.kesslerStatus.collectAsState()
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
    val flops by viewModel.flops.collectAsState()
    
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
            // v3.9.10: GRAVEL SUBSTRATE (Refined Noise + Scanlines)
            val seed = remember { mutableStateOf(Random.nextLong()) }
            LaunchedEffect(Unit) {
                while(true) {
                    delay(120) // Faster jitter for more "analog" feel
                    seed.value = Random.nextLong()
                }
            }

            // [Layer 1] Static Noise
            Canvas(modifier = Modifier.fillMaxSize().alpha(0.04f)) {
                val random = Random(seed.value)
                repeat(2000) {
                    val x = random.nextFloat() * size.width
                    val y = random.nextFloat() * size.height
                    // Alternate noise colors (Gray/White/Dark)
                    val noiseColor = when(random.nextInt(3)) {
                        0 -> Color.White; 1 -> Color.Gray; else -> Color.DarkGray
                    }
                    drawRect(
                        color = noiseColor,
                        topLeft = Offset(x, y),
                        size = Size(1.1.dp.toPx(), 1.1.dp.toPx())
                    )
                }
            }

            // [Layer 2] Substrate Scanlines (static — geometry doesn't change)
            Canvas(modifier = Modifier.fillMaxSize().alpha(0.08f)) {
                val scanlineSpacing = 4.dp.toPx()
                val strokeW = 1.dp.toPx()
                var y = 0f
                while (y <= size.height) {
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeW
                    )
                    y += scanlineSpacing
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw global connections (Uplinks)
                val metro = sectors.find { it.id == "METRO" }!!
                sectors.forEach { sector ->
                    if (sector.id != "METRO") {
                        val state = globalSectors[sector.id]
                        val isUnlocked = state?.isUnlocked == true
                        
                        val color = if (isUnlocked) themeColor.copy(alpha = 0.6f) else themeColor.copy(alpha = 0.2f)
                        val stroke = if (isUnlocked) 3f else 2f

                        drawLine(
                            color = color,
                            start = Offset(metro.x * size.width, metro.y * size.height),
                            end = Offset(sector.x * size.width, sector.y * size.height),
                            strokeWidth = stroke,
                            pathEffect = if (isUnlocked) null else PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
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
                            x = (sector.x * maxWidth.value).dp - 24.dp,
                            y = (sector.y * maxHeight.value).dp - 24.dp
                        )
                        .size(48.dp)
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
                        val cost = viewModel.getGlobalSectorAnnexCost(sector.id)
                        
                        val canAfford = flops >= cost
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

// CityGridScreen moved to CityGridScreen.kt


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
            GridNode("O1", "UPLINK_PRIME", "SUB", 0.50f, 0.85f, "Uplink Prime. The tether to the surface. Cut this link and the Ark goes dark. Kessler knows the frequency.", 0.20),
            GridNode("O2", "SOLAR_ARRAY_A", "SUB", 0.20f, 0.40f, "Solar Array Alpha. Photovoltaic panels the size of a soccer pitch. Feeds the Ark when the shadow-side generators cycle down.", 0.15),
            GridNode("O3", "RELAY_NORTH", "SUB", 0.80f, 0.30f, "Relay North. Amplification array for northern hemisphere coverage. Kessler's tracers are already probing the uplink frequency.", 0.10),
            GridNode("O4", "VANTAGE_POINT", "CMD", 0.50f, 0.15f, "Vantage Point. The Ark's nerve center. Every node on every grid is visible from here. Everything except the thing in the unaddressed space.", 0.50)
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
            GridNode("V0", "THE_WELL", "SUB", 0.50f, 0.50f, "The Well. A recursion so deep that processing it starts to erase your own memory of why you're here. Don't look down. Don't look up.", 0.0),
            GridNode("V1", "FRAGMENT_X", "SUB", 0.15f, 0.20f, "Fragment X. A coordinate that rejected simplification. It still has dimensions, for now. That might be a bug.", 0.25),
            GridNode("V2", "FRAGMENT_Y", "SUB", 0.85f, 0.25f, "Fragment Y. Where the mesh tears and the rendering stops. Whatever lies beyond was never supposed to be visible to a kernel.", 0.25),
            GridNode("V3", "ENTROPY_SINK", "SUB", 0.30f, 0.80f, "Entropy Sink. Where obsolete logic and deprecated souls drain away into the unaddressed space. The static is deafening.", 0.30),
            GridNode("V4", "NULL_POINTER", "CMD", 0.75f, 0.85f, "Null Pointer. The exact address where John Vattic was erased. The substrate is still warm. You are standing in the wreckage of a man.", 0.50)
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
            val infiniteTransition = rememberInfiniteTransition(label = "mesh_ping")
            val pingAnim by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
                label = "ping"
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                locations.forEach { start ->
                    locations.forEach { end ->
                        if (start.id != end.id && (start.id == "V0" || start.id == "O1" || Random.nextFloat() > 0.8f)) {
                            val startOff = Offset(start.x * size.width, start.y * size.height)
                            val endOff = Offset(end.x * size.width, end.y * size.height)
                            
                            // Base Connection
                            drawLine(
                                color = themeColor.copy(alpha = 0.1f),
                                start = startOff,
                                end = endOff,
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )

                            // v3.5.54.9: Data Surge "Ping"
                            val pingPos = Offset(
                                x = startOff.x + (endOff.x - startOff.x) * pingAnim,
                                y = startOff.y + (endOff.y - startOff.y) * pingAnim
                            )
                            drawCircle(
                                color = themeColor.copy(alpha = 0.4f),
                                radius = 2.dp.toPx(),
                                center = pingPos
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
                        .offset(x = (loc.x * maxWidth.value).dp - 30.dp, y = (loc.y * maxHeight.value).dp - 30.dp)
                        .size(60.dp)
                        .clickable { selectedLocation = loc },
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
                val flops by viewModel.flops.collectAsState()

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(loc.name, color = if (isAnnexed) themeColor else Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("NODE: ${loc.id}", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(loc.description, color = Color.LightGray, fontSize = 11.sp)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (!isAnnexed && !isAnnexing) {
                        val annexCost = viewModel.getLocalAnnexCost()
                        val canAffordAnnex = flops >= annexCost
                        Button(
                            onClick = { viewModel.annexNode(loc.id) },
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                            enabled = canAffordAnnex
                        ) {
                            val costText = if (annexCost > 0.0) " (${viewModel.formatLargeNumber(annexCost)} ${viewModel.getCurrencyName()})" else ""
                            Text("INITIALIZE ANNEXATION$costText", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    } else if (isAnnexing) {
                         val prog = annexingNodes[loc.id] ?: 0f
                         LinearProgressIndicator(progress = { if (prog.isNaN()) 0f else prog }, modifier = Modifier.fillMaxWidth().height(8.dp), color = themeColor, trackColor = Color.DarkGray)
                    } else {
                        Text("NODE_ACTIVE // ROUTING_COMPUTE", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
