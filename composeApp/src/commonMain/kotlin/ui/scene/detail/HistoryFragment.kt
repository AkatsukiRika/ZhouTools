package ui.scene.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import extension.toHourMinString
import extension.toTimeString
import global.AppColors
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import store.AppStore
import ui.widget.EmptyLayout
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.average_working_time
import zhoutools.composeapp.generated.resources.fold_by_month
import zhoutools.composeapp.generated.resources.fold_by_quarter
import zhoutools.composeapp.generated.resources.fold_by_week
import zhoutools.composeapp.generated.resources.fold_by_year
import zhoutools.composeapp.generated.resources.ic_history_overtime
import zhoutools.composeapp.generated.resources.ic_history_run
import zhoutools.composeapp.generated.resources.ic_working
import zhoutools.composeapp.generated.resources.max_working_time
import zhoutools.composeapp.generated.resources.min_working_time
import zhoutools.composeapp.generated.resources.no_history_data
import zhoutools.composeapp.generated.resources.time_card
import zhoutools.composeapp.generated.resources.time_run
import zhoutools.composeapp.generated.resources.working_time

@Composable
fun HistoryFragment(state: DetailHistoryState, onAction: (DetailAction) -> Unit) {
    if (state.weekList.isEmpty() || AppStore.timeCards.isBlankJson()) {
        EmptyLayout(description = stringResource(Res.string.no_history_data))
    } else {
        WeekList(state, onAction)
    }
}

@Composable
private fun WeekList(state: DetailHistoryState, onAction: (DetailAction) -> Unit) {
    val foldPeriodList = state.foldPeriodList

    LazyColumn(modifier = Modifier
        .fillMaxWidth()
    ) {
        item {
            FoldSelections(modifier = Modifier.padding(top = 8.dp), state, onAction)
        }

        if (foldPeriodList != null) {
            items(state.foldPeriodList) {
                val startStr = it.startTime.toDateString()
                val endStr = it.endTime.toDateString()

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "$startStr - $endStr",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                FoldPeriodCard(foldPeriod = it)
            }
        } else {
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
}

@Composable
private fun FoldPeriodCard(foldPeriod: DetailHistoryFoldPeriod) {
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
            val annotatedString = buildAnnotatedString {
                withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.sp)) {
                    append(foldPeriod.totalOvertimeDays.toString())
                }
                withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp)) {
                    append("\n/ " + foldPeriod.totalWorkDays.toString())
                }
            }

            Text(
                text = annotatedString,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 4.dp),
                color = Color.White,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        val annotatedString = buildAnnotatedString {
            withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)) {
                append(stringResource(Res.string.average_working_time) + ": ")
            }

            withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp)) {
                append(foldPeriod.getAverageWorkingTime().toHourMinString(utc = true) + "\n")
            }

            withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)) {
                append(stringResource(Res.string.max_working_time) + ": ")
            }

            withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp)) {
                append(foldPeriod.maxWorkingTime.toHourMinString(utc = true) + "\n")
            }

            withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.sp)) {
                append(stringResource(Res.string.min_working_time) + ": ")
            }

            withStyle(SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.sp)) {
                append(foldPeriod.minWorkingTime.toHourMinString(utc = true))
            }
        }

        Text(
            annotatedString,
            lineHeight = 20.sp,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

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

@Composable
private fun FoldSelections(modifier: Modifier = Modifier, state: DetailHistoryState, onAction: (DetailAction) -> Unit) {
    val chipColors = AppColors.getChipColors()
    LazyRow(modifier) {
        item {
            Spacer(modifier = Modifier.width(16.dp))
        }

        item {
            ElevatedFilterChip(
                selected = state.foldType == DetailFoldType.WEEK,
                onClick = {
                    onAction(DetailAction.ChangeFoldType(DetailFoldType.WEEK))
                },
                label = {
                    androidx.compose.material3.Text(text = stringResource(Res.string.fold_by_week))
                },
                colors = chipColors
            )

            Spacer(modifier = Modifier.width(16.dp))
        }

        item {
            ElevatedFilterChip(
                selected = state.foldType == DetailFoldType.MONTH,
                onClick = {
                    onAction(DetailAction.ChangeFoldType(DetailFoldType.MONTH))
                },
                label = {
                    androidx.compose.material3.Text(text = stringResource(Res.string.fold_by_month))
                },
                colors = chipColors
            )

            Spacer(modifier = Modifier.width(16.dp))
        }

        item {
            ElevatedFilterChip(
                selected = state.foldType == DetailFoldType.QUARTER,
                onClick = {
                    onAction(DetailAction.ChangeFoldType(DetailFoldType.QUARTER))
                },
                label = {
                    androidx.compose.material3.Text(text = stringResource(Res.string.fold_by_quarter))
                },
                colors = chipColors
            )

            Spacer(modifier = Modifier.width(16.dp))
        }

        item {
            ElevatedFilterChip(
                selected = state.foldType == DetailFoldType.YEAR,
                onClick = {
                    onAction(DetailAction.ChangeFoldType(DetailFoldType.YEAR))
                },
                label = {
                    androidx.compose.material3.Text(text = stringResource(Res.string.fold_by_year))
                },
                colors = chipColors
            )

            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}
