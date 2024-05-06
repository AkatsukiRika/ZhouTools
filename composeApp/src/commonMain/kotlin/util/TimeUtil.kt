package util

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
}