package store

import PREFERENCES_NAME
import com.tangping.kotstore.model.KotStoreModel
import kotlinx.coroutines.runBlocking

object AppStore : KotStoreModel(storeName = PREFERENCES_NAME) {
    var loginToken by stringStore(key = "login_token", default = "")
    var loginUsername by stringStore(key = "login_username", default = "")
    var loginPassword by stringStore(key = "login_password", default = "")
    var customServerUrl by stringStore(key = "custom_server_url", default = "")
    var timeCards by stringStore(key = "time_cards", default = "{}", syncSave = true)
    var memos by stringStore(key = "memos", default = "{}", syncSave = true)
    var schedules by stringStore(key = "schedules", default = "{}", syncSave = true)
    var depositMonths by stringStore(key = "deposit_months", default = "{}", syncSave = true)
    var lastSync by longStore(key = "last_sync", default = 0L)
    var minWorkingHours by floatStore(key = "min_working_hours", default = 9.5f)
    var minOvertimeHours by floatStore(key = "min_overtime_hours", default = 1f)
    var totalDepositGoal by longStore(key = "total_deposit_goal", default = 0L)
    var isCurrentBalanceSet by booleanStore(key = "is_current_balance_set", default = false)
    var currentBalance by longStore(key = "current_balance", default = 0L)

    fun clearCache() {
        customServerUrl = ""
        timeCards = "{}"
        memos = "{}"
        schedules = "{}"
        depositMonths = "{}"
        lastSync = 0L
        totalDepositGoal = 0L
        isCurrentBalanceSet = false
        currentBalance = 0L
        AppFlowStore.clearLastPushStatuses()
    }

    fun setMinWorkingHoursWithFlow(hours: Float) {
        minWorkingHours = hours
        runBlocking {
            AppFlowStore.minWorkingHoursFlow.emit(hours)
        }
    }

    fun setMinOvertimeHoursWithFlow(hours: Float) {
        minOvertimeHours = hours
        runBlocking {
            AppFlowStore.minOvertimeHoursFlow.emit(hours)
        }
    }

    fun setTotalDepositGoalWithFlow(goal: Long) {
        totalDepositGoal = goal
        runBlocking {
            AppFlowStore.totalDepositGoalFlow.emit(goal)
        }
    }
}