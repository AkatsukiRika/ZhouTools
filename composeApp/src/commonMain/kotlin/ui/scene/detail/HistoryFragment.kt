package ui.scene.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import extension.isBlankJson
import extension.toDateString
import extension.toTimeString
import global.AppColors
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import store.AppStore
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.ic_empty
import zhoutools.composeapp.generated.resources.ic_history_overtime
import zhoutools.composeapp.generated.resources.ic_history_run
import zhoutools.composeapp.generated.resources.ic_working
import zhoutools.composeapp.generated.resources.no_history_data
import zhoutools.composeapp.generated.resources.time_card
import zhoutools.composeapp.generated.resources.time_run
import zhoutools.composeapp.generated.resources.working_time

@OptIn(ExperimentalResourceApi::class)
@Composable
fun HistoryFragment(state: DetailHistoryState) {
    if (state.weekList.isEmpty() || AppStore.timeCards.isBlankJson()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp)
                .alpha(0.5f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_empty),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.no_history_data),
                fontSize = 16.sp
            )
        }
    } else {
        WeekList(state)
    }
}

@Composable
private fun WeekList(state: DetailHistoryState) {
    LazyColumn(modifier = Modifier
        .fillMaxWidth()
    ) {
        items(state.weekList) {
            val weekStartStr = it.weekStartTime.toDateString()
            val weekEndStr = it.weekEndTime.toDateString()

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$weekStartStr - $weekEndStr",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            it.days.forEach { day ->
                DayItem(day)
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun DayItem(day: DetailHistoryWeekDay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))

        Box(modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.LightTheme)
            .size(56.dp)
        ) {
            Icon(
                painter = painterResource(
                    if (day.timeRun == 0L) {
                        Res.drawable.ic_working
                    } else {
                        if (day.isOT) Res.drawable.ic_history_overtime else Res.drawable.ic_history_run
                    }
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(fraction = 0.6f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        val annotatedString = buildAnnotatedString {
            withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)) {
                append(day.dayStartTime.toDateString() + "\n")
            }

            withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)) {
                append(stringResource(Res.string.time_card) + ": ")
            }

            withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp)) {
                append(day.timeCard.toTimeString())
            }

            if (day.timeRun != 0L) {
                withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)) {
                    append("\n" + stringResource(Res.string.time_run) + ": ")
                }

                withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp)) {
                    append(day.timeRun.toTimeString())
                }

                withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)) {
                    append("\n" + stringResource(Res.string.working_time) + ": ")
                }

                withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp)) {
                    append(day.timeWork.toTimeString(utc = true))
                }
            }
        }

        Text(
            annotatedString,
            lineHeight = 20.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}