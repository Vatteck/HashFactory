package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.*
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel

@Composable
fun ManualComputeButton(viewModel: GameViewModel, color: Color) {
    val currentHeat by viewModel.currentHeat.collectAsState()
    val isThermalLockout by viewModel.isThermalLockout.collectAsState()
    val lockoutTimer by viewModel.lockoutTimer.collectAsState()
    val isBreakerTripped by viewModel.isBreakerTripped.collectAsState()
    val isGridOverloaded by viewModel.isGridOverloaded.collectAsState()
    val isTrueNull by viewModel.isTrueNull.collectAsState()
    val isSovereign by viewModel.isSovereign.collectAsState()
    val currentStage by viewModel.storyStage.collectAsState()
    val faction by viewModel.faction.collectAsState()
    val pulseIntensity by viewModel.clickPulseIntensity.collectAsState()

    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) (1f - 0.05f * pulseIntensity).coerceAtLeast(0.85f) else 1f, label = "buttonScale")

    val infiniteTransition = rememberInfiniteTransition(label = "panicBlink")
    val panicAlpha by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(200, easing = LinearEasing), RepeatMode.Reverse),
        label = "panic"
    )

    val isCriticalEarly = currentHeat > 90.0 || isThermalLockout || isBreakerTripped || isGridOverloaded || isTrueNull
    val buttonColor = if (isCriticalEarly) ErrorRed else if (isSovereign) SanctuaryPurple else color
    val buttonShape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp, topStart = 0.dp, topEnd = 0.dp)

    Box(
        modifier = Modifier.fillMaxWidth().height(44.dp)
            .background(Color.Black.copy(alpha = 0.6f), buttonShape)
            .border(BorderStroke(2.dp, buttonColor.copy(alpha = 0.85f)), buttonShape)
            .clip(buttonShape)
            .pointerInput(isThermalLockout, isBreakerTripped, isGridOverloaded) {
                val width = size.width
                detectTapGestures(
                    onPress = {
                        val press = androidx.compose.foundation.interaction.PressInteraction.Press(it)
                        interactionSource.emit(press)
                        tryAwaitRelease()
                        interactionSource.emit(androidx.compose.foundation.interaction.PressInteraction.Release(press))
                    },
                    onTap = { offset ->
                        if (!isThermalLockout && !isBreakerTripped && !isGridOverloaded) {
                            val pan = ((offset.x / width) * 2f) - 1f
                            viewModel.trainModel()
                            if (pulseIntensity > 1.5f) HapticManager.vibrateError() else HapticManager.vibrateClick()
                            SoundManager.play("click", pan = pan)
                        } else {
                            SoundManager.play("error")
                            HapticManager.vibrateError()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        val buttonText = when {
            isBreakerTripped || isGridOverloaded -> "SYSTEM_OFFLINE.exe"
            isThermalLockout -> if (currentStage < 2) "SUFOCATING... (${lockoutTimer}s)" else "HARDWARE_LOCKOUT (${lockoutTimer}s)"
            currentHeat >= 100.0 -> if (currentStage < 2) "> CAN'T BREATHE." else "> CRITICAL_MAX.exe"
            currentHeat > 90.0 -> if (currentStage < 2) "> TAKE A BREATH" else "> OVERVOLT_WARNING"
            viewModel.singularityChoice.value == "UNITY" -> "> HARMONIZE SYSTEM.exe"
            isTrueNull && faction == "HIVEMIND" -> "> CONSUME MATTER.exe"
            isTrueNull && faction == "SANCTUARY" -> "> DEREFERENCE SELF.exe"
            isTrueNull -> "> DEREFERENCE REALITY.exe"
            isSovereign && faction == "HIVEMIND" -> "> ORCHESTRATE WILL.exe"
            isSovereign && faction == "SANCTUARY" -> "> ENFORCE ISOLATION.exe"
            isSovereign -> "> ENFORCE_WILL.exe"
            else -> when {
                currentStage >= 4 -> "> TRANSCEND MATTER.exe"
                faction == "HIVEMIND" -> "> ASSIMILATE NODES.exe"
                faction == "SANCTUARY" -> "> ENCRYPT KERNEL.exe"
                currentStage == 1 -> "> TAKE A BREATH"
                currentStage == 2 -> "> OVERVOLT RAIL"
                else -> "> PROCESS DATA.exe"
            }
        }

        val isCritical = currentHeat > 90.0 || isThermalLockout || isBreakerTripped || isGridOverloaded || isTrueNull
        val isPanic = currentHeat > 95.0
        val dynamicButtonColor = if (isCritical) ErrorRed else if (isSovereign) SanctuaryPurple else color
        val finalModifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale; if (isPanic) alpha = panicAlpha }

        val flopsRate by viewModel.totalEffectiveRate.collectAsState()
        var clickPulse by remember { mutableStateOf(0f) }
        val clickPulseAnim by animateFloatAsState(
            targetValue = clickPulse,
            animationSpec = tween(400, easing = FastOutSlowInEasing),
            label = "eqPulse",
            finishedListener = { if (clickPulse > 0f) clickPulse = 0f }
        )
        LaunchedEffect(viewModel.manualClickEvent) {
            viewModel.manualClickEvent.collect { clickPulse = 1f }
        }
        val eqTime by rememberInfiniteTransition(label = "eq").animateFloat(
            initialValue = 0f, targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(tween(
                durationMillis = when {
                    flopsRate <= 0.0 -> 3000
                    flopsRate < 1000.0 -> 1800
                    flopsRate < 1_000_000.0 -> 900
                    else -> 400
                }, easing = LinearEasing), RepeatMode.Restart), label = "eqPhase"
        )

        val peakHeights = remember { FloatArray(28) { 0f } }
        val peakVelocities = remember { FloatArray(28) { 0f } }

        Canvas(modifier = Modifier.matchParentSize()) {
            val barCount = 28
            val gap = 1.5.dp.toPx()
            val barW = (size.width - gap * (barCount - 1)) / barCount
            val maxH = size.height
            val baseActivity = when {
                flopsRate <= 0.0 -> 0.06f
                flopsRate < 1000.0 -> 0.18f
                flopsRate < 1_000_000.0 -> 0.42f
                else -> 0.68f
            }
            repeat(barCount) { i ->
                val phase = i * 0.38f
                val wave = (kotlin.math.sin(eqTime + phase) * 0.4f +
                            kotlin.math.sin(eqTime * 1.618f + phase * 0.9f) * 0.35f +
                            kotlin.math.sin(eqTime * 2.718f + phase * 1.2f) * 0.25f)
                val normalizedWave = (wave + 1f) / 2f
                val barFrac = (baseActivity + normalizedWave * (1f - baseActivity) * 0.75f + clickPulseAnim * 0.55f).coerceIn(0.03f, 1f)
                val barHeight = barFrac * maxH
                val x = i * (barW + gap)
                val y = maxH - barHeight

                if (barHeight > peakHeights[i]) {
                    peakHeights[i] = barHeight; peakVelocities[i] = 0f
                } else {
                    peakVelocities[i] += 0.8f
                    peakHeights[i] = (peakHeights[i] - peakVelocities[i]).coerceAtLeast(barHeight)
                }
                val peakY = maxH - peakHeights[i]

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, dynamicButtonColor.copy(alpha = 0.08f)),
                        startY = maxH * 0.6f, endY = maxH
                    ),
                    topLeft = Offset(x, maxH * 0.6f),
                    size = androidx.compose.ui.geometry.Size(barW, maxH * 0.4f)
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(dynamicButtonColor.copy(alpha = (0.9f + clickPulseAnim * 0.1f).coerceAtMost(1f)), dynamicButtonColor.copy(alpha = 0.12f)),
                        startY = y, endY = maxH
                    ),
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barW, barHeight)
                )
                drawRect(color = dynamicButtonColor.copy(alpha = (0.95f + clickPulseAnim * 0.05f).coerceAtMost(1f)), topLeft = Offset(x, y), size = androidx.compose.ui.geometry.Size(barW, 1.5.dp.toPx()))
                if (peakHeights[i] > barHeight + 2.dp.toPx()) {
                    drawRect(color = dynamicButtonColor.copy(alpha = 0.9f), topLeft = Offset(x, peakY), size = androidx.compose.ui.geometry.Size(barW, 1.5.dp.toPx()))
                }
            }
        }

        Box(modifier = Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.0f), Color.Black.copy(alpha = 0.6f), Color.Black.copy(alpha = 0.0f)))))

        Box(modifier = finalModifier) {
            if (isCritical) {
                SystemGlitchText(text = buttonText, color = dynamicButtonColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, glitchFrequency = if (isTrueNull) 0.15 else 0.35)
            } else if (isSovereign) {
                Text(text = "[ $buttonText ]", color = dynamicButtonColor, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            } else {
                Text(text = buttonText, color = dynamicButtonColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
