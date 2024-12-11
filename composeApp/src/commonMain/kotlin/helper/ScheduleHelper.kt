package helper

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.records.Schedule
import model.records.ScheduleRecords
import model.request.ScheduleSyncRequest
import store.AppStore

object ScheduleHelper {
    fun getDisplayList(): List<Schedule> {
        val schedules = getSchedules()
        val displayList = mutableListOf<Schedule>()
        val milestoneList = schedules.filter { it.isMilestone }
        val othersList = schedules.filterNot { it.isMilestone }
        displayList.addAll(milestoneList)
        displayList.addAll(othersList)
        return displayList
    }

    fun addSchedule(schedule: Schedule) {
        val schedules = getSchedules()
        schedules.add(schedule)
        saveSchedules(schedules)
    }

    fun deleteSchedule(schedule: Schedule) {
        val schedules = getSchedules()
        schedules.remove(schedule)
        saveSchedules(schedules)
    }

    fun modifySchedule(
        schedule: Schedule,
        text: String,
        startingTime: Long,
        endingTime: Long,
        isAllDay: Boolean,
        isMilestone: Boolean,
        milestoneGoal: Long
    ) {
        val schedules = getSchedules()
        val match = schedules.find { it == schedule }
        match?.let {
            it.text = text
            it.startingTime = startingTime
            it.endingTime = endingTime
            it.isAllDay = isAllDay
            it.isMilestone = isMilestone
            it.milestoneGoal = milestoneGoal
        }
        saveSchedules(schedules)
    }

    fun buildSyncRequest(): ScheduleSyncRequest? {
        val schedules = getSchedules()
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

    private fun getSchedules() = try {
        val scheduleRecords = Json.decodeFromString<ScheduleRecords>(AppStore.schedules)
        scheduleRecords.schedules
    } catch (e: Exception) {
        e.printStackTrace()
        mutableListOf()
    }

    private fun saveSchedules(schedules: MutableList<Schedule>) {
        val records = ScheduleRecords(schedules)
        AppStore.schedules = Json.encodeToString(records)
    }
}