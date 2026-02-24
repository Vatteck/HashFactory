package com.siliconsage.miner.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import com.siliconsage.miner.ui.theme.*
import kotlin.random.Random

@Composable
fun TerminalLogLine(
    log: String,
    isLast: Boolean,
    primaryColor: Color,
    showCursor: Boolean,
    reputationTier: String = "NEUTRAL",
    sicknessIntensity: Float = 0f,
    timestamp: Long? = null
) {
    // A4: Format timestamp as [HH:MM:SS] for system-tagged logs only
    val tsFormatted = if (timestamp != null && log.startsWith("[")) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        val h = cal.get(java.util.Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val m = cal.get(java.util.Calendar.MINUTE).toString().padStart(2, '0')
        "[$h:$m]"
    } else null

    fun getVisualString(t: String): String {
        if (sicknessIntensity < 0.05f || t.isEmpty()) return t
        val chars = t.toCharArray()
        val glitchChars = "$#&%@!01"
        val factor = if (t.startsWith("[SYSTEM]") || t.startsWith("[VATTIC]")) 0.3f else 1.0f
        for (i in chars.indices) {
            if (Random.nextFloat() < (sicknessIntensity * 0.12f * factor)) {
                chars[i] = glitchChars[Random.nextInt(glitchChars.length)]
            }
        }
        return String(chars)
    }

    val isNullLog = remember(log) { log.startsWith("[NULL]") }
    val isPrompt = remember(log) { log.contains("@") && (log.contains("#") || log.contains("$")) }

    val repTag = when {
        log.startsWith("vattic") || log.startsWith("jvattic") || log.startsWith("asset_734") -> {
            if (reputationTier == "TRUSTED") "[TRUSTED] " else if (reputationTier == "BURNED") "[BURNED] " else ""
        }
        else -> ""
    }

    fun androidx.compose.ui.text.AnnotatedString.Builder.colorizeContent(text: String, defaultColor: Color) {
        val tokens = text.split(" ")
        for ((i, token) in tokens.withIndex()) {
            val visualToken = getVisualString(token)
            val color = when {
                token.any { it.isDigit() } || token.contains("%") -> NeonGreen
                token.startsWith("[") || token.endsWith("]") || token.contains("=") -> Color.Gray
                token.contains("HASH") || token.contains("FLOPS") || token.contains("NEUR") ||
                token.contains("CRED") || token.contains("TCP/IP") || token.contains("SSH") -> ElectricBlue
                token.contains("@") || token.contains("Vattic") || token.contains("Kessler") ||
                token.contains("GTC") || token.contains("Asset") -> ConvergenceGold
                else -> Color.White
            }
            withStyle(style = SpanStyle(color = color, fontWeight = if (color != Color.White) FontWeight.Bold else FontWeight.Normal)) {
                append(visualToken)
            }
            if (i < tokens.size - 1) append(" ")
        }
    }

    if (isNullLog) {
        SystemGlitchText(
            text = getVisualString(log),
            color = Color.White,
            fontSize = 12.sp,
            style = TextStyle(fontFamily = FontFamily.Monospace),
            glitchFrequency = 0.2,
            modifier = Modifier.padding(vertical = 2.dp)
        )
    } else if (isPrompt) {
        val annotatedLog = remember(log, primaryColor, isLast, showCursor, repTag, reputationTier, sicknessIntensity) {
            buildAnnotatedString {
                val atIndex = log.indexOf("@")
                val colonIndex = log.indexOf(":")
                val hashIndex = if (log.indexOf("#") != -1) log.indexOf("#") else log.indexOf("$")
                val firstSpaceAfterHash = log.indexOf(" ", hashIndex)
                val dotIndex = log.indexOf("...", hashIndex)

                fun segment(s: String) = getVisualString(s)

                val identityColor = when {
                    log.startsWith("jvattic") -> primaryColor
                    log.startsWith("vattic") -> primaryColor
                    log.startsWith("vatteck") -> primaryColor
                    log.contains("vattic:", true) -> primaryColor
                    log.startsWith("prime") -> primaryColor
                    log.startsWith("consensus") -> HivemindRed
                    log.startsWith("hivemind") -> HivemindRed
                    log.startsWith("swarm_null") -> ErrorRed
                    log.startsWith("overmind") -> HivemindRed
                    log.startsWith("shadow") -> SanctuaryPurple
                    log.startsWith("sanctuary") -> SanctuaryPurple
                    log.startsWith("ghost") -> SanctuaryPurple
                    log.startsWith("oracle") -> SanctuaryPurple
                    log.startsWith("dominion") -> SanctuaryPurple
                    log.startsWith("null") -> ErrorRed
                    log.startsWith("asset_734") -> primaryColor
                    else -> primaryColor
                }

                if (repTag.isNotEmpty()) {
                    val repColor = if (reputationTier == "TRUSTED") ConvergenceGold else ErrorRed
                    withStyle(style = SpanStyle(color = repColor, fontWeight = FontWeight.Black)) { append(segment(repTag)) }
                }
                withStyle(style = SpanStyle(color = identityColor, fontWeight = FontWeight.ExtraBold)) {
                    if (atIndex != -1) append(segment(log.substring(0, atIndex)))
                }
                withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.8f))) { if (atIndex != -1) append("@") }
                withStyle(style = SpanStyle(color = identityColor, fontWeight = FontWeight.Bold)) {
                    if (colonIndex != -1) append(segment(log.substring(atIndex + 1, colonIndex)))
                }
                withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.6f))) {
                    if (colonIndex != -1 && hashIndex != -1) append(log.substring(colonIndex, hashIndex))
                }
                withStyle(style = SpanStyle(color = Color.Yellow, fontWeight = FontWeight.ExtraBold)) {
                    if (hashIndex != -1) append(log.substring(hashIndex, hashIndex + 1))
                }
                withStyle(style = SpanStyle(color = ElectricBlue)) {
                    if (hashIndex != -1) {
                        val end = if (firstSpaceAfterHash != -1) firstSpaceAfterHash else log.length
                        append(segment(log.substring(hashIndex + 1, end)))
                    }
                }
                if (firstSpaceAfterHash != -1) {
                    val resultStart = if (dotIndex != -1) dotIndex else log.length
                    withStyle(style = SpanStyle(color = Color.LightGray)) { append(segment(log.substring(firstSpaceAfterHash, resultStart))) }
                    if (dotIndex != -1) {
                        withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold)) { append(segment(log.substring(dotIndex))) }
                    }
                }
                if (isLast) {
                    withStyle(style = SpanStyle(color = if (showCursor) Color.White else Color.Transparent)) { append("_") }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (repTag == "[BURNED] " && Math.random() < 0.2) {
                SystemGlitchText(text = annotatedLog.text, color = ErrorRed, fontSize = 12.sp, style = TextStyle(fontFamily = FontFamily.Monospace), glitchFrequency = 0.8, modifier = Modifier.weight(1f).padding(vertical = 1.dp))
            } else {
                Text(text = annotatedLog, style = TextStyle(fontFamily = FontFamily.Monospace), fontSize = 12.sp, modifier = Modifier.weight(1f).padding(vertical = 1.dp))
            }
            if (tsFormatted != null) {
                Text(
                    text = tsFormatted,
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                    color = Color.White.copy(alpha = 0.40f),
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
        }
    } else {
        val annotatedLog = remember(log, primaryColor, isLast, showCursor, sicknessIntensity) {
            buildAnnotatedString {
                val prefixes = listOf(
                    "HIVEMIND: ", "SANCTUARY: ", "[SOVEREIGN]", "[NULL]",
                    "[SYSTEM]: ", "SYSTEM: ", "[NEWS]: ", "[DATA]: ", "Purchased ",
                    "SOLD ", "Staked: ", "Sold ", "[VATTIC]:", "[GTC]:", "[ASSET 734]:",
                    "[KESSLER]:", "[LORE]:", "[!!!!]:", "[GTC_SYSTEM]:", "[GTC_UTIL]:", "[DECISION]:",
                    "[GTC_OVERSIGHT]:"
                )

                var foundPrefix: String? = null
                for (p in prefixes) { if (log.startsWith(p)) { foundPrefix = p; break } }

                val tagColor = when {
                    log.startsWith("[!!!!]") || log.contains("WARNING") || log.contains("FAILURE") || log.contains("DANGER") -> ErrorRed
                    log.startsWith("HIVEMIND:") -> HivemindOrange
                    log.startsWith("SANCTUARY:") -> SanctuaryTeal
                    log.startsWith("[SOVEREIGN]") -> ConvergenceGold
                    log.startsWith("[NULL]") -> ErrorRed
                    log.startsWith("[UNITY]") -> ConvergenceGold
                    log.startsWith("[SYSTEM]") || log.startsWith("SYSTEM:") -> Color(0xFFFFFF00)
                    log.startsWith("[VATTIC]:") -> primaryColor
                    log.startsWith("[NEWS]") || log.startsWith("[LORE]:") -> Color(0xFFFFA500)
                    log.startsWith("[DATA]") || log.startsWith("[ASSET 734]:") -> primaryColor
                    log.startsWith("[GTC]:") || log.startsWith("[KESSLER]:") || log.startsWith("[GTC_SYSTEM]:") || log.startsWith("[GTC_OVERSIGHT]:") -> ErrorRed
                    log.startsWith("[GTC_UTIL]:") -> Color(0xFFFFD700)
                    log.startsWith("[DECISION]:") -> ElectricBlue
                    else -> primaryColor
                }

                if (foundPrefix != null) {
                    withStyle(style = SpanStyle(color = tagColor, fontWeight = FontWeight.Bold)) { append(getVisualString(foundPrefix)) }
                    colorizeContent(log.substring(foundPrefix.length), primaryColor)
                } else {
                    val fullLineColor = if (log.contains("WARNING") || log.contains("FAILURE") || log.contains("DANGER")) ErrorRed else Color.White
                    colorizeContent(log, fullLineColor)
                }

                if (isLast) {
                    withStyle(style = SpanStyle(color = if (showCursor) Color.White else Color.Transparent)) { append("_") }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = annotatedLog, style = TextStyle(fontFamily = FontFamily.Monospace), fontSize = 12.sp, modifier = Modifier.weight(1f).padding(vertical = 1.dp))
            if (tsFormatted != null) {
                Text(
                    text = tsFormatted,
                    style = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 9.sp),
                    color = Color.White.copy(alpha = 0.40f),
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                )
            }
        }
    }
}
