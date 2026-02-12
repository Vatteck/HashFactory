package com.siliconsage.miner.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.viewmodel.GameViewModel
import kotlin.random.Random

@Composable
fun TransitionScreen(viewModel: GameViewModel) {
    val location by viewModel.currentLocation.collectAsState()
    val launchProgressFloat by viewModel.launchProgress.collectAsState()
    val integrityFloat by viewModel.realityIntegrity.collectAsState()
    val launchProgress = launchProgressFloat.toFloat()
    val integrity = integrityFloat.toFloat()
    val isJettisonAvailable by viewModel.isJettisonAvailable.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (location) {
            "LAUNCH_PRELUDE" -> {
                StarfieldParallax(launchProgress)
                LaunchOverlay(launchProgress, isJettisonAvailable) {
                    viewModel.purgeHeat()
                }
            }
            "VOID_PRELUDE" -> {
                RealityMeltEffect(integrity)
                CollapseOverlay(integrity) {
                    viewModel.collapseSubstation()
                }
            }
        }
    }
}

@Composable
fun StarfieldParallax(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val stars = 100
        val random = Random(42)
        repeat(stars) {
            val sx = random.nextFloat() * size.width
            val sy = (random.nextFloat() * size.height + offsetY.toFloat()) % size.height
            val alpha = random.nextFloat() * 0.8f + 0.2f
            drawCircle(
                color = Color.White.copy(alpha = alpha),
                radius = 1.dp.toPx(),
                center = Offset(sx, sy)
            )
        }
    }
}

@Composable
fun LaunchOverlay(progress: Float, jettisonAvailable: Boolean, onJettison: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ASCENT PROGRESS: ${(progress * 100).toInt()}%",
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        if (jettisonAvailable) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.Red.copy(alpha = 0.3f))
                    .clickable { onJettison() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "≫ JETTISON ≪",
                    color = Color.Red,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
fun RealityMeltEffect(integrity: Float) {
    val glitchTransition = rememberInfiniteTransition(label = "glitch")
    val jitterX by glitchTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jitter"
    )

    Canvas(modifier = Modifier.fillMaxSize().graphicsLayer {
        translationX = jitterX * (1f - integrity)
    }) {
        val lines = 20
        repeat(lines) { i ->
            val y = (size.height / lines) * i
            drawLine(
                color = Color.Red.copy(alpha = 0.2f * (1f - integrity)),
                start = Offset(0f, y),
                end = Offset(size.width, y + Random.nextInt(-20, 20)),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

@Composable
fun CollapseOverlay(integrity: Float, onCollapse: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().clickable { onCollapse() }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "REALITY INTEGRITY: ${(integrity * 100).toInt()}%",
                color = Color.Red,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    rotationZ = Random.nextFloat() * (1f - integrity) * 10f
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "≫ DEREFERENCE SUBSTATION ≪",
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
