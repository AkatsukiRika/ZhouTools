package util

import extension.dayStartTime
import extension.isBlankJson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.TimeCardDay
import model.TimeCardRecords
import model.TimeCardSyncRequest
import store.AppStore

object TimeCardUtil {
    const val MIN_WORKING_TIME = ((8 + 1.5) * 60 * 60 * 1000).toLong()
    const val MIN_OT_TIME = ((9 + 1.5) * 60 * 60 * 1000).toLong()

    fun pressTimeCard() {
        val curTime = TimeUtil.currentTimeMillis()
        val dayStartTime = curTime.dayStartTime()
        val timeCardDay = TimeCardDay(
            dayStartTime = dayStartTime,
            latestTimeCard = curTime
        )
        try {
            val records: TimeCardRecords = Json.decodeFromString(AppStore.timeCards)
            val days = records.days
            val curDay = days.find { it.dayStartTime == dayStartTime }
            if (curDay != null) {
                curDay.latestTimeCard = curTime
            } else {
                days.add(timeCardDay)
            }
            AppStore.timeCards = Json.encodeToString(records)
        } catch (e: Exception) {
            e.printStackTrace()
            val days = mutableListOf<TimeCardDay>()
            days.add(timeCardDay)
            val records = TimeCardRecords(days = days)
            AppStore.timeCards = Json.encodeToString(records)
        }
    }

    /**
     * @return isSuccess
     */
    fun run(): Boolean {
        val curTime = TimeUtil.currentTimeMillis()
        val dayStartTime = curTime.dayStartTime()
        return try {
            val records: TimeCardRecords = Json.decodeFromString(AppStore.timeCards)
            val days = records.days
            val curDay = days.find { it.dayStartTime == dayStartTime }
            if (curDay != null) {
                curDay.latestTimeRun = curTime
                AppStore.timeCards = Json.encodeToString(records)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun hasTodayTimeCard(): Boolean {
        if (AppStore.timeCards.isBlankJson()) {
            return false
        }
        val curTime = TimeUtil.currentTimeMillis()
        val dayStartTime = curTime.dayStartTime()
        return try {
            val records: TimeCardRecords = Json.decodeFromString(AppStore.timeCards)
            val days = records.days
            val curDay = days.find { it.dayStartTime == dayStartTime }
            curDay != null && curDay.latestTimeRun == null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun hasTodayRun(): Boolean {
        if (AppStore.timeCards.isBlankJson()) {
            return false
        }
        val curTime = TimeUtil.currentTimeMillis()
        val dayStartTime = curTime.dayStartTime()
        return try {
            val records: TimeCardRecords = Json.decodeFromString(AppStore.timeCards)
            val days = records.days
            val curDay = days.find { it.dayStartTime == dayStartTime }
            curDay?.latestTimeRun != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun todayTimeCard(): Long? {
        if (!hasTodayTimeCard()) {
            return null
        }
        val curTime = TimeUtil.currentTimeMillis()
        val dayStartTime = curTime.dayStartTime()
        return try {
            val records: TimeCardRecords = Json.decodeFromString(AppStore.timeCards)
            val days = records.days
            val curDay = days.find { it.dayStartTime == dayStartTime }
            return curDay?.latestTimeCard
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun todayWorkingTime(): Long? {
        if (!hasTodayTimeCard()) {
            return null
        }
        val todayTimeCard = todayTimeCard() ?: return null
        val curTime = TimeUtil.currentTimeMillis()
        return curTime - todayTimeCard
    }

    fun buildSyncRequest(): TimeCardSyncRequest? {
        return try {
            val records: TimeCardRecords = Json.decodeFromString(AppStore.timeCards)
            val username = AppStore.loginUsername
            if (username.isBlank()) {
                null
            } else {
                TimeCardSyncRequest(username, records)
            }
        } catch (e: Exception) {
            null
        }
    }
}