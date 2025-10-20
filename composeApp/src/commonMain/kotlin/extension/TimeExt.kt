package extension

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import util.CalendarUtil
import util.TimeUtil
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun Long.dayStartTime(): Long {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val startOfDay = localDateTime.date.atStartOfDayIn(TimeZone.currentSystemDefault())
    return startOfDay.toEpochMilliseconds()
}

@OptIn(ExperimentalTime::class)
fun Long.weekStartTime(): Long {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val localDate = localDateTime.date
    val daysToMonday = (localDate.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
    val mondayDate = localDate.minus(DatePeriod(days = daysToMonday))
    val startOfDay = mondayDate.atStartOfDayIn(TimeZone.currentSystemDefault())
    return startOfDay.toEpochMilliseconds()
}

@OptIn(ExperimentalTime::class)
fun Long.monthStartTime(): Long {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val localDate = localDateTime.date
    val startOfMonth = localDate.minus(DatePeriod(days = localDate.dayOfMonth - 1))
    val startOfDay = startOfMonth.atStartOfDayIn(TimeZone.currentSystemDefault())
    return startOfDay.toEpochMilliseconds()
}

@OptIn(ExperimentalTime::class)
fun Long.getNextWeekStartTime(): Long {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val localDate = localDateTime.date
    val daysToMonday = (localDate.dayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
    val nextMondayDate = localDate.minus(DatePeriod(days = daysToMonday)).plus(DatePeriod(days = 7))
    val startOfDay = nextMondayDate.atStartOfDayIn(TimeZone.currentSystemDefault())
    return startOfDay.toEpochMilliseconds()
}

@OptIn(ExperimentalTime::class)
fun Long.getNextMonthStartTime(): Long {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val localDate = localDateTime.date
    val nextMonthDate = localDate.minus(DatePeriod(days = localDate.dayOfMonth - 1)).plus(DatePeriod(months = 1))
    val startOfDay = nextMonthDate.atStartOfDayIn(TimeZone.currentSystemDefault())
    return startOfDay.toEpochMilliseconds()
}

@OptIn(ExperimentalTime::class)
fun Long.getNextQuarterStartTime(): Long {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val localDate = localDateTime.date
    val month = localDate.monthNumber
    val nextQuarterMonth = when (month) {
        in 1..3 -> 4
        in 4..6 -> 7
        in 7..9 -> 10
        else -> 1
    }
    val nextQuarterYear = if (nextQuarterMonth == 1) localDate.year + 1 else localDate.year
    val nextQuarterStartTime = TimeUtil.toEpochMillis(nextQuarterYear, nextQuarterMonth, 1, 0, 0)
    return nextQuarterStartTime
}

@OptIn(ExperimentalTime::class)
fun Long.getNextYearStartTime(): Long {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val localDate = localDateTime.date
    val nextYearStartTime = TimeUtil.toEpochMillis(localDate.year + 1, 1, 1, 0, 0)
    return nextYearStartTime
}

@OptIn(ExperimentalTime::class)
fun Long.toTimeString(utc: Boolean = false): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(
        if (utc) TimeZone.UTC else TimeZone.currentSystemDefault()
    )
    return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}:${localDateTime.second.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalTime::class)
fun Long.toHourMinString(utc: Boolean = false): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(
        if (utc) TimeZone.UTC else TimeZone.currentSystemDefault()
    )
    return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
}

fun Long.toDays(): Int {
    return (this / (1000 * 60 * 60 * 24)).toInt()
}

fun String.daysToMillis(): Long {
    return this.toLong() * 1000 * 60 * 60 * 24
}

@OptIn(ExperimentalTime::class)
fun Long.getHour(): Int {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.hour
}

@OptIn(ExperimentalTime::class)
fun Long.getMinute(): Int {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.minute
}

@OptIn(ExperimentalTime::class)
fun Long.getYear(): Int {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.year
}

@OptIn(ExperimentalTime::class)
fun Long.getMonthOfYear(): Int {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.monthNumber
}

@OptIn(ExperimentalTime::class)
fun Long.getDayOfMonth(): Int {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return localDateTime.dayOfMonth
}

@OptIn(ExperimentalTime::class)
fun Long.toDateString(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.year}/${localDateTime.monthNumber.toString().padStart(2, '0')}/${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
}

@OptIn(ExperimentalTime::class)
fun Long.toMonthDayString(): String {
    val instant = Instant.fromEpochMilliseconds(this)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.monthNumber.toString().padStart(2, '0')}/${localDateTime.dayOfMonth.toString().padStart(2, '0')}"
}

/**
 * @return Strings like "May 2024", "Jan 2025"
 */
@OptIn(ExperimentalTime::class)
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
