package com.siliconsage.miner.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.ui.theme.ErrorRed

@Composable
fun SettingsScreen(viewModel: GameViewModel) {
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { com.siliconsage.miner.ui.theme.NeonGreen }
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "SYSTEM CONFIGURATION",
            color = themeColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Audio Settings
        SettingsGroup("AUDIO") {
            SettingsToggle("SFX ENABLED", com.siliconsage.miner.util.SoundManager.isSfxEnabled) {
                com.siliconsage.miner.util.SoundManager.isSfxEnabled = it
            }
            SettingsToggle("HAPTICS ENABLED", com.siliconsage.miner.util.HapticManager.isHapticsEnabled) {
                com.siliconsage.miner.util.HapticManager.isHapticsEnabled = it
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Display Settings
        val singularityChoice by viewModel.singularityChoice.collectAsState()
        val faction by viewModel.faction.collectAsState()
        SettingsGroup("INTERFACE") {
            Text("THEME COLOR: CHOICE-SYNC ACTIVE", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(32.dp).background(themeColor, RoundedCornerShape(4.dp)).border(1.dp, Color.White, RoundedCornerShape(4.dp)))
                Text("CURRENT: ${if (singularityChoice != "NONE") singularityChoice else faction}", color = Color.White, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterVertically))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Human Condition
        val humanity by viewModel.humanityScore.collectAsState()
        SettingsGroup("NEURAL SYNC") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("HUMANITY INDEX: $humanity%", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                val isDevVisible by viewModel.isDevMenuVisible.collectAsState()
                Box(modifier = Modifier.size(24.dp).clickable { viewModel.toggleDevMenu() }) // Invisible dev trigger
            }
            LinearProgressIndicator(
                progress = { humanity / 100f },
                modifier = Modifier.fillMaxWidth().height(4.dp).padding(vertical = 4.dp),
                color = themeColor,
                trackColor = Color.DarkGray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Developer Menu (Hidden)
        val isDevMenuVisible by viewModel.isDevMenuVisible.collectAsState()
        if (isDevMenuVisible) {
            SettingsGroup("DEVELOPER OVERRIDE") {
                Button(
                    onClick = { viewModel.resetGame(true) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed.copy(alpha = 0.5f))
                ) {
                    Text("FORCED KERNEL WIPE", fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.debugGrantPhase13Resources() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue.copy(alpha = 0.5f))
                ) {
                    Text("GRANT v13 RESOURCES", fontSize = 10.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Operations Settings
        val isPaused by viewModel.isSettingsPaused.collectAsState()
        SettingsGroup("OPERATIONS") {
            SettingsToggle("SUSPEND CALCULATIONS (PAUSE)", isPaused) {
                viewModel.setGamePaused(it)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Game Version & Updates
        SettingsGroup("SYSTEM INFO") {
            Text("VERSION: ${com.siliconsage.miner.BuildConfig.VERSION_NAME}", color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.checkForUpdates(context, true) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("CHECK FOR UPDATES", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Dangerous Actions
        Button(
            onClick = { viewModel.resetGame(true) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = com.siliconsage.miner.ui.theme.ErrorRed.copy(alpha = 0.3f)),
            border = BorderStroke(1.dp, com.siliconsage.miner.ui.theme.ErrorRed)
        ) {
            Text("WIPE KERNEL (RESET GAME)", color = com.siliconsage.miner.ui.theme.ErrorRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonGreen,
                checkedTrackColor = NeonGreen.copy(alpha = 0.3f)
            )
        )
    }
}

@Composable
private fun ColorBox(hex: String, viewModel: GameViewModel) {
    val themeColorHex by viewModel.themeColor.collectAsState()
    val isSelected = themeColorHex == hex
    
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(Color(android.graphics.Color.parseColor(hex)), RoundedCornerShape(4.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.White else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { 
                viewModel.themeColor.value = hex
                com.siliconsage.miner.util.SoundManager.play("click")
            }
    )
}
