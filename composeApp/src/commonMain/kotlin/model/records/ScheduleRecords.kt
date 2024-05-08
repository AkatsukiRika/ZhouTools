package model.records

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.DoubleToLongSerializer

@Serializable
data class ScheduleRecords(
    val schedules: MutableList<Schedule>
)

@Serializable
data class Schedule(
    var text: String = "",
    @Serializable(with = DoubleToLongSerializer::class)
    @SerialName("day_start_time")
    var dayStartTime: Long = 0L,
    @Serializable(with = DoubleToLongSerializer::class)
    @SerialName("starting_time")
    var startingTime: Long = 0L,
    @Serializable(with = DoubleToLongSerializer::class)
    @SerialName("ending_time")
    var endingTime: Long = 0L,
    @SerialName("is_all_day")
    var isAllDay: Boolean = false,
    @SerialName("is_milestone")
    var isMilestone: Boolean = false
)