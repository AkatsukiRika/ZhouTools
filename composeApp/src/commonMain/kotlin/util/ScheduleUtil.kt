package util

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger
import model.records.Schedule
import model.records.ScheduleRecords
import store.AppStore

class ScheduleUtil {
    private val schedules = mutableListOf<Schedule>()

    init {
        refreshData()
    }

    fun refreshData() {
        try {
            val records: ScheduleRecords = Json.decodeFromString(AppStore.schedules)
            schedules.addAll(records.schedules)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getScheduleList(): List<Schedule> = schedules

    fun addSchedule(schedule: Schedule) {
        schedules.add(schedule)
        saveToDataStore()
    }

    private fun saveToDataStore() {
        val records = ScheduleRecords(schedules)
        AppStore.schedules = Json.encodeToString(records)
        logger.i { "ScheduleUtil savedToDataStore, AppStore.schedules=${AppStore.schedules}" }
    }
}