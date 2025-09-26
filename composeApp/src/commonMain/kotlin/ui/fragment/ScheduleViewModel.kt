package ui.fragment

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import model.calendar.MonthDay
import util.CalendarUtil

data class ScheduleState(
    val currYear: Int,
    val currMonthOfYear: Int,    // 1..12
    val currMonthDays: List<MonthDay> = emptyList(),
    val prevMonthDays: List<MonthDay> = emptyList(),
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
}

sealed interface ScheduleAction {
    data object GoPrevMonth : ScheduleAction
    data object GoNextMonth : ScheduleAction
    data class SelectDay(val date: Triple<Int, Int, Int>) : ScheduleAction
}

class ScheduleViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<ScheduleState>
    val uiState: StateFlow<ScheduleState>

    init {
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val initialYear = currentDate.year
        val initialMonth = currentDate.monthNumber
        val initialDay = currentDate.dayOfMonth

        val initialState = ScheduleState(
            currYear = initialYear,
            currMonthOfYear = initialMonth,
            selectDate = Triple(initialYear, initialMonth, initialDay)
        )
        _uiState = MutableStateFlow(initialState)
        uiState = _uiState.asStateFlow()

        refreshMonthDays()

        viewModelScope.launch {
            CalendarUtil.getHolidayMap().collect { holidayMap ->
                if (holidayMap.isNotEmpty()) {
                    refreshMonthDays()
                }
            }
        }
    }

    fun dispatch(action: ScheduleAction) {
        when (action) {
            is ScheduleAction.GoPrevMonth -> {
                goPrevMonth()
            }
            is ScheduleAction.GoNextMonth -> {
                goNextMonth()
            }
            is ScheduleAction.SelectDay -> {
                _uiState.update { it.copy(selectDate = action.date) }
            }
        }
    }

    private fun refreshMonthDays() {
        val year = _uiState.value.currYear
        val monthOfYear = _uiState.value.currMonthOfYear

        if (!CalendarUtil.getHolidayMap().value.containsKey(year)) {
            viewModelScope.launch(Dispatchers.IO) {
                CalendarUtil.fetchHolidayMap(year)
            }
        }

        val monthDays = CalendarUtil.getMonthDays(year, monthOfYear)
        val monthDayList = mutableListOf<MonthDay>()
        monthDays.forEach {
            val isHoliday = CalendarUtil.isHoliday(year, monthOfYear, it.first)
            monthDayList.add(MonthDay(it.first, it.second, isHoliday))
        }

        val (prevYear, prevMonth) = if (monthOfYear == 1) {
            year - 1 to 12
        } else {
            year to monthOfYear - 1
        }
        val prevMonthDaysData = CalendarUtil.getMonthDays(prevYear, prevMonth)
        val prevMonthList = mutableListOf<MonthDay>()
        prevMonthDaysData.forEach {
            val isHoliday = CalendarUtil.isHoliday(prevYear, prevMonth, it.first)
            prevMonthList.add(MonthDay(it.first, it.second, isHoliday))
        }

        _uiState.update {
            it.copy(
                currMonthDays = monthDayList,
                prevMonthDays = prevMonthList
            )
        }
    }

    private fun goPrevMonth() {
        val s = _uiState.value
        val newYear: Int
        val newMonth: Int
        if (s.currMonthOfYear == 1) {
            newYear = s.currYear - 1
            newMonth = 12
        } else {
            newYear = s.currYear
            newMonth = s.currMonthOfYear - 1
        }
        _uiState.update { it.copy(currYear = newYear, currMonthOfYear = newMonth) }
        refreshMonthDays()
    }

    private fun goNextMonth() {
        val s = _uiState.value
        val newYear: Int
        val newMonth: Int
        if (s.currMonthOfYear == 12) {
            newYear = s.currYear + 1
            newMonth = 1
        } else {
            newYear = s.currYear
            newMonth = s.currMonthOfYear + 1
        }
        _uiState.update { it.copy(currYear = newYear, currMonthOfYear = newMonth) }
        refreshMonthDays()
    }
}
