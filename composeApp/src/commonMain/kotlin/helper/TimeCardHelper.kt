package helper

import extension.dayStartTime
import extension.isBlankJson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.records.TimeCardDay
import model.records.TimeCardRecords
import model.request.TimeCardSyncRequest
import store.AppFlowStore
import store.AppStore
import util.TimeUtil

object TimeCardHelper {
    fun getMinWorkingTimeMillis() = runBlocking {
        val hours = AppFlowStore.minWorkingHoursFlow.first()
        val millis = hours * 60 * 60 * 1000
        millis.toLong()
    }

    fun getMinOvertimeMillis() = runBlocking {
        val hours = AppFlowStore.minWorkingHoursFlow.first() + AppFlowStore.minOvertimeHoursFlow.first()
        val millis = hours * 60 * 60 * 1000
        millis.toLong()
    }

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

    private fun hasTodayTimeCard(): Boolean {
        if (AppStore.timeCards.isBlankJson()) {
            return false
        }
        val curTime = TimeUtil.currentTimeMillis()
        val dayStartTime = curTime.dayStartTime()
        return try {
            val records: TimeCardRecords = Json.decodeFromString(AppStore.timeCards)
            val days = records.days
            val curDay = days.find { it.dayStartTime == dayStartTime }
            curDay != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun todayTimeRun(): Long? {
        if (AppStore.timeCards.isBlankJson()) {
            return null
        }
        val curTime = TimeUtil.currentTimeMillis()
        val dayStartTime = curTime.dayStartTime()
        return try {
            val records: TimeCardRecords = Json.decodeFromString(AppStore.timeCards)
            val days = records.days
            val curDay = days.find { it.dayStartTime == dayStartTime }
            curDay?.latestTimeRun
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun todayTimeCard(): Long? {
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