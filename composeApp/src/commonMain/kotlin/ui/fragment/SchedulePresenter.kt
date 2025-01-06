package ui.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
    var selectDate by remember { mutableStateOf(Triple(0, 1, 1)) }
    val holidayMap = CalendarUtil.getHolidayMap().collectAsState().value

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
        selectDate = Triple(currYear, currMonthOfYear, currentDate.dayOfMonth)
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

    LaunchedEffect(holidayMap) {
        if (holidayMap.isNotEmpty()) {
            refreshMonthDays()
        }
    }

    actionFlow.collectAction {
        when (this) {
            is ScheduleAction.GoPrevMonth -> {
                goPrevMonth()
            }
            is ScheduleAction.GoNextMonth -> {
                goNextMonth()
            }
            is ScheduleAction.SelectDay -> {
                selectDate = date
            }
        }
    }

    return ScheduleState(currYear, currMonthOfYear, currMonthDays, prevMonthDays, selectDate)
}

data class ScheduleState(
    val currYear: Int,
    val currMonthOfYear: Int,    // 1..12
    val currMonthDays: List<Pair<Int, DayOfWeek>> = emptyList(),
    val prevMonthDays: List<Pair<Int, DayOfWeek>> = emptyList(),
    val selectDate: Triple<Int, Int, Int>   // year, month (1..12), day (1..31)
) {
    @Composable
    fun getCurrMonthName(): String {
        val index = currMonthOfYear - 1
        val monthNames = CalendarUtil.getMonthNames()
        return if (index in monthNames.indices) {
            monthNames[index]
        } else ""
    }

    fun isToday(dayOfMonth: Int): Boolean {
        val todayDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return currYear == todayDate.year && currMonthOfYear == todayDate.monthNumber && dayOfMonth == todayDate.dayOfMonth
    }

    fun isSelect(dayOfMonth: Int): Boolean {
        return selectDate.first == currYear && selectDate.second == currMonthOfYear && selectDate.third == dayOfMonth
    }

    fun isHoliday(dayOfMonth: Int) = CalendarUtil.isHoliday(currYear, currMonthOfYear, dayOfMonth)
}

sealed interface ScheduleAction {
    data object GoPrevMonth : ScheduleAction
    data object GoNextMonth : ScheduleAction
    data class SelectDay(val date: Triple<Int, Int, Int>) : ScheduleAction
}