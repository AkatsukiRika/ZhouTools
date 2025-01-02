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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LineData<T : Number>(
    val values: LinkedHashMap<Color, T>,
    val label: String,
    val valueToString: (T) -> String
)

@Composable
fun <T : Number> LineChart(
    modifier: Modifier = Modifier,
    data: List<LineData<T>>,
    pointRadius: Dp = 4.dp,
    lineWidth: Dp = 2.dp,
    pointSpacing: Dp = 40.dp,
    chartHeight: Dp = 200.dp,
    axisColor: Color = Color.Gray,
    textColor: Color = Color.DarkGray
) {
    if (data.isEmpty()) {
        return
    }

    val scrollState = rememberScrollState()
    val totalWidth = remember(data.size, pointSpacing) {
        pointSpacing * (data.size + 1)
    }
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    Row(modifier = modifier.horizontalScroll(scrollState)) {
        Canvas(modifier = Modifier
            .padding(start = 16.dp, end = 24.dp)
            .width(totalWidth)
            .height(chartHeight + 40.dp)
        ) {
            val spacingPx = pointSpacing.toPx()
            val maxValue = data.maxOfOrNull {
                it.values.values.maxOf { value -> value.toDouble() }
            } ?: 0.0

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
                path = Path().apply {
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
                path = Path().apply {
                    moveTo(size.width + arrowSize, startY + actualChartHeight)
                    lineTo(size.width, startY + actualChartHeight - arrowSize/2)
                    lineTo(size.width, startY + actualChartHeight + arrowSize/2)
                    close()
                },
                color = axisColor
            )

            // Draw lines for every color
            data.first().values.keys.forEach { color ->
                val points = data.mapIndexed { index, lineData ->
                    val x = spacingPx + index * spacingPx
                    val value = lineData.values[color] ?: return@forEach
                    val y = if (maxValue > 0) {
                        startY + actualChartHeight - (value.toDouble() / maxValue * actualChartHeight).toFloat()
                    } else {
                        startY + actualChartHeight
                    }
                    Offset(x, y)
                }

                // Draw Line
                for (i in 0 until points.size - 1) {
                    drawLine(
                        color = color,
                        start = points[i],
                        end = points[i + 1],
                        strokeWidth = lineWidth.toPx()
                    )
                }

                // Draw points on the line
                points.forEach { point ->
                    drawCircle(
                        color = color,
                        radius = pointRadius.toPx(),
                        center = point
                    )
                }
            }

            // Draw labels and values
            data.forEachIndexed { index, lineData ->
                val x = spacingPx + index * spacingPx
                val y = startY + actualChartHeight

                // Draw labels on X axis
                val labelLayoutResult = textMeasurer.measure(
                    text = lineData.label,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 12.sp
                    )
                )

                drawText(
                    textLayoutResult = labelLayoutResult,
                    topLeft = Offset(
                        x = x - labelLayoutResult.size.width / 2,
                        y = y + 10.dp.toPx()
                    )
                )

                // Draw values
                lineData.values.forEach { (_, value) ->
                    val valueText = lineData.valueToString(value)
                    val valueLayoutResult = textMeasurer.measure(
                        text = valueText,
                        style = TextStyle(
                            color = textColor,
                            fontSize = 12.sp
                        )
                    )

                    val valueY = if (maxValue > 0) {
                        y - (value.toDouble() / maxValue * actualChartHeight).toFloat()
                    } else {
                        y
                    }

                    drawText(
                        textLayoutResult = valueLayoutResult,
                        topLeft = Offset(
                            x = x - valueLayoutResult.size.width / 2,
                            y = valueY - 20.dp.toPx()
                        )
                    )
                }
            }
        }
    }
}