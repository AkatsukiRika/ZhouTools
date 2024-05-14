package model.request

import kotlinx.serialization.Serializable
import model.records.Schedule

@Serializable
data class ScheduleSyncRequest(
    val username: String,
    val schedules: List<Schedule>
)
