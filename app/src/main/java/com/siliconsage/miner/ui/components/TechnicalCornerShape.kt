package com.siliconsage.miner.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * TechnicalCornerShape v1.0
 * Provides 45-degree beveled/clipped corners for a high-frontier industrial aesthetic.
 */
class TechnicalCornerShape(private val cornerSize: Float = 12f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(cornerSize, 0f)
            lineTo(size.width - cornerSize, 0f)
            lineTo(size.width, cornerSize)
            lineTo(size.width, size.height - cornerSize)
            lineTo(size.width - cornerSize, size.height)
            lineTo(cornerSize, size.height)
            lineTo(0f, size.height - cornerSize)
            lineTo(0f, cornerSize)
            close()
        }
        return Outline.Generic(path)
    }
}
