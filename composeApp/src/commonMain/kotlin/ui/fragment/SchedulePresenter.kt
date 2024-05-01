package ui.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import moe.tlaster.precompose.molecule.collectAction
import util.CalendarUtil

@Composable
fun SchedulePresenter(actionFlow: Flow<ScheduleAction>): ScheduleState {
    var currYear by remember { mutableIntStateOf(0) }
    var currMonthOfYear by remember { mutableIntStateOf(0) }
    var currMonthDays by remember { mutableStateOf<List<Pair<Int, DayOfWeek>>>(emptyList()) }
    var prevMonthDays by remember { mutableStateOf<List<Pair<Int, DayOfWeek>>>(emptyList()) }

    fun refreshMonthDays() {
        val monthDays = CalendarUtil.getMonthDays(currYear, currMonthOfYear)
        val prevMonth = if (currMonthOfYear == 1) {
            CalendarUtil.getMonthDays(currYear - 1, 12)
        } else {
            CalendarUtil.getMonthDays(currYear, currMonthOfYear - 1)
        }
        currMonthDays = monthDays
        prevMonthDays = prevMonth
    }

    fun init() {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        currYear = currentDate.year
        currMonthOfYear = currentDate.monthNumber
        refreshMonthDays()
    }

    fun goPrevMonth() {
        if (currMonthOfYear == 1) {
            currYear--
            currMonthOfYear = 12
        } else {
            currMonthOfYear--
        }
        refreshMonthDays()
    }

    fun goNextMonth() {
        if (currMonthOfYear == 12) {
            currYear++
            currMonthOfYear = 1
        } else {
            currMonthOfYear++
        }
        refreshMonthDays()
    }

    LaunchedEffect(Unit) {
        init()
    }

    actionFlow.collectAction {
        when (this) {
            is ScheduleAction.GoPrevMonth -> {
                goPrevMonth()
            }
            is ScheduleAction.GoNextMonth -> {
                goNextMonth()
            }
        }
    }

    return ScheduleState(currYear, currMonthOfYear, currMonthDays, prevMonthDays)
}

data class ScheduleState(
    val currYear: Int,
    val currMonthOfYear: Int,    // 1..12
    val currMonthDays: List<Pair<Int, DayOfWeek>> = emptyList(),
    val prevMonthDays: List<Pair<Int, DayOfWeek>> = emptyList()
) {
    @Composable
    fun getCurrMonthName(): String {
        val index = currMonthOfYear - 1
        val monthNames = CalendarUtil.getMonthNames()
        return if (index in monthNames.indices) {
            monthNames[index]
        } else ""
    }
}

sealed interface ScheduleAction {
    data object GoPrevMonth : ScheduleAction
    data object GoNextMonth : ScheduleAction
}