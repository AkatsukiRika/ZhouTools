package model.records

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.DoubleToLongSerializer

@Serializable
data class TimeCardRecords(
    val days: MutableList<TimeCardDay>
)

@Serializable
data class TimeCardDay(
    @Serializable(with = DoubleToLongSerializer::class)
    @SerialName("day_start_time")
    val dayStartTime: Long,
    @Serializable(with = DoubleToLongSerializer::class)
    @SerialName("latest_time_card")
    var latestTimeCard: Long,
    @Serializable(with = DoubleToLongSerializer::class)
    @SerialName("latest_time_run")
    var latestTimeRun: Long? = null
)