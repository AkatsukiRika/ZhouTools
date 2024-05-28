package extension

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import util.CalendarUtil

fun Long.dayStartTime(): Long {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val startOfDay = localDateTime.date.atStartOfDayIn(TimeZone.currentSystemDefault())
    return startOfDay.toEpochMilliseconds()
}

fun Long.weekStartTime(): Long {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val localDate = localDateTime.date
    val daysToMonday = (localDate.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
    val mondayDate = localDate.minus(DatePeriod(days = daysToMonday))
    val startOfDay = mondayDate.atStartOfDayIn(TimeZone.currentSystemDefault())
    return startOfDay.toEpochMilliseconds()
}

fun Long.monthStartTime(): Long {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val localDate = localDateTime.date
    val startOfMonth = localDate.minus(DatePeriod(days = localDate.dayOfMonth - 1))
    val startOfDay = startOfMonth.atStartOfDayIn(TimeZone.currentSystemDefault())
    return startOfDay.toEpochMilliseconds()
}

fun Long.toTimeString(utc: Boolean = false): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(
        if (utc) TimeZone.UTC else TimeZone.currentSystemDefault()
    )
    return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}:${localDateTime.second.toString().padStart(2, '0')}"
}

fun Long.toHourMinString(utc: Boolean = false): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(
        if (utc) TimeZone.UTC else TimeZone.currentSystemDefault()
    )
    return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
}

fun Long.getHour(): Int {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.hour
}

fun Long.getMinute(): Int {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.minute
}

fun Long.getYear(): Int {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.year
}

fun Long.getMonthOfYear(): Int {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.monthNumber
}

fun Long.getDayOfMonth(): Int {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.dayOfMonth
}

fun Long.toDateString(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.year}/${localDateTime.monthNumber.toString().padStart(2, '0')}/${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
}

fun Long.toMonthDayString(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.monthNumber.toString().padStart(2, '0')}/${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
}

/**
 * @return Strings like "May 2024", "Jan 2025"
 */
fun Long.toMonthYearString(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val monthList = runBlocking {
        CalendarUtil.getMonthNamesNonComposable()
    }
    val monthNumberFromZero = localDateTime.monthNumber - 1
    if (monthNumberFromZero in monthList.indices) {
        val monthName = monthList[monthNumberFromZero]
        return "$monthName ${localDateTime.year}"
    }
    return ""
}
