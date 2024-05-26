package model.records

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.DoubleToLongSerializer

@Serializable
data class DepositRecords(
    val months: List<DepositMonth>
)

@Serializable
data class DepositMonth(
    @Serializable(with = DoubleToLongSerializer::class)
    @SerialName("month_start_time")
    val monthStartTime: Long = 0L,
    @Serializable(with = DoubleToLongSerializer::class)
    @SerialName("current_amount")
    val currentAmount: Long = 0L,
    @Serializable(with = DoubleToLongSerializer::class)
    @SerialName("monthly_income")
    val monthlyIncome: Long = 0L,
    @Serializable(with = DoubleToLongSerializer::class)
    @SerialName("extra_deposit")
    val extraDeposit: Long = 0L
)