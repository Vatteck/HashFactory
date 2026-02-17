package com.siliconsage.miner.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * CyberHeader v1.0
 * Renders text with a subtle chromatic aberration / ghosting effect.
 */
@Composable
fun CyberHeader(
    text: String,
    color: Color,
    fontSize: TextUnit = 18.sp,
    isGlitched: Boolean = false
) {
    Box {
        if (isGlitched) {
            // Cyan Ghost
            Text(
                text = text,
                color = Color.Cyan.copy(alpha = 0.5f),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.offset(x = (-1).dp, y = 0.dp).alpha(0.7f)
            )
            // Red Ghost
            Text(
                text = text,
                color = Color.Red.copy(alpha = 0.5f),
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.offset(x = 1.dp, y = 0.dp).alpha(0.7f)
            )
        }
        
        // Main Text
        Text(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
