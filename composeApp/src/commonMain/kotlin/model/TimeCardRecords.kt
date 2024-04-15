package model

import kotlinx.serialization.Serializable

@Serializable
data class TimeCardRecords(
    val days: MutableList<TimeCardDay>
)

@Serializable
data class TimeCardDay(
    val dayStartTime: Long,
    var latestTimeCard: Long,
    var latestTimeRun: Long? = null
)