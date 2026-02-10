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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.viewmodel.GameViewModel
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.ui.theme.NeonGreen

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
        SettingsGroup("INTERFACE") {
            Text("THEME COLOR", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ColorBox("#00FF00", viewModel) // Neon Green
                ColorBox("#FFD700", viewModel) // Gold
                ColorBox("#7DF9FF", viewModel) // Electric Blue
                ColorBox("#FF3131", viewModel) // Error Red
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Game Version & Updates
        SettingsGroup("SYSTEM INFO") {
            Text("VERSION: ${com.siliconsage.miner.BuildConfig.VERSION_NAME}", color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.checkForUpdates(context) },
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
