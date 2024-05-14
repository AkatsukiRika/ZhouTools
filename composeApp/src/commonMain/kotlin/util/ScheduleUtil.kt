package util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger
import model.records.Schedule
import model.records.ScheduleRecords
import model.request.ScheduleSyncRequest
import store.AppStore

object ScheduleUtil {
    private val schedules = mutableListOf<Schedule>()

    init {
        refreshData()
    }

    fun refreshData() {
        try {
            val records: ScheduleRecords = Json.decodeFromString(AppStore.schedules)
            schedules.clear()
            schedules.addAll(records.schedules)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDisplayList(): List<Schedule> {
        val displayList = mutableListOf<Schedule>()
        val milestoneList = schedules.filter { it.isMilestone }
        val othersList = schedules.filterNot { it.isMilestone }
        displayList.addAll(milestoneList)
        displayList.addAll(othersList)
        return displayList
    }

    fun addSchedule(schedule: Schedule) {
        schedules.add(schedule)
        saveToDataStore()
    }

    fun deleteSchedule(schedule: Schedule) {
        schedules.remove(schedule)
        saveToDataStore()
    }

    fun buildSyncRequest(): ScheduleSyncRequest? {
        return try {
            if (AppStore.loginUsername.isEmpty()) {
                null
            } else {
                ScheduleSyncRequest(username = AppStore.loginUsername, schedules = schedules)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun saveToDataStore() {
        val records = ScheduleRecords(schedules)
        AppStore.schedules = Json.encodeToString(records)
        logger.i { "ScheduleUtil savedToDataStore, AppStore.schedules=${AppStore.schedules}" }
    }
}