package store

import FLOW_PREFERENCES_NAME
import com.tangping.kotstore.model.KotStoreFlowModel

object AppFlowStore : KotStoreFlowModel<AppFlowStore>(storeName = FLOW_PREFERENCES_NAME) {
    val minWorkingHoursFlow by floatFlowStore(key = "min_working_hours_flow", default = 9.5f)
    val minOvertimeHoursFlow by floatFlowStore(key = "min_overtime_hours_flow", default = 1f)
    val autoSyncFlow by booleanFlowStore(key = "auto_sync_flow", default = false)
}