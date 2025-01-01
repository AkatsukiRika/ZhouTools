package ui.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val textMeasurer = rememberTextMeasurer()

    Row(modifier = modifier.horizontalScroll(scrollState)) {
        Canvas(modifier = Modifier
            .padding(horizontal = 16.dp)
            .width(totalWidth)
            .height(chartHeight + 40.dp)
        ) {
            val barWidthPx = barWidth.toPx()
            val spacingPx = barSpacing.toPx()
            val maxValue = data.maxOfOrNull { it.value.toDouble() } ?: 0.0

            val topPadding = 30.dp.toPx()
            val bottomPadding = 30.dp.toPx()
            val startY = topPadding
            val actualChartHeight = size.height - topPadding - bottomPadding

            // Y Axis
            drawLine(
                color = axisColor,
                start = Offset(0f, startY),
                end = Offset(0f, startY + actualChartHeight),
                strokeWidth = with(density) { 2.dp.toPx() }
            )

            // X Axis
            drawLine(
                color = axisColor,
                start = Offset(0f, startY + actualChartHeight),
                end = Offset(size.width, startY + actualChartHeight),
                strokeWidth = with(density) { 2.dp.toPx() }
            )

            data.forEachIndexed { index, barData ->
                val barHeight = if (maxValue > 0) {
                    (barData.value.toDouble() / maxValue * actualChartHeight).toFloat()
                } else {
                    0f
                }
                val x = spacingPx + index * (barWidthPx + spacingPx)

                // Draw bars
                drawRect(
                    color = barData.color,
                    topLeft = Offset(x, startY + actualChartHeight - barHeight),
                    size = Size(barWidthPx, barHeight)
                )

                // Draw values above each bar
                val valueText = barData.value.toString()
                val valueLayoutResult = textMeasurer.measure(
                    text = valueText,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 12.sp
                    )
                )
                
                drawText(
                    textLayoutResult = valueLayoutResult,
                    topLeft = Offset(
                        x = x + (barWidthPx - valueLayoutResult.size.width) / 2,
                        y = startY + actualChartHeight - barHeight - 20.dp.toPx()
                    )
                )

                // Draw labels on X axis
                val labelLayoutResult = textMeasurer.measure(
                    text = barData.label,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 12.sp
                    )
                )
                
                drawText(
                    textLayoutResult = labelLayoutResult,
                    topLeft = Offset(
                        x = x + (barWidthPx - labelLayoutResult.size.width) / 2,
                        y = startY + actualChartHeight + 10.dp.toPx()
                    )
                )
            }
        }
    }
}