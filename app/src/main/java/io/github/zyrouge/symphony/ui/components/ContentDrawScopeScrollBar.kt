package io.github.zyrouge.symphony.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.unit.dp

object ContentDrawScopeScrollBarDefaults {
    val scrollPointerWidth = 4.dp
    val scrollPointerHeight = 16.dp
}

fun ContentDrawScope.drawScrollBar(
    scrollPointerColor: Color,
    scrollPointerOffsetY: Float,
) {
    val scrollPointerWidth = ContentDrawScopeScrollBarDefaults.scrollPointerWidth.toPx()
    val scrollPointerHeight = ContentDrawScopeScrollBarDefaults.scrollPointerHeight.toPx()
    val scrollPointerCorner = CornerRadius(scrollPointerWidth, scrollPointerWidth)

    drawPath(
        color = scrollPointerColor,
        path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset = Offset(
                            size.width - scrollPointerWidth,
                            scrollPointerOffsetY,
                        ),
                        size = Size(scrollPointerWidth, scrollPointerHeight),
                    ),
                    topLeft = scrollPointerCorner,
                    bottomLeft = scrollPointerCorner,
                )
            )
        },
    )
}
