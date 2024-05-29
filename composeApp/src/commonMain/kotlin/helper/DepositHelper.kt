package helper

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.records.DepositMonth
import model.records.DepositRecords
import model.request.DepositSyncRequest
import store.AppStore

object DepositHelper {
    fun buildSyncRequest(): DepositSyncRequest? {
        val months = getMonths()
        return try {
            if (AppStore.loginUsername.isEmpty()) {
                null
            } else {
                DepositSyncRequest(username = AppStore.loginUsername, depositMonths = months)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getMonths() = try {
        val depositRecords = Json.decodeFromString<DepositRecords>(AppStore.depositMonths)
        depositRecords.months
    } catch (e: Exception) {
        e.printStackTrace()
        mutableListOf()
    }

    fun addMonth(month: DepositMonth) {
        val months = getMonths().toMutableList()
        val alreadyMonth = months.find { it.monthStartTime == month.monthStartTime }
        if (alreadyMonth != null) {
            months.remove(alreadyMonth)
        }
        months.add(month)
        saveMonths(months)
    }

    fun removeMonth(month: DepositMonth) {
        val months = getMonths().toMutableList()
        months.remove(month)
        saveMonths(months)
    }

    private fun saveMonths(months: List<DepositMonth>) {
        val depositRecords = DepositRecords(months)
        AppStore.depositMonths = Json.encodeToString(depositRecords)
    }
}