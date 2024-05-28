package util

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

object TimeUtil {
    fun currentTimeMillis(): Long {
        val now = Clock.System.now()
        return now.toEpochMilliseconds()
    }

    fun toEpochMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        val localDateTime = LocalDateTime(year, month, day, hour, minute)
        return localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    fun monthYearStringToMonthStartTime(monthYearString: String): Long? {
        return try {
            val (monthStr, yearStr) = monthYearString.split(" ")
            val monthNames = runBlocking { CalendarUtil.getMonthNamesNonComposable() }
            val monthNumber = monthNames.indexOf(monthStr) + 1
            val year = yearStr.toInt()
            toEpochMillis(year, monthNumber, 1, 0, 0)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}