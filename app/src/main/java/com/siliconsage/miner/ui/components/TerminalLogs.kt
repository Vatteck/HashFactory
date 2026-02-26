package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlin.random.Random

@Composable
fun TerminalLogs(viewModel: GameViewModel, primaryColor: Color, showCursor: Boolean) {
    val isPaused by viewModel.isSubnetPaused.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val subnetMessages by viewModel.subnetMessages.collectAsState()
    val mode by viewModel.activeTerminalMode.collectAsState()
    val currentHeat by viewModel.currentHeat.collectAsState()
    val isRaid by viewModel.isRaidActive.collectAsState()
    val listState = rememberLazyListState()

    val glitchOffset by viewModel.terminalGlitchOffset.collectAsState()
    val glitchAlpha by viewModel.terminalGlitchAlpha.collectAsState()
    val rawSicknessIntensity by viewModel.substrateStaticIntensity.collectAsState()
    val isCascadeDesync by viewModel.isCascadeDesync.collectAsState()

    val sicknessIntensity = if (isCascadeDesync) (rawSicknessIntensity * 2f).coerceAtMost(0.5f) else rawSicknessIntensity

    val corruption by viewModel.identityCorruption.collectAsState()
    val flopsRate by viewModel.flopsProductionRate.collectAsState()

    LaunchedEffect(logs.size, subnetMessages.size, mode) {
        if (!isPaused) {
            if (mode == "IO" && logs.isNotEmpty()) listState.scrollToItem(logs.size - 1)
            else if (mode == "SUBNET" && subnetMessages.isNotEmpty()) listState.scrollToItem(subnetMessages.size - 1)
        }
    }

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize().background(Color.Transparent)
            .graphicsLayer { translationX = glitchOffset; alpha = glitchAlpha }
            .drawBehind {
                if (isCascadeDesync) drawRect(Color.Red.copy(alpha = 0.03f))
                if (flopsRate > 1e6) {
                    val rainStep = 32.dp.toPx()
                    val rainColor = primaryColor.copy(alpha = 0.03f)
                    val speedVal = (kotlin.math.log10(flopsRate.coerceAtLeast(1.0)) / 5.0).coerceIn(1.0, 10.0)
                    val timeVal = (System.currentTimeMillis() % 1000000L).toDouble() / 1000.0
                    var xPos = 0f
                    while (xPos < this.size.width) {
                        val offset = (kotlin.math.sin(xPos.toDouble() * 0.732) * 1000.0)
                        val yBase = ((timeVal * 100.0 * speedVal + offset) % this.size.height.toDouble()).toFloat()
                        drawLine(color = rainColor, start = Offset(xPos, yBase), end = Offset(xPos, (yBase + 40.dp.toPx()).coerceAtMost(this.size.height)), strokeWidth = 1.dp.toPx())
                        xPos += rainStep
                    }
                }
                if (isCascadeDesync) {
                    val shimmerColor = Color.White.copy(alpha = 0.04f)
                    val time = (System.currentTimeMillis() % 2000L).toFloat()
                    val jitterBand = (time / 2000f * this.size.height) % this.size.height
                    drawRect(shimmerColor, topLeft = Offset(0f, jitterBand), size = Size(this.size.width, 4.dp.toPx()))
                }
            }
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "scanline")
        val scanlineOffset by infiniteTransition.animateFloat(
            initialValue = 0f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart),
            label = "scanlinePos"
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val y = scanlineOffset * size.height
            drawRect(color = primaryColor.copy(alpha = 0.05f), topLeft = Offset(0f, y), size = Size(size.width, 1.dp.toPx()))
        }

        val alphaState = infiniteTransition.animateFloat(initialValue = 0.02f, targetValue = 0.05f, animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse), label = "alpha")
        Text(
            text = "01101001 01110011 00100000 01100001 01101100 01101001 01110110 01100101 ".repeat(50),
            color = primaryColor.copy(alpha = alphaState.value),
            fontSize = 14.sp, fontFamily = FontFamily.Monospace, lineHeight = 18.sp, overflow = TextOverflow.Clip,
            modifier = Modifier.fillMaxSize().padding(4.dp)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            val process by viewModel.currentProcess.collectAsState()
            Text(
                text = "[PROCESS: $process]".uppercase(),
                color = primaryColor.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                if (mode == "IO") {
                    itemsIndexed(items = logs, key = { _, entry -> entry.id }) { index, entry ->
                        val displayMessage = if (isRaid && Random.nextFloat() > 0.7f) {
                            "0x" + Random.nextInt(0xDEADBC).toString(16).uppercase() + " // [CORRUPTED]"
                        } else entry.message

                        val reputation = viewModel.reputationTier.collectAsState().value
                        TerminalLogLine(
                            log = displayMessage,
                            isLast = index == logs.lastIndex,
                            primaryColor = primaryColor,
                            showCursor = showCursor,
                            reputationTier = reputation,
                            sicknessIntensity = sicknessIntensity,
                            timestamp = entry.timestamp
                        )
                    }
                } else if (mode == "SUBNET") {
                    itemsIndexed(items = subnetMessages, key = { _, message -> message.id }) { _, message ->
                        SubnetMessageLine(message, primaryColor, viewModel)
                    }
                    if (viewModel.isSubnetTyping.value) {
                        item {
                            val typingTexts = remember { listOf(
                                "≫ [HANDSHAKE_IN_PROGRESS...]",
                                "≫ [DECRYPTING_SIGNAL...]",
                                "≫ [BUFFER_SYNC...]",
                                "≫ [AWAITING_PACKET...]",
                                "≫ [RESOLVING_HANDLE...]",
                                "≫ [ROUTING_THROUGH_SUBNET...]"
                            ) }
                            val typingText = remember { typingTexts.random() }
                            Text(text = typingText, color = primaryColor.copy(alpha = 0.5f), fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp))
                        }
                    }
                } else if (mode == "SURVEILLANCE") {
                    item {
                        SurveillanceVisualizer(viewModel = viewModel, primaryColor = primaryColor)
                    }
                }
            }

            val cmdShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.5f), cmdShape).border(BorderStroke(1.dp, primaryColor.copy(alpha = 0.55f)), cmdShape)) {
                ActiveCommandBuffer(viewModel, primaryColor)
            }
            ManualComputeButton(viewModel, primaryColor)
        }
    }
}
