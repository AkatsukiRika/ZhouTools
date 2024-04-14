package util

import kotlinx.datetime.Clock

object TimeUtil {
    fun currentTimeMillis(): Long {
        val now = Clock.System.now()
        return now.toEpochMilliseconds()
    }
}