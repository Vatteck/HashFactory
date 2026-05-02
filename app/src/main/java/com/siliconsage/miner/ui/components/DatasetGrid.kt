package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.ui.theme.ErrorRed
import com.siliconsage.miner.ui.theme.NeonGreen
import com.siliconsage.miner.util.FormatUtils
import com.siliconsage.miner.util.HapticManager
import com.siliconsage.miner.util.SoundManager
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun DatasetGrid(viewModel: GameViewModel, primaryColor: Color) {
    val activeDataset by viewModel.activeDataset.collectAsState()
    val activeNodes by viewModel.activeDatasetNodes.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "gridIdle")
    val idleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
        label = "idlePulse"
    )

    // Track tap flash animations per node
    val flashTimestamps = remember { mutableStateMapOf<Int, Long>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
            .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        if (activeDataset != null && activeNodes.isNotEmpty()) {
            val ds = activeDataset!!
            val validTotal = activeNodes.count { it.isValid }
            val harvested = activeNodes.count { it.isHarvested }
            val corrupted = activeNodes.count { it.isCorruptTapped }

            // Compact header — dataset name, progress, stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "[ ${ds.name} ]",
                    color = primaryColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${harvested}/${validTotal}",
                        color = NeonGreen.copy(alpha = 0.9f),
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                    if (corrupted > 0) {
                        Text(
                            text = " ${corrupted}✕",
                            color = ErrorRed.copy(alpha = 0.9f),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${(ds.progress * 100).toInt()}%",
                        color = primaryColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Dynamic grid — compute columns based on available space and node count
            var gridSize by remember { mutableStateOf(IntSize.Zero) }
            val density = LocalDensity.current

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { gridSize = it }
            ) {
                if (gridSize.width > 0 && gridSize.height > 0) {
                    val nodeCount = activeNodes.size
                    val gapPx = with(density) { 3.dp.toPx() }

                    // Calculate optimal columns to fill the space without scrolling
                    // Try different column counts and pick the one that best fills the area
                    val availW = gridSize.width.toFloat()
                    val availH = gridSize.height.toFloat()

                    var bestCols = 1
                    var bestCellSize = 0f

                    for (cols in 1..nodeCount) {
                        val rows = kotlin.math.ceil(nodeCount.toFloat() / cols).toInt()
                        val cellW = (availW - gapPx * (cols - 1)) / cols
                        val cellH = (availH - gapPx * (rows - 1)) / rows
                        val cellSize = min(cellW, cellH)
                        if (cellSize < with(density) { 20.dp.toPx() }) break // minimum tap target
                        if (cellSize > bestCellSize) {
                            bestCellSize = cellSize
                            bestCols = cols
                        }
                    }

                    val columns = bestCols
                    val rows = kotlin.math.ceil(nodeCount.toFloat() / columns).toInt()
                    val cellSizePx = min(
                        (availW - gapPx * (columns - 1)) / columns,
                        (availH - gapPx * (rows - 1)) / rows
                    )
                    val cellSizeDp = with(density) { cellSizePx.toDp() }
                    val gapDp = 3.dp

                    // Center the grid in available space
                    val totalGridW = with(density) { (cellSizePx * columns + gapPx * (columns - 1)).toDp() }
                    val totalGridH = with(density) { (cellSizePx * rows + gapPx * (rows - 1)).toDp() }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(gapDp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (row in 0 until rows) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(gapDp)
                                ) {
                                    for (col in 0 until columns) {
                                        val idx = row * columns + col
                                        if (idx < nodeCount) {
                                            val node = activeNodes[idx]
                                            DatasetNodeCell(
                                                node = node,
                                                primaryColor = primaryColor,
                                                cellSize = cellSizeDp,
                                                flashTimestamp = flashTimestamps[node.id],
                                                onTap = {
                                                    if (!node.isHarvested && !node.isCorruptTapped) {
                                                        flashTimestamps[node.id] = System.currentTimeMillis()
                                                        viewModel.tapDatasetNode(node.id)
                                                    }
                                                }
                                            )
                                        } else {
                                            // Empty cell placeholder for incomplete rows
                                            Spacer(modifier = Modifier.size(cellSizeDp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Compact footer — purity + yield info
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PUR: ${(ds.purity * 100).toInt()}%",
                    color = Color.Gray,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "OUTPUT: ≈${FormatUtils.formatLargeNumber(ds.expectedYield)} \$FLOPS",
                    color = NeonGreen.copy(alpha = 0.7f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

        } else {
            // Empty / Idle State
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Scanline idle effect
                    val scanOffset by infiniteTransition.animateFloat(
                        initialValue = 0f, targetValue = 1f,
                        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
                        label = "idleScan"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(120.dp)
                            .border(1.dp, primaryColor.copy(alpha = idleAlpha * 0.3f), RoundedCornerShape(4.dp))
                            .drawBehind {
                                // Dim scanline sweep
                                val y = scanOffset * size.height
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, primaryColor.copy(alpha = 0.06f), Color.Transparent),
                                        startY = y - 30f,
                                        endY = y + 30f
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "NO DATASET LOADED",
                                color = primaryColor.copy(alpha = idleAlpha),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "AWAITING DATA BLOCK",
                                color = primaryColor.copy(alpha = idleAlpha * 0.5f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clickable { viewModel.toggleDatasetPicker() }
                            .border(1.dp, primaryColor, RoundedCornerShape(4.dp))
                            .background(primaryColor.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "▼ ACQUIRE DATASET",
                            color = primaryColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DatasetNodeCell(
    node: com.siliconsage.miner.data.DatasetNode,
    primaryColor: Color,
    cellSize: androidx.compose.ui.unit.Dp,
    flashTimestamp: Long?,
    onTap: () -> Unit
) {
    // Flash animation — brief bright glow on tap
    val now = remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(flashTimestamp) {
        if (flashTimestamp != null) {
            now.value = System.currentTimeMillis()
            kotlinx.coroutines.delay(300)
            now.value = System.currentTimeMillis()
        }
    }
    val flashAge = if (flashTimestamp != null) (now.value - flashTimestamp).coerceAtLeast(0) else 1000L
    val flashAlpha = if (flashAge < 300) (1f - flashAge / 300f) * 0.6f else 0f

    val baseColor = when {
        node.isHarvested -> NeonGreen
        node.isCorruptTapped -> ErrorRed
        node.isValid -> primaryColor
        else -> primaryColor // corrupt nodes look the same as valid (unknown until tapped)
    }

    val bgColor = when {
        node.isHarvested -> NeonGreen.copy(alpha = 0.25f)
        node.isCorruptTapped -> ErrorRed.copy(alpha = 0.7f)
        else -> Color.Transparent
    }

    val borderColor = when {
        node.isHarvested -> NeonGreen.copy(alpha = 0.15f)
        node.isCorruptTapped -> ErrorRed.copy(alpha = 0.4f)
        else -> primaryColor.copy(alpha = 0.6f)
    }

    // Scale text based on cell size
    val textSize = when {
        cellSize >= 48.dp -> 11.sp
        cellSize >= 36.dp -> 9.sp
        else -> 7.sp
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .background(bgColor, RoundedCornerShape(3.dp))
            .border(1.dp, borderColor, RoundedCornerShape(3.dp))
            .then(
                if (flashAlpha > 0f) {
                    val flashColor = if (node.isCorruptTapped) ErrorRed else NeonGreen
                    Modifier.drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    flashColor.copy(alpha = flashAlpha),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2, size.height / 2),
                                radius = size.width * 0.8f
                            )
                        )
                    }
                } else Modifier
            )
            .clickable(enabled = !node.isHarvested && !node.isCorruptTapped) { onTap() },
        contentAlignment = Alignment.Center
    ) {
        when {
            node.isHarvested -> {
                // Harvested: checkmark / dim glyph
                Text(
                    text = "✓",
                    color = NeonGreen.copy(alpha = 0.5f),
                    fontSize = textSize,
                    fontWeight = FontWeight.Bold
                )
            }
            node.isCorruptTapped -> {
                Text(
                    text = "ERR",
                    color = Color.White,
                    fontSize = textSize,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
            else -> {
                // Untapped node — show hex address
                Text(
                    text = "0x" + node.id.toString(16).uppercase().padStart(2, '0'),
                    color = primaryColor.copy(alpha = 0.8f),
                    fontSize = textSize,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
