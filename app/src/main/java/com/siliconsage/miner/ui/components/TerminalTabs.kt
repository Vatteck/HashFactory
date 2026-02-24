package com.siliconsage.miner.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.siliconsage.miner.ui.theme.ElectricBlue
import com.siliconsage.miner.ui.theme.ErrorRed

@Composable
fun TerminalTabButton(
    text: String,
    active: Boolean,
    hasNew: Boolean,
    color: Color,
    isChoicePending: Boolean,
    isPaused: Boolean = false,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TabGlow")

    val flashAlpha by infiniteTransition.animateFloat(
        0.2f, 1f,
        infiniteRepeatable(tween(if (isChoicePending) 300 else 800), RepeatMode.Reverse),
        label = "glow"
    )

    val alertColor = when {
        isChoicePending -> ErrorRed
        text == "SUBNET" && hasNew -> ElectricBlue
        else -> color
    }

    Column(modifier = Modifier.clickable { onClick() }.padding(vertical = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = text,
            color = if (active) color else alertColor.copy(alpha = if (hasNew || isChoicePending) flashAlpha else 0.4f),
            fontSize = 11.sp,
            fontWeight = if (active || hasNew || isChoicePending) FontWeight.ExtraBold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        )
        if ((hasNew || isChoicePending) && !active) {
            Box(modifier = Modifier.padding(top = 1.dp).width(20.dp).height(2.dp).background(alertColor.copy(alpha = flashAlpha)))
        } else if (active) {
            Box(modifier = Modifier.padding(top = 1.dp).width(16.dp).height(1.dp).background(color))
        }
    }
}

@Composable
fun TerminalTab(
    label: String,
    active: Boolean,
    hasFlash: Boolean,
    color: Color,
    corruption: Double,
    modifier: Modifier = Modifier,
    isDecision: Boolean = false,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tab_flash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha"
    )

    val tabColor = when {
        isDecision && !active -> ErrorRed
        active -> color
        else -> Color.Gray
    }

    Box(
        modifier = modifier
            .background(
                if (active) color.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.1f),
                RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
            )
            .border(
                BorderStroke(1.5.dp, if (active) tabColor else Color.DarkGray.copy(alpha = 0.65f)),
                RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        CyberHeader(
            text = label,
            color = if (active) tabColor else tabColor.copy(alpha = if (hasFlash) flashAlpha else 0.4f),
            fontSize = 10.sp,
            isGlitched = active && corruption > 0.4
        )
    }
}
