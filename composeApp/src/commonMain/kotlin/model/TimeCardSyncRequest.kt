package model

import kotlinx.serialization.Serializable

@Serializable
data class TimeCardSyncRequest(
    val username: String,
    val timeCard: TimeCardRecords
)