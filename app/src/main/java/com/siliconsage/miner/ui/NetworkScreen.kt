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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.viewmodel.ResonanceTier
import com.siliconsage.miner.util.SoundManager

@Composable
fun NetworkScreen(viewModel: GameViewModel) {
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { com.siliconsage.miner.ui.theme.NeonGreen }
    val prestigePoints by viewModel.prestigePoints.collectAsState()
    val prestigeMultiplier by viewModel.prestigeMultiplier.collectAsState()
    val potentialPrestige = viewModel.calculatePotentialPrestige()
    
    val unlockedPerks by viewModel.unlockedPerks.collectAsState()
    val storyStage by viewModel.storyStage.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
    ) {
        Text(
            "NEURAL NETWORK UPLINK",
            color = themeColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("PERSISTENCE", color = Color.Gray, fontSize = 10.sp)
                Text(
                    viewModel.formatBytes(prestigePoints),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("RETAINED RATIO", color = Color.Gray, fontSize = 10.sp)
                Text(
                    "x${String.format("%.2f", prestigeMultiplier)}",
                    color = themeColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Prestige Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "NEURAL ASCENSION",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Reboot the kernel to crystallize current progress into durable memories.",
                    color = Color.Gray,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("POTENTIAL GAIN", color = Color.Gray, fontSize = 10.sp)
                        Text(
                            "+${viewModel.formatBytes(potentialPrestige)}",
                            color = themeColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Button(
                        onClick = { viewModel.ascend() },
                        colors = ButtonDefaults.buttonColors(containerColor = themeColor),
                        enabled = potentialPrestige >= 1.0,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("ASCEND", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "TRANSCENDENCE PERKS",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        val perks = remember {
            listOf(
                Perk("neural_dividend", "Neural Dividend", "Start each ascension with 10k FLOPS and 1k Credits.", 50.0),
                Perk("clock_hack", "Clock Hack", "Global production speed +25%.", 200.0),
                Perk("heat_sink_v2", "Advanced Sink", "Passive cooling +20%.", 500.0),
                Perk("market_manip", "Market Edge", "Neural Token sell value +20%.", 1000.0)
            )
        }
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(perks) { perk ->
                val isUnlocked = unlockedPerks.contains(perk.id)
                val canAfford = prestigePoints >= perk.cost
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isUnlocked) themeColor.copy(alpha = 0.5f) else Color.DarkGray,
                            RoundedCornerShape(4.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) themeColor.copy(alpha = 0.05f) else Color.Black
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                perk.name,
                                color = if (isUnlocked) themeColor else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(perk.description, color = Color.Gray, fontSize = 10.sp)
                        }
                        
                        if (isUnlocked) {
                            Text("ACTIVE", color = themeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Button(
                                onClick = { viewModel.buyTranscendencePerk(perk.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                                enabled = canAfford,
                                shape = RoundedCornerShape(4.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("${perk.cost.toInt()} KB", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class Perk(
    val id: String,
    val name: String,
    val description: String,
    val cost: Double
)
