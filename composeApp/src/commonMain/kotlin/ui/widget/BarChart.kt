package ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import global.AppColors

data class BarData<T : Number>(
    val value: T,
    val label: String,
    val color: Color = AppColors.Theme
)

@Composable
fun <T : Number> BarChart(
    modifier: Modifier = Modifier,
    data: List<BarData<T>>,
    barWidth: Dp = 40.dp,
    barSpacing: Dp = 20.dp,
    chartHeight: Dp = 200.dp,
    axisColor: Color = Color.Gray,
    textColor: Color = Color.DarkGray
) {
    val scrollState = rememberScrollState()
    val totalWidth = remember(data.size, barWidth, barSpacing) {
        val barsWidth = barWidth * data.size
        val spacingWidth = barSpacing * (data.size + 1)
        barsWidth + spacingWidth
    }
    val density = LocalDensity.current

    Row(modifier = modifier.horizontalScroll(scrollState)) {
        Canvas(modifier = Modifier
            .width(totalWidth)
            .height(chartHeight)
        ) {
            val barWidthPx = barWidth.toPx()
            val spacingPx = barSpacing.toPx()
            val maxValue = data.maxOfOrNull { it.value.toDouble() } ?: 0.0

            // Y Axis
            drawLine(
                color = axisColor,
                start = Offset(0f, 0f),
                end = Offset(0f, size.height),
                strokeWidth = with(density) { 2.dp.toPx() }
            )

            data.forEachIndexed { index, barData ->
                val barHeight = if (maxValue > 0) {
                    (barData.value.toDouble() / maxValue * size.height).toFloat()
                } else {
                    0f
                }
                val x = spacingPx + index * (barWidthPx + spacingPx)

                drawRect(
                    color = barData.color,
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidthPx, barHeight)
                )
            }
        }
    }
}