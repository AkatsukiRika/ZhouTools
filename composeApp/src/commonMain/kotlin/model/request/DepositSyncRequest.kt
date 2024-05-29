package model.request

import kotlinx.serialization.Serializable
import model.records.DepositMonth

@Serializable
data class DepositSyncRequest(
    val username: String,
    val depositMonths: List<DepositMonth>
)
