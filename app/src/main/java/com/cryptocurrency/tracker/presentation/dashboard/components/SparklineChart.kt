package com.cryptocurrency.tracker.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SparklineChart(
    data: List<Double>,
    modifier: Modifier = Modifier,
    color: Color = Color.Green,
    showFill: Boolean = true,
    strokeWidth: Dp = 2.dp
) {
    if (data.size < 2) return

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxPrice = data.maxOrNull() ?: 0.0
        val minPrice = data.minOrNull() ?: 0.0
        val priceRange = (maxPrice - minPrice).coerceAtLeast(0.000001)
        
        val verticalPadding = 2.dp.toPx()
        val usableHeight = height - (verticalPadding * 2)

        val points = data.mapIndexed { index, price ->
            val x = index * (width / (data.size - 1))
            val y = verticalPadding + (usableHeight - ((price - minPrice) / priceRange * usableHeight)).toFloat()
            Offset(x, y)
        }

        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        if (showFill) {
            val fillPath = Path().apply {
                addPath(strokePath)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }

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
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}
