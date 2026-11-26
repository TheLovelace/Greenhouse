@file:Suppress("ktlint:standard:function-naming")

package org.greenhouse.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

enum class ArrowDirection {
    Left,
    Right,
}

@Composable
fun ArrowIcon(
    direction: ArrowDirection,
    color: Color,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Canvas(
        modifier =
            if (contentDescription == null) {
                modifier
            } else {
                modifier.semantics { this.contentDescription = contentDescription }
            },
    ) {
        val startX = if (direction == ArrowDirection.Right) size.width * 0.12f else size.width * 0.88f
        val endX = if (direction == ArrowDirection.Right) size.width * 0.88f else size.width * 0.12f
        val headX = if (direction == ArrowDirection.Right) endX - size.width * 0.32f else endX + size.width * 0.32f
        val centerY = size.height / 2
        val headOffset = size.height * 0.28f
        val strokeWidth = 2.5.dp.toPx()

        drawLine(
            color = color,
            start = Offset(startX, centerY),
            end = Offset(endX, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(endX, centerY),
            end = Offset(headX, centerY - headOffset),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(endX, centerY),
            end = Offset(headX, centerY + headOffset),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}
