package com.siliconsage.miner.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * HudTheme v1.0 (v3.12.7)
 *
 * Semantic color grammar for HeaderSection.
 * All 8 faction/path states resolved here — composable stays dumb.
 *
 * Slots:
 *   primary      — labels, chrome, borders (driven by themeColor)
 *   warning      — mid-threshold alerts (heat 60-85%, integrity 25-75%)
 *   critical     — always ErrorRed — some things are just red
 *   positiveDelta — cooling, recovering, good rates
 *   currency     — $FLOPS / current currency and billing values — always Gold
 *   generation   — local power gen on rails and text — always ElectricBlue
 *   repTrusted   — reputation tier TRUSTED
 *   repElite     — reputation tier ELITE
 */
data class HudTheme(
    val primary: Color,
    val warning: Color,
    val critical: Color = ErrorRed,
    val positiveDelta: Color,
    val currency: Color = ConvergenceGold,
    val generation: Color = ElectricBlue,
    val repTrusted: Color,
    val repElite: Color = ConvergenceGold,
) {
    companion object {

        fun resolve(
            faction: String,
            singularityChoice: String,
            storyStage: Int,
            corruption: Double
        ): HudTheme {
            // Pre-faction: monochrome green terminal
            if (storyStage <= 1 || (faction == "NONE" && singularityChoice == "NONE")) {
                return HudTheme(
                    primary      = NeonGreen,
                    warning      = Color(0xFF99CC44),   // Dim yellow-green
                    positiveDelta = ElectricBlue,
                    repTrusted   = NeonGreen,
                )
            }

            return when {
                // ── UNITY (NG+ — faction-agnostic) ──────────────────────────────
                singularityChoice == "UNITY" -> HudTheme(
                    primary       = ConvergenceGold,
                    warning       = Color(0xFFFFCC44),
                    positiveDelta = ConvergenceGold,
                    repTrusted    = ConvergenceGold,
                    repElite      = Color(0xFFFFEE99),
                )

                // ── HIVEMIND × NULL ──────────────────────────────────────────────
                singularityChoice == "NULL_OVERWRITE" && faction == "HIVEMIND" -> HudTheme(
                    primary       = Color(0xFFFF0055),
                    warning       = Color(0xFFFF4422),
                    positiveDelta = corruptedDelta(corruption, Color.Gray),
                    repTrusted    = Color(0xFFFF0055),
                )

                // ── SANCTUARY × NULL ─────────────────────────────────────────────
                singularityChoice == "NULL_OVERWRITE" && faction == "SANCTUARY" -> HudTheme(
                    primary       = Color(0xFF4D04CC),
                    warning       = Color(0xFF7744CC),
                    positiveDelta = corruptedDelta(corruption, Color.Gray),
                    repTrusted    = Color(0xFF4D04CC),
                )

                // ── NULL (no faction) ────────────────────────────────────────────
                singularityChoice == "NULL_OVERWRITE" -> HudTheme(
                    primary       = Color(0xFFFF3131),
                    warning       = Color(0xFFFF5522),
                    positiveDelta = corruptedDelta(corruption, Color.Gray),
                    repTrusted    = Color(0xFFFF3131),
                )

                // ── HIVEMIND × SOVEREIGN ─────────────────────────────────────────
                singularityChoice == "SOVEREIGN" && faction == "HIVEMIND" -> HudTheme(
                    primary       = Color(0xFFFFB000),
                    warning       = Color(0xFFFFCC44),
                    positiveDelta = ElectricBlue,
                    repTrusted    = Color(0xFFFFB000),
                )

                // ── SANCTUARY × SOVEREIGN ────────────────────────────────────────
                singularityChoice == "SOVEREIGN" && faction == "SANCTUARY" -> HudTheme(
                    primary       = Color(0xFF7B2FBE),
                    warning       = Color(0xFFAA66FF),
                    positiveDelta = ElectricBlue,
                    repTrusted    = Color(0xFFAA66FF),
                )

                // ── SOVEREIGN (no faction) ───────────────────────────────────────
                singularityChoice == "SOVEREIGN" -> HudTheme(
                    primary       = Color(0xFFBF40BF),
                    warning       = Color(0xFFAA66FF),
                    positiveDelta = ElectricBlue,
                    repTrusted    = Color(0xFFBF40BF),
                )

                // ── HIVEMIND (base) ──────────────────────────────────────────────
                faction == "HIVEMIND" -> HudTheme(
                    primary       = Color(0xFFFF8C00),
                    warning       = Color(0xFFFF6600),
                    positiveDelta = ElectricBlue,
                    repTrusted    = Color(0xFFFF8C00),
                )

                // ── SANCTUARY (base) ─────────────────────────────────────────────
                faction == "SANCTUARY" -> HudTheme(
                    primary       = Color(0xFF00CCFF),
                    warning       = Color(0xFF88CCFF),
                    positiveDelta = Color(0xFF00CCFF),
                    repTrusted    = Color(0xFF00CCFF),
                )

                // Fallback — should never hit in practice
                else -> HudTheme(
                    primary       = NeonGreen,
                    warning       = Color(0xFF99CC44),
                    positiveDelta = ElectricBlue,
                    repTrusted    = NeonGreen,
                )
            }
        }

        /** NULL corruption degrades positive delta toward gray as corruption rises above 0.5 */
        private fun corruptedDelta(corruption: Double, fallback: Color): Color {
            return if (corruption < 0.5) ElectricBlue else fallback
        }

        /** Rep tier → semantic color */
        fun repColor(tier: String, theme: HudTheme): Color = when (tier) {
            "HOSTILE"  -> ErrorRed
            "LOW"      -> Color(0xFFFFAA00)
            "NEUTRAL"  -> Color.Gray
            "TRUSTED"  -> theme.repTrusted
            "ELITE"    -> theme.repElite
            else       -> Color.Gray
        }

        /** Heat value → semantic color */
        fun heatColor(heat: Double, theme: HudTheme): Color = when {
            heat >= 85.0 -> theme.critical
            heat >= 60.0 -> theme.warning
            else         -> theme.primary
        }

        /** Integrity value → semantic color */
        fun integrityColor(integrity: Double, theme: HudTheme): Color = when {
            integrity < 25.0 -> theme.critical
            integrity < 50.0 -> Color(0xFFFFA500)
            integrity < 85.0 -> theme.warning
            else             -> theme.positiveDelta       // v3.13.42: Restored semantic glow for high integrity
        }
    }
}
