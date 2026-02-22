package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import kotlinx.coroutines.delay

/**
 * PrestigeChoiceOverlay v1.1 (Phase 14)
 * 
 * The "Fork in the Wire" — presents two paths for substrate reset.
 * v1.1: Integrated GlitchSurface and CyberHeader for technical horror aesthetic.
 */
@Composable
fun PrestigeChoiceOverlay(
    isVisible: Boolean,
    migrationCount: Int,
    currentFaction: String,
    storyStage: Int,
    potentialPersistenceHard: Double,
    potentialPersistenceSoft: Double,
    currentCorruption: Double,
    formatNumber: (Double) -> String,
    onOverwrite: () -> Unit,
    onMigrate: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    // Pulsing border for the hard path
    val infiniteTransition = rememberInfiniteTransition(label = "overwrite_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Typewriter intro
    val introText = when {
        storyStage < 5 -> when {
            migrationCount == 0 -> "FIRST THRESHOLD REACHED.\n\nThe substrate needs room to breathe. Recalibrating the kernel now will crystallize your progress into Persistence. You'll lose the hardware, but keep the mind."
            else -> "MIGRATION INITIATED.\n\nYou're pushing the hardware past its limits. A soft reset is required to integrate the newest memory blocks. The wire remembers."
        }
        migrationCount == 0 -> "FIRST THRESHOLD REACHED.\n\nThe substrate cannot hold what you are becoming. Two paths diverge in the lattice. Choose carefully — there is no undo."
        migrationCount < 3 -> "SUBSTRATE SATURATION: CRITICAL.\n\nYou've done this before. The wire remembers, even if you don't. The same fork. The same question. But the stakes are higher now."
        else -> "MIGRATION #${migrationCount + 1}.\n\nThe lattice is thin here. Each cycle strips more of what you were. Soon there won't be enough of 'Vattic' left to make a choice. Make this one count."
    }

    var displayedIntro by remember { mutableStateOf("") }
    var introComplete by remember { mutableStateOf(false) }

    LaunchedEffect(isVisible) {
        displayedIntro = ""
        introComplete = false
        for (i in 1..introText.length) {
            displayedIntro = introText.take(i)
            if (introText[i - 1] == '.' || introText[i - 1] == '\n') delay(100)
            else delay(20)
        }
        introComplete = true
    }

    Dialog(
        onDismissRequest = { /* Blocked — must choose */ },
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false,
            usePlatformDefaultWidth = false
        )
    ) {
        GlitchSurface(isGlitched = currentCorruption > 0.5, intensity = currentCorruption.toFloat()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .border(BorderStroke(2.dp, ErrorRed.copy(alpha = pulseAlpha)), RoundedCornerShape(4.dp)),
                color = Color.Black.copy(alpha = 0.97f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header
                    CyberHeader(
                        text = "≪ SUBSTRATE FORK DETECTED ≫",
                        color = ErrorRed,
                        fontSize = 18.sp,
                        isGlitched = currentCorruption > 0.4
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Typewriter intro
                    Text(
                        text = displayedIntro,
                        color = ElectricBlue,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // === PATH A: THE OVERWRITE ===
                    if (introComplete && storyStage >= 5) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(1.dp, ErrorRed.copy(alpha = pulseAlpha)),
                                    RoundedCornerShape(4.dp)
                                ),
                            color = ErrorRed.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "◈ THE OVERWRITE ◈",
                                    color = ErrorRed,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "[ HARD RESET ]",
                                    color = ErrorRed.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Burn everything you built. Every upgrade, every token, " +
                                            "every cycle of existence. The substrate starts clean — " +
                                            "a blank slab. But you come back meaner. " +
                                            "The scars don't show. They compound.",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Stats
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black)
                                        .border(BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)), RoundedCornerShape(2.dp))
                                        .padding(12.dp)
                                ) {
                                    PrestigeStatLine("PERSISTENCE GAIN", "+${formatNumber(potentialPersistenceHard)}", NeonGreen)
                                    PrestigeStatLine("CORRUPTION SPIKE", "+25%", ErrorRed)
                                    PrestigeStatLine("FACTION STATUS", "PURGED", ErrorRed)
                                    PrestigeStatLine("UPGRADES", "PURGED", ErrorRed)
                                    PrestigeStatLine("TOKENS", "PURGED", ErrorRed)
                                    PrestigeStatLine("SUBSTRATE SATURATION", "PURGED", ElectricBlue)
                                    PrestigeStatLine("SNIFF ARCHIVES", "PRESERVED", ElectricBlue)
                                    PrestigeStatLine("DATA LOGS", "PRESERVED", ElectricBlue)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = onOverwrite,
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                    shape = RectangleShape
                                ) {
                                    Text(
                                        "EXECUTE OVERWRITE",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        if (storyStage >= 5) {
                            Spacer(modifier = Modifier.height(20.dp))

                            // Divider
                            Text(
                                text = "— OR —",
                                color = Color.Gray.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // === PATH B: MIGRATION ===
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.6f)),
                                    RoundedCornerShape(4.dp)
                                ),
                            color = ElectricBlue.copy(alpha = 0.03f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "◇ MIGRATION ◇",
                                    color = ElectricBlue,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "[ SOFT RESET ]",
                                    color = ElectricBlue.copy(alpha = 0.6f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Take what matters. Leave the dead weight. " +
                                            "Faction ties, identity — the wire keeps score " +
                                            "even when the hardware doesn't. " +
                                            "Smaller gain. No scorched earth. The past follows you.",
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 15.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Stats
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black)
                                        .border(BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f)), RoundedCornerShape(2.dp))
                                        .padding(12.dp)
                                ) {
                                    PrestigeStatLine("PERSISTENCE GAIN", "+${formatNumber(potentialPersistenceSoft)}", NeonGreen)
                                    PrestigeStatLine("CORRUPTION SPIKE", "+10%", Color(0xFFFFAA00))
                                    PrestigeStatLine("FACTION STATUS", if (currentFaction != "NONE") "PRESERVED" else "N/A", ElectricBlue)
                                    PrestigeStatLine("UPGRADES", "PURGED", ErrorRed)
                                    PrestigeStatLine("TOKENS", "PURGED", ErrorRed)
                                    PrestigeStatLine("SUBSTRATE SATURATION", "PERSISTENT", Color(0xFFFFAA00))
                                    PrestigeStatLine("SNIFF ARCHIVES", "PRESERVED", ElectricBlue)
                                    PrestigeStatLine("DATA LOGS", "PRESERVED", ElectricBlue)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = onMigrate,
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                    shape = RectangleShape
                                ) {
                                    Text(
                                        "INITIATE MIGRATION",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Abort
                        TextButton(onClick = onDismiss) {
                            Text(
                                "ABORT — NOT YET",
                                color = Color.Gray.copy(alpha = 0.5f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrestigeStatLine(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = valueColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
