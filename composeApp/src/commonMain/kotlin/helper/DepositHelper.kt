package helper

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.records.DepositMonth
import model.records.DepositRecords
import store.AppStore

object DepositHelper {
    fun getMonths() = try {
        val depositRecords = Json.decodeFromString<DepositRecords>(AppStore.depositMonths)
        depositRecords.months
    } catch (e: Exception) {
        e.printStackTrace()
        mutableListOf()
    }

    fun addMonth(month: DepositMonth) {
        val months = getMonths().toMutableList()
        months.add(month)
        saveMonths(months)
    }

    private fun saveMonths(months: List<DepositMonth>) {
        val depositRecords = DepositRecords(months)
        AppStore.depositMonths = Json.encodeToString(depositRecords)
    }
}