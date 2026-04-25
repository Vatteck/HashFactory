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
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: GameViewModel, onNavigate: (Screen) -> Unit = {}) {
    val themeColorHex by viewModel.themeColor.collectAsState()
    val themeColor = try { Color(android.graphics.Color.parseColor(themeColorHex)) } catch (e: Exception) { com.siliconsage.miner.ui.theme.NeonGreen }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            var configClickCount by remember { mutableIntStateOf(0) }
            Text(
                "SYSTEM CONFIGURATION",
                color = themeColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { 
                        configClickCount++
                        if (configClickCount >= 5) {
                            viewModel.toggleDevMenu()
                            configClickCount = 0
                            com.siliconsage.miner.util.SoundManager.play("glitch")
                        }
                    }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Audio Settings
            val sfxEnabled by com.siliconsage.miner.util.SoundManager.isSfxEnabled.collectAsState()
            val sfxVolume by com.siliconsage.miner.util.SoundManager.sfxVolume.collectAsState()
            val bgmEnabled by com.siliconsage.miner.util.SoundManager.isBgmEnabled.collectAsState()
            val bgmVolume by com.siliconsage.miner.util.SoundManager.bgmVolume.collectAsState()
            val selectedBgmTrack by com.siliconsage.miner.util.SoundManager.selectedBgmTrack.collectAsState()

            SettingsGroup("AUDIO") {
                SettingsToggle("SFX ENABLED", sfxEnabled) {
                    com.siliconsage.miner.util.SoundManager.setSfxEnabled(it)
                }
                if (sfxEnabled) {
                    Text("SFX VOLUME", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = sfxVolume,
                        onValueChange = { com.siliconsage.miner.util.SoundManager.setSfxVolume(it) },
                        colors = SliderDefaults.colors(thumbColor = themeColor, activeTrackColor = themeColor)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                SettingsToggle("BGM ENABLED", bgmEnabled) {
                    com.siliconsage.miner.util.SoundManager.setBgmEnabled(it)
                }
                if (bgmEnabled) {
                    Text("BGM VOLUME", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = bgmVolume,
                        onValueChange = { com.siliconsage.miner.util.SoundManager.setBgmVolume(it) },
                        colors = SliderDefaults.colors(thumbColor = themeColor, activeTrackColor = themeColor)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("BGM TRACK", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        listOf("bgm.ogg", "bgm2.ogg", "bgm3.ogg").forEachIndexed { index, track ->
                            Button(
                                onClick = { com.siliconsage.miner.util.SoundManager.setSelectedBgmTrack(track) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedBgmTrack == track) themeColor else Color.DarkGray
                                ),
                                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("TRACK ${index + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (selectedBgmTrack == track) Color.Black else Color.White)
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Human Condition
            val decisionsMade by viewModel.decisionsMade.collectAsState()
            SettingsGroup("NARRATIVE ENGAGEMENT") {
                Text("DECISIONS MADE: $decisionsMade / 30", color = themeColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { (decisionsMade / 30f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp).padding(vertical = 4.dp),
                    color = themeColor,
                    trackColor = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // v3.2.19: Utility Audit
            val lifetimePower by viewModel.lifetimePowerPaid.collectAsState()
            val energyPrice by viewModel.energyPriceMultiplier.collectAsState()
            SettingsGroup("UTILITY AUDIT") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("LIFETIME POWER COST:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${viewModel.formatLargeNumber(lifetimePower)} \$N", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("CURRENT TARIFF:", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${String.format("%.2f", energyPrice)} \$N/kW", color = themeColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // v3.2.19: Data Sovereignty
            SettingsGroup("DATA MANAGEMENT") {
                Button(
                    onClick = { 
                        val json = viewModel.exportSystemDump()
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("silicon_sage_dump", json)
                        clipboard.setPrimaryClip(clip)
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("SYSTEM DUMP COPIED TO CLIPBOARD")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, themeColor.copy(alpha = 0.3f))
                ) {
                    Text("EXPORT SYSTEM DUMP (JSON)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                var showImportDialog by remember { mutableStateOf(false) }
                var importText by remember { mutableStateOf("") }
                
                if (showImportDialog) {
                    AlertDialog(
                        onDismissRequest = { showImportDialog = false },
                        title = { Text("LOAD SYSTEM DUMP", color = themeColor, fontWeight = FontWeight.Bold) },
                        text = {
                            Column {
                                Text("PASTE JSON KERNEL DUMP BELOW:", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(bottom = 8.dp))
                                OutlinedTextField(
                                    value = importText,
                                    onValueChange = { importText = it },
                                    modifier = Modifier.fillMaxWidth().height(200.dp),
                                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.LightGray,
                                        focusedBorderColor = themeColor,
                                        unfocusedBorderColor = Color.Gray
                                    )
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                if (viewModel.importSystemDump(importText)) {
                                    showImportDialog = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("KERNEL RELOAD SUCCESSFUL")
                                    }
                                }
                            }) {
                                Text("INITIALIZE RELOAD", color = themeColor, fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showImportDialog = false }) {
                                Text("CANCEL", color = Color.Gray)
                            }
                        },
                        containerColor = Color.Black,
                        shape = RoundedCornerShape(4.dp)
                    )
                }
                
                Button(
                    onClick = { showImportDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.8f)),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text("IMPORT SYSTEM DUMP", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Operations Settings
            val isPaused by viewModel.isSettingsPaused.collectAsState()
            SettingsGroup("OPERATIONS") {
                SettingsToggle("SUSPEND CALCULATIONS (PAUSE)", isPaused) {
                    viewModel.setGamePaused(it)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // UI Scaling Options
            val currentScale by viewModel.uiScale.collectAsState()
            val customScaleFactor by viewModel.customUiScaleFactor.collectAsState()
            SettingsGroup("DISPLAY") {
                Text("UI SCALING", color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                // v3.4.11: Fine-grained slider control
                Slider(
                    value = customScaleFactor,
                    onValueChange = { viewModel.setCustomUIScale(it) },
                    valueRange = 0.5f..1.5f,
                    steps = 20,
                    colors = SliderDefaults.colors(thumbColor = themeColor, activeTrackColor = themeColor)
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("0.5x", color = Color.Gray, fontSize = 8.sp)
                    Text("${String.format("%.2f", customScaleFactor)}x", color = themeColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    Text("1.5x", color = Color.Gray, fontSize = 8.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    com.siliconsage.miner.data.UIScale.values().forEach { scale ->
                        Button(
                            onClick = { viewModel.setUIScale(scale) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentScale == scale) themeColor else Color.DarkGray
                            ),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(scale.displayName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (currentScale == scale) Color.Black else Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            // Game Version & Updates
            SettingsGroup("SYSTEM INFO") {
                Text("VERSION: ${com.siliconsage.miner.BuildConfig.VERSION_NAME}", color = Color.White, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { 
                        viewModel.checkForUpdates(context, true) { info, found ->
                            if (!found) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("SYSTEM IS UP TO DATE")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Text("CHECK FOR UPDATES", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dangerous Actions
            Button(
                onClick = { onNavigate(Screen.ARCHIVE) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = themeColor.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, themeColor.copy(alpha = 0.5f))
            ) {
                Text("DATA LOG ARCHIVE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.resetGame(true) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = com.siliconsage.miner.ui.theme.ErrorRed.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, com.siliconsage.miner.ui.theme.ErrorRed.copy(alpha = 0.3f))
            ) {
                Text("WIPE KERNEL (RESET GAME)", color = com.siliconsage.miner.ui.theme.ErrorRed.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "SILICON SAGE v${com.siliconsage.miner.BuildConfig.VERSION_NAME}",
                color = Color.Gray,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)
        )
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
