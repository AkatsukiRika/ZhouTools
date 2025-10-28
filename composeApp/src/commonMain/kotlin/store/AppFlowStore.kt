package store

import FLOW_PREFERENCES_NAME
import com.tangping.kotstore.model.KotStoreFlowModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import logger

object AppFlowStore : KotStoreFlowModel<AppFlowStore>(storeName = FLOW_PREFERENCES_NAME) {
    const val STATUS_NONE = -1
    const val STATUS_SUCCESS = 0
    const val STATUS_FAIL = 1

    val minWorkingHoursFlow by floatFlowStore(key = "min_working_hours_flow", default = 9.5f)
    val minOvertimeHoursFlow by floatFlowStore(key = "min_overtime_hours_flow", default = 1f)
    val autoSyncFlow by booleanFlowStore(key = "auto_sync_flow", default = false)
    val totalDepositGoalFlow by longFlowStore(key = "total_deposit_goal_flow", default = 0L)
    val lastPushTimeCardStatus by intFlowStore(key = "last_push_time_card_status_flow", default = STATUS_NONE)
    val lastPushScheduleStatus by intFlowStore(key = "last_push_schedule_status_flow", default = STATUS_NONE)
    val lastPushMemoStatus by intFlowStore(key = "last_push_memo_status_flow", default = STATUS_NONE)
    val lastPushDepositStatus by intFlowStore(key = "last_push_deposit_status_flow", default = STATUS_NONE)

    private val _lastPushFailed = MutableStateFlow(false)
    val lastPushFailed: StateFlow<Boolean> = _lastPushFailed

    init {
        scope.launch {
            combine(lastPushTimeCardStatus, lastPushScheduleStatus, lastPushMemoStatus, lastPushDepositStatus) { f1, f2, f3, f4 ->
                logger.i { "f1 = $f1, f2 = $f2, f3 = $f3, f4 = $f4" }
                _lastPushFailed.emit(f1 == STATUS_FAIL || f2 == STATUS_FAIL || f3 == STATUS_FAIL || f4 == STATUS_FAIL)
            }.collect {}
        }
    }

    fun setLastPushTimeCardStatus(status: Int) {
        lastPushTimeCardStatus.emitIn(scope, status)
        autoSyncFlow.emitIn(scope, false)
    }

    fun setLastPushScheduleStatus(status: Int) {
        lastPushScheduleStatus.emitIn(scope, status)
        autoSyncFlow.emitIn(scope, false)
    }

    fun setLastPushMemoStatus(status: Int) {
        lastPushMemoStatus.emitIn(scope, status)
        autoSyncFlow.emitIn(scope, false)
    }

    fun setLastPushDepositStatus(status: Int) {
        lastPushDepositStatus.emitIn(scope, status)
        autoSyncFlow.emitIn(scope, false)
    }
}