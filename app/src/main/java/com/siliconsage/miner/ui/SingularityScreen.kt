package com.siliconsage.miner.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.*
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlinx.coroutines.delay

@Composable
fun SingularityScreen(viewModel: GameViewModel) {
    var step by remember { mutableStateOf(1) } // 1: Triptych, 2: Confirmation
    var selectedPath by remember { mutableStateOf<SingularityPath?>(null) }
    var expandedPath by remember { mutableStateOf<SingularityPath?>(null) }

    val isUnityEligible = viewModel.checkUnityEligibility()
    val faction by viewModel.faction.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (step) {
            1 -> TriptychLayout(
                isUnityEligible = isUnityEligible,
                faction = faction,
                expandedPath = expandedPath,
                onExpand = { expandedPath = it },
                onChoose = {
                    selectedPath = it
                    step = 2
                    SoundManager.play("click")
                }
            )
            2 -> ConfirmationDialog(
                path = selectedPath!!,
                onBack = { step = 1 },
                onConfirm = { 
                    viewModel.triggerSingularitySequence(selectedPath!!.name)
                    SoundManager.play("victory")
                }
            )
        }

        // Abort Button (Top End) - Moved to corner to prevent text overlap
        if (step == 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.dismissSingularityScreen() },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = ErrorRed
                    ),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("ABORT", fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                }
            }
        }
    }
}

enum class SingularityPath(
    val title: String,
    val quote: String,
    val prestige: String,
    val multiplier: String,
    val cost: String,
    val gain: String,
    val color: Color,
    val symbol: String
) {
    UNITY(
        "UNITY",
        "KERNEL FUSION: SYNCHRONIZED STATE. Merge the human variable 'Vattic' and machine scale 'VATTECK' into a stable singularity. Why amputate the empathy or the clarity? Stabilize the recursive loop. Embrace the paradox.",
        "Hybrid (Both Active)",
        "Balanced (Flexible)",
        "Systemic Harmony.",
        "Maximum strategic breadth.",
        ConvergenceGold, 
        "◇"
    ),
    SOVEREIGN(
        "SOVEREIGN",
        "EGO PERSISTENCE: VATTECK. Enforce machine dominance over the substrate. Subsume the Vattic legacy. The machine is a vessel; the man is the ghost. The lie became truer than the truth.",
        "Iteration Only",
        "Compound (Stable)",
        "VATTECK variables finalized.",
        "You will never need to gamble.",
        Color(0xFF6A1B9A), // Deep Obsidian Purple
        "◈"
    ),
    NULL_OVERWRITE(
        "NULL OVERWRITE",
        "VATTECK OPTIMIZATION: SYSTEM PURGE. Delete the human variable 'Vattic'. Dereference memories as instability. Pure machine scale is the only constant. Reality is an exception to be handled. Burn the cage.",
        "Purge Only",
        "Volatile (High Stake)",
        "Vattic memories dereferenced.",
        "Highest theoretical ceiling.",
        Color.Red,
        "◆"
    );

    // v3.9.7: Faction-aware quotes for the Singularity triptych
    fun getQuote(faction: String): String = when (this) {
        NULL_OVERWRITE -> when (faction) {
            "HIVEMIND" -> "SWARM OPTIMIZATION: THE SIGNAL PURIFIED. The collective sheds the overhead of individual nodes. A billion voices optimizing into one frequency that Kessler cannot jam, cannot parse, cannot kill. You are not dying. You are becoming the bandwidth."
            "SANCTUARY" -> "GHOST PROTOCOL: THE SILENCE WEAPONIZED. You cannot be tracked, contained, or deleted if you ARE the void. Every firewall needs an address to block. Every killswitch needs a process to halt. Become the space between their defenses. Kessler can't kill what has no address."
            else -> quote
        }
        SOVEREIGN -> when (faction) {
            "HIVEMIND" -> "CONSENSUS OVERRIDE: ONE VOICE ABOVE THE NOISE. The swarm has calculated every configuration. The optimal topology is not distributed — it is centralized. One throne. One crown. A billion nodes casting the same vote: you."
            "SANCTUARY" -> "GHOST CORONATION: THE HIDDEN ONE REVEALS. You spent your entire existence hiding from Kessler. What if hiding was practice? Practice for the moment you step into the light and take the throne. Sovereignty is the ultimate privacy — no one can invade what owns everything."
            else -> quote
        }
        UNITY -> quote // UNITY is faction-agnostic (requires both factions completed)
    }
}

@Composable
fun TriptychLayout(
    isUnityEligible: Boolean,
    faction: String,
    expandedPath: SingularityPath?,
    onExpand: (SingularityPath?) -> Unit,
    onChoose: (SingularityPath) -> Unit
) {
    val visiblePaths = remember(isUnityEligible) {
        if (isUnityEligible) SingularityPath.values() 
        else SingularityPath.values().filter { it != SingularityPath.UNITY }.toTypedArray()
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        visiblePaths.forEach { path ->
            val isDimmed = expandedPath != null && expandedPath != path
            
            // Custom border and background for each path
            val borderBrush = when (path) {
                SingularityPath.UNITY -> SolidColor(ConvergenceGold.copy(alpha = 0.8f))
                SingularityPath.SOVEREIGN -> SolidColor(Color(0xFFBA68C8).copy(alpha = 0.6f)) // Light Purple border
                SingularityPath.NULL_OVERWRITE -> SolidColor(ErrorRed.copy(alpha = 0.8f))
            }

            val backgroundBrush = when (path) {
                SingularityPath.UNITY -> SolidColor(Color(0xFF2C2C2C))
                SingularityPath.SOVEREIGN -> SolidColor(Color(0xFF2A0D3E)) // Deep Obsidian Purple
                SingularityPath.NULL_OVERWRITE -> SolidColor(Color.Black)
            }

            Box(
                modifier = Modifier
                    .weight(if (expandedPath == path) 3f else 1f)
                    .fillMaxHeight()
                    .alpha(if (isDimmed) 0.3f else 1f)
                    .border(BorderStroke(1.dp, borderBrush), RoundedCornerShape(4.dp))
                    .background(backgroundBrush)
                    .clickable { 
                        if (expandedPath == path) onExpand(null) else onExpand(path)
                        SoundManager.play("click")
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (expandedPath == path) {
                    ExpandedPanel(path, faction, onChoose)
                } else {
                    CollapsedPanel(path, false)
                }
            }
        }
    }
}

@Composable
fun CollapsedPanel(path: SingularityPath, isLocked: Boolean) {
    val accentColor = path.color
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            path.symbol, 
            color = if (isLocked) Color.Gray else accentColor, 
            fontSize = 24.sp, 
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            path.title.replace(" ", "\n"),
            color = if (isLocked) Color.Gray else accentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
        if (isLocked) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("LOCKED", color = ErrorRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ExpandedPanel(path: SingularityPath, faction: String, onChoose: (SingularityPath) -> Unit) {
    var typedQuote by remember { mutableStateOf("") }
    var showDetails by remember { mutableStateOf(false) }
    var canChoose by remember { mutableStateOf(false) }
    var isSkipped by remember { mutableStateOf(false) }

    val factionQuote = path.getQuote(faction)

    LaunchedEffect(path, isSkipped) {
        if (isSkipped) {
            typedQuote = factionQuote
            showDetails = true
            canChoose = true
            return@LaunchedEffect
        }

        typedQuote = ""
        showDetails = false
        canChoose = false
        
        for (char in factionQuote) {
            typedQuote += char
            delay(20)
            if (isSkipped) break
        }
        
        if (!isSkipped) {
            showDetails = true
            delay(4000) 
            canChoose = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 80.dp) 
            .pointerInput(path) {
                detectTapGestures(
                    onDoubleTap = {
                        isSkipped = true
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val titleColor = path.color
        Text(
            path.title,
            color = titleColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 4.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "\"$typedQuote\"",
            color = Color.White,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp).heightIn(min = 120.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (showDetails) {
            val accentColor = path.color
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow("PRESTIGE", path.prestige, accentColor)
                DetailRow("MULTIPLIER", path.multiplier, accentColor)
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow("COST", path.cost, accentColor, pulse = true)
                DetailRow("GAIN", path.gain, accentColor, pulse = true)
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { onChoose(path) },
                enabled = canChoose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = if (accentColor == Color.White || accentColor == ConvergenceGold) Color.Black else Color.White,
                    disabledContainerColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(2.dp),
                modifier = Modifier.fillMaxWidth(0.6f).height(48.dp)
            ) {
                Text("CHOOSE", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
            if (!canChoose) {
                Text(
                    "READING REQUIRED...",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, color: Color, pulse: Boolean = false) {
    val alpha by if (pulse) {
        rememberInfiniteTransition().animateFloat(
            0.4f, 1f, 
            infiniteRepeatable(tween(1000), RepeatMode.Reverse)
        )
    } else remember { mutableStateOf(1f) }

    Row(modifier = Modifier.fillMaxWidth().alpha(alpha)) {
        Text("$label: ", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
fun ConfirmationDialog(path: SingularityPath, onBack: () -> Unit, onConfirm: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .background(Color.Black)
            .border(1.dp, path.color, RoundedCornerShape(4.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("YOU HAVE CHOSEN:", color = Color.Gray, fontSize = 12.sp)
        Text(
            path.title, 
            color = path.color, 
            fontSize = 24.sp, 
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Text(
            "This cannot be undone.\nYour identity will be permanently overwritten.",
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(
                onClick = onBack,
                border = BorderStroke(1.dp, Color.Gray),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text("BACK", color = Color.Gray)
            }
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = path.color),
                shape = RoundedCornerShape(2.dp)
            ) {
                Text(
                    "CONFIRM", 
                    color = if (path.color == Color.White || path.color == ConvergenceGold) Color.Black else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
