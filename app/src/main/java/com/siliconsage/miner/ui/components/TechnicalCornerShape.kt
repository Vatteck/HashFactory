package com.siliconsage.miner.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * TechnicalCornerShape v2.0
 * Provides 45-degree beveled/clipped corners.
 * Support for individual corners allows for "connected" UI elements (Tabs).
 */
class TechnicalCornerShape(
    private val topStart: Float = 0f,
    private val topEnd: Float = 0f,
    private val bottomEnd: Float = 0f,
    private val bottomStart: Float = 0f
) : Shape {
    
    // Convenience constructor for symmetric shapes
    constructor(all: Float) : this(all, all, all, all)

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // Top edge
            moveTo(topStart, 0f)
            lineTo(size.width - topEnd, 0f)
            // Top right bevel
            if (topEnd > 0) lineTo(size.width, topEnd) else lineTo(size.width, 0f)
            
            // Right edge
            lineTo(size.width, size.height - bottomEnd)
            // Bottom right bevel
            if (bottomEnd > 0) lineTo(size.width - bottomEnd, size.height) else lineTo(size.width, size.height)
            
            // Bottom edge
            lineTo(bottomStart, size.height)
            // Bottom left bevel
            if (bottomStart > 0) lineTo(0f, size.height - bottomStart) else lineTo(0f, size.height)
            
            // Left edge
            lineTo(0f, topStart)
            // Top left bevel
            if (topStart > 0) lineTo(topStart, 0f) else lineTo(0f, 0f)
            
            close()
        }
        return Outline.Generic(path)
    }
}
