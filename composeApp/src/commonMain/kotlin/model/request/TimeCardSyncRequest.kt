package model.request

import kotlinx.serialization.Serializable
import model.records.TimeCardRecords

@Serializable
data class TimeCardSyncRequest(
    val username: String,
    val timeCard: TimeCardRecords
)