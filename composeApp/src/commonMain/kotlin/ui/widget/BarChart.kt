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
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import store.CurrentProcessStore

data class BarData<T : Number>(
    val values: LinkedHashMap<Color, T>,
    val label: String,
    val valueToString: (T) -> String
) {
    fun getTotalValue(): T {
        val sum = values.values.sumOf { it.toDouble() }
        return when (values.values.firstOrNull()) {
            is Int -> sum.toInt() as T
            is Long -> sum.toLong() as T
            is Float -> sum.toFloat() as T
            is Double -> sum as T
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }
}

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
    if (data.isEmpty()) {
        return
    }

    val density = LocalDensity.current
    val screenWidthPixels = CurrentProcessStore.screenWidthPixels.collectAsStateWithLifecycle().value
    val scrollState = rememberScrollState()
    val canvasPaddingStart = 16.dp
    val canvasPaddingEnd = 24.dp
    val totalWidth = remember(data.size, barWidth, barSpacing, screenWidthPixels) {
        val barsWidth = barWidth * data.size
        val spacingWidth = barSpacing * (data.size + 1)
        val screenWidth = with(density) {
            screenWidthPixels.toDp()
        }
        max(screenWidth - canvasPaddingStart - canvasPaddingEnd, barsWidth + spacingWidth)
    }
    val textMeasurer = rememberTextMeasurer()

    Row(modifier = modifier.horizontalScroll(scrollState)) {
        Canvas(modifier = Modifier
            .padding(start = canvasPaddingStart, end = canvasPaddingEnd)
            .width(totalWidth)
            .height(chartHeight + 40.dp)
        ) {
            val barWidthPx = barWidth.toPx()
            val spacingPx = barSpacing.toPx()
            val maxValue = data.maxOfOrNull { it.getTotalValue().toDouble() } ?: 0.0

            val topPadding = 30.dp.toPx()
            val bottomPadding = 30.dp.toPx()
            val startY = topPadding
            val actualChartHeight = size.height - topPadding - bottomPadding

            val arrowSize = 10.dp.toPx()

            // Y Axis
            drawLine(
                color = axisColor,
                start = Offset(0f, arrowSize),
                end = Offset(0f, startY + actualChartHeight),
                strokeWidth = with(density) { 2.dp.toPx() }
            )
            
            // Y Axis Arrow
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(-arrowSize / 2, arrowSize)
                    lineTo(arrowSize / 2, arrowSize)
                    close()
                },
                color = axisColor
            )

            // X Axis
            drawLine(
                color = axisColor,
                start = Offset(0f, startY + actualChartHeight),
                end = Offset(size.width, startY + actualChartHeight),
                strokeWidth = with(density) { 2.dp.toPx() }
            )
            
            // X Axis Arrow
            drawPath(
                path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width + arrowSize, startY + actualChartHeight)
                    lineTo(size.width, startY + actualChartHeight - arrowSize/2)
                    lineTo(size.width, startY + actualChartHeight + arrowSize/2)
                    close()
                },
                color = axisColor
            )

            data.forEachIndexed { index, barData ->
                val totalValue = barData.getTotalValue()
                val barHeight = if (maxValue > 0) {
                    totalValue.toFloat() / maxValue.toFloat() * actualChartHeight
                } else {
                    0f
                }
                val x = spacingPx + index * (barWidthPx + spacingPx)

                // Draw stacked bars
                var currentHeight = 0f
                barData.values.forEach { (color, value) ->
                    val partHeight = (value.toDouble() / maxValue * actualChartHeight).toFloat()
                    drawRect(
                        color = color,
                        topLeft = Offset(x, startY + actualChartHeight - currentHeight - partHeight),
                        size = Size(barWidthPx, partHeight)
                    )
                    currentHeight += partHeight
                }

                // Draw values above each bar
                val valueText = barData.valueToString(totalValue)
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