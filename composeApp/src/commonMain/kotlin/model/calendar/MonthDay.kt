package model.calendar

import kotlinx.datetime.DayOfWeek
import util.CalendarUtil

data class MonthDay(
    val day: Int,               // 1..31
    val dayOfWeek: DayOfWeek,
    val isHoliday: Int = CalendarUtil.NOT_HOLIDAY
)
