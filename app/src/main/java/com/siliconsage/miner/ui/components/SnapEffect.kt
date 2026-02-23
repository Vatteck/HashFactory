package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.siliconsage.miner.viewmodel.GameViewModel

/**
 * SnapEffect — CRT reboot animation overlay.
 * Triggered by vm.snapTrigger changes.
 * 300ms total: 80ms white flash → 120ms black → 100ms fade-in.
 */
@Composable
fun SnapEffect(viewModel: GameViewModel) {
    val triggerTime by viewModel.snapTrigger.collectAsState()
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(triggerTime) {
        if (triggerTime == 0L) return@LaunchedEffect
        // Phase 1: White flash
        alpha.snapTo(1f)
        alpha.animateTo(1f, animationSpec = tween(80))
        // Phase 2: Black hold
        alpha.snapTo(-1f) // Negative = black mode
        kotlinx.coroutines.delay(120)
        // Phase 3: Fade out
        alpha.snapTo(0.8f)
        alpha.animateTo(0f, animationSpec = tween(100))
    }

    val currentAlpha = alpha.value
    if (currentAlpha != 0f) {
        val color = if (currentAlpha < 0f) {
            Color.Black.copy(alpha = 1f)
        } else {
            Color.White.copy(alpha = currentAlpha.coerceIn(0f, 1f))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color)
        )
    }
}
