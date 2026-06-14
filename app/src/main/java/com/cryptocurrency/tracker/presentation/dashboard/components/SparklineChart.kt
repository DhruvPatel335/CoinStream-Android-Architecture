package com.cryptocurrency.tracker.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cryptocurrency.tracker.domain.model.SparklineData

@Composable
fun SparklineChart(
    data: SparklineData,
    modifier: Modifier = Modifier,
    color: Color = Color.Green,
    showFill: Boolean = true,
    strokeWidth: Dp = 2.dp
) {
    if (data.values.size < 2) return

    val density = LocalDensity.current
    
    // Optimization: Cache the drawing logic to prevent re-calculating paths during every recomposition
    // Especially important when parent recomposes frequently due to price updates.
    val drawModifier = remember(data, color, showFill, strokeWidth, density) {
        Modifier.drawWithCache {
            val width = size.width
            val height = size.height
            
            val maxPrice = data.values.maxOrNull() ?: 0.0
            val minPrice = data.values.minOrNull() ?: 0.0
            val priceRange = (maxPrice - minPrice).coerceAtLeast(0.000001)
            
            val verticalPaddingPx = 2.dp.toPx()
            val usableHeight = height - (verticalPaddingPx * 2)

            val strokePath = Path().apply {
                val firstX = 0f
                val firstY = verticalPaddingPx + (usableHeight - ((data.values[0] - minPrice) / priceRange * usableHeight)).toFloat()
                moveTo(firstX, firstY)
                
                for (i in 1 until data.values.size) {
                    val x = i * (width / (data.values.size - 1))
                    val y = verticalPaddingPx + (usableHeight - ((data.values[i] - minPrice) / priceRange * usableHeight)).toFloat()
                    lineTo(x, y)
                }
            }

            val fillPath = if (showFill) {
                Path().apply {
                    addPath(strokePath)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
            } else null

            val strokeWidthPx = strokeWidth.toPx()

            onDrawBehind {
                if (fillPath != null) {
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
                            endY = height
                        )
                    )
                }

                drawPath(
                    path = strokePath,
                    color = color,
                    style = Stroke(width = strokeWidthPx)
                )
            }
        }
    }

    Canvas(modifier = modifier.then(drawModifier)) {
        // Drawing is handled by drawWithCache
    }
}
