package com.siliconsage.miner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siliconsage.miner.util.SocialManager

@Composable
fun SubnetMessageLine(message: SocialManager.SubnetMessage, color: Color) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row {
            Text(
                text = message.handle,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = ">>",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = message.content,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
