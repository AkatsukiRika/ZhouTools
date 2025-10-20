package helper

import helper.effect.DepositEffect
import helper.effect.EffectHelper
import helper.effect.TimeCardEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger
import model.records.DepositRecords
import model.records.MemoRecords
import model.records.ScheduleRecords
import store.AppFlowStore
import store.AppStore
import util.TimeUtil

object SyncHelper {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isAutoPulling = MutableStateFlow(false)
    val isAutoPulling: StateFlow<Boolean> = _isAutoPulling

    private val _isAutoPushing = MutableStateFlow(false)
    val isAutoPushing: StateFlow<Boolean> = _isAutoPushing

    // Pulling will be done only once per app launch
    private var isTimeCardPulled = false
    private var isSchedulePulled = false
    private var isMemoPulled = false
    private var isDepositPulled = false

    fun clearPulledFlags() {
        isTimeCardPulled = false
        isSchedulePulled = false
        isMemoPulled = false
        isDepositPulled = false
    }

    fun autoPullTimeCard() {
        if (isTimeCardPulled) {
            return
        }
        scope.launch(Dispatchers.IO) {
            val isAutoSync = AppFlowStore.autoSyncFlow.first()
            if (isAutoSync) {
                _isAutoPulling.value = true
                pullTimeCard(
                    onSuccess = {
                        logger.i("SyncHelper") { "autoPullTimeCard success" }
                        _isAutoPulling.value = false
                    },
                    onError = {
                        logger.i("SyncHelper") { "autoPullTimeCard error" }
                        _isAutoPulling.value = false
                    }
                )
                isTimeCardPulled = true
            }
        }
    }

    fun autoPushTimeCard() {
        scope.launch(Dispatchers.IO) {
            val isAutoSync = AppFlowStore.autoSyncFlow.first()
            if (isAutoSync) {
                _isAutoPushing.value = true
                pushTimeCard(
                    onSuccess = {
                        logger.i("SyncHelper") { "autoPushTimeCard success" }
                        _isAutoPushing.value = false
                    },
                    onError = {
                        logger.i("SyncHelper") { "autoPushTimeCard error" }
                        _isAutoPushing.value = false
                    }
                )
            }
        }
    }

    fun autoPullSchedule(onSuccess: (() -> Unit)? = null) {
        if (isSchedulePulled) {
            return
        }
        scope.launch(Dispatchers.IO) {
            val isAutoSync = AppFlowStore.autoSyncFlow.first()
            if (isAutoSync) {
                _isAutoPulling.value = true
                pullSchedule(
                    onSuccess = {
                        logger.i("SyncHelper") { "autoPullSchedule success" }
                        onSuccess?.invoke()
                        _isAutoPulling.value = false
                    },
                    onError = {
                        logger.i("SyncHelper") { "autoPullSchedule error" }
                        _isAutoPulling.value = false
                    }
                )
                isSchedulePulled = true
            }
        }
    }

    fun autoPushSchedule() {
        scope.launch(Dispatchers.IO) {
            val isAutoSync = AppFlowStore.autoSyncFlow.first()
            if (isAutoSync) {
                _isAutoPushing.value = true
                pushSchedule(
                    onSuccess = {
                        logger.i("SyncHelper") { "autoPushSchedule success" }
                        _isAutoPushing.value = false
                    },
                    onError = {
                        logger.i("SyncHelper") { "autoPushSchedule error" }
                        _isAutoPushing.value = false
                    }
                )
            }
        }
    }

    fun autoPullMemo(onSuccess: (() -> Unit)? = null) {
        if (isMemoPulled) {
            return
        }
        scope.launch(Dispatchers.IO) {
            val isAutoSync = AppFlowStore.autoSyncFlow.first()
            if (isAutoSync) {
                _isAutoPulling.value = true
                pullMemo(
                    onSuccess = {
                        logger.i("SyncHelper") { "autoPullMemo success" }
                        onSuccess?.invoke()
                        _isAutoPulling.value = false
                    },
                    onError = {
                        logger.i("SyncHelper") { "autoPullMemo error" }
                        _isAutoPulling.value = false
                    }
                )
                isMemoPulled = true
            }
        }
    }

    fun autoPushMemo() {
        scope.launch(Dispatchers.IO) {
            val isAutoSync = AppFlowStore.autoSyncFlow.first()
            if (isAutoSync) {
                _isAutoPushing.value = true
                pushMemo(
                    onSuccess = {
                        logger.i("SyncHelper") { "autoPushMemo success" }
                        _isAutoPushing.value = false
                    },
                    onError = {
                        logger.i("SyncHelper") { "autoPushMemo error" }
                        _isAutoPushing.value = false
                    }
                )
            }
        }
    }

    fun autoPullDeposit(onSuccess: (() -> Unit)? = null) {
        if (isDepositPulled) {
            return
        }
        scope.launch(Dispatchers.IO) {
            val isAutoSync = AppFlowStore.autoSyncFlow.first()
            if (isAutoSync) {
                _isAutoPulling.value = true
                pullDepositMonths(
                    onSuccess = {
                        logger.i("SyncHelper") { "autoPullDeposit success" }
                        onSuccess?.invoke()
                        _isAutoPulling.value = false
                    },
                    onError = {
                        logger.i("SyncHelper") { "autoPullDeposit error" }
                        _isAutoPulling.value = false
                    }
                )
                isDepositPulled = true
            }
        }
    }

    fun autoPushDeposit() {
        scope.launch(Dispatchers.IO) {
            val isAutoSync = AppFlowStore.autoSyncFlow.first()
            if (isAutoSync) {
                _isAutoPushing.value = true
                pushDepositMonths(
                    onSuccess = {
                        logger.i("SyncHelper") { "autoPushDeposit success" }
                        _isAutoPushing.value = false
                    },
                    onError = {
                        logger.i("SyncHelper") { "autoPushDeposit error" }
                        _isAutoPushing.value = false
                    }
                )
            }
        }
    }

    suspend fun pullMemo(onSuccess: () -> Unit, onError: () -> Unit) {
        if (AppStore.loginToken.isNotBlank() && AppStore.loginUsername.isNotBlank()) {
            val serverData = NetworkHelper.getServerMemos(AppStore.loginToken, AppStore.loginUsername)
            if (serverData == null) {
                onError()
                return
            }
            val memoRecords = MemoRecords(memos = serverData.toMutableList())
            AppStore.memos = Json.encodeToString(memoRecords)
            AppStore.lastSync = TimeUtil.currentTimeMillis()
            onSuccess()
        } else {
            onError()
        }
    }

    suspend fun pullTimeCard(onSuccess: () -> Unit, onError: () -> Unit) {
        if (AppStore.loginToken.isNotBlank() && AppStore.loginUsername.isNotBlank()) {
            val serverData = NetworkHelper.getServerTimeCards(AppStore.loginToken, AppStore.loginUsername)
            if (serverData == null) {
                onError()
                return
            }
            AppStore.timeCards = Json.encodeToString(serverData)
            AppStore.lastSync = TimeUtil.currentTimeMillis()
            EffectHelper.emitTimeCardEffect(TimeCardEffect.RefreshTodayState)
            onSuccess()
        } else {
            onError()
        }
    }

    suspend fun pullSchedule(onSuccess: () -> Unit, onError: () -> Unit) {
        if (AppStore.loginToken.isNotBlank() && AppStore.loginUsername.isNotBlank()) {
            val serverData = NetworkHelper.getServerSchedules(AppStore.loginToken, AppStore.loginUsername)
            if (serverData == null) {
                onError()
                return
            }
            val scheduleRecords = ScheduleRecords(schedules = serverData.toMutableList())
            AppStore.schedules = Json.encodeToString(scheduleRecords)
            AppStore.lastSync = TimeUtil.currentTimeMillis()
            onSuccess()
        } else {
            onError()
        }
    }

    suspend fun pullDepositMonths(onSuccess: () -> Unit, onError: () -> Unit) {
        if (AppStore.loginToken.isNotBlank() && AppStore.loginUsername.isNotBlank()) {
            val serverData = NetworkHelper.getServerDepositMonths(AppStore.loginToken, AppStore.loginUsername)
            if (serverData == null) {
                onError()
                return
            }
            val depositRecords = DepositRecords(months = serverData)
            AppStore.depositMonths = Json.encodeToString(depositRecords)
            AppStore.lastSync = TimeUtil.currentTimeMillis()
            EffectHelper.emitDepositEffect(DepositEffect.RefreshData)
            onSuccess()
        } else {
            onError()
        }
    }

    suspend fun pushMemo(onError: () -> Unit, onSuccess: () -> Unit) {
        val request = MemoHelper.buildSyncRequest()
        if (request == null) {
            onError()
            return
        }
        val response = NetworkHelper.syncMemo(AppStore.loginToken, request)
        if (!response.first) {
            onError()
            return
        }
        AppStore.lastSync = TimeUtil.currentTimeMillis()
        onSuccess()
    }

    suspend fun pushTimeCard(onError: () -> Unit, onSuccess: () -> Unit) {
        val request = TimeCardHelper.buildSyncRequest()
        if (request == null) {
            onError()
            return
        }
        val response = NetworkHelper.sync(AppStore.loginToken, request)
        if (!response.first) {
            onError()
            return
        }
        AppStore.lastSync = TimeUtil.currentTimeMillis()
        onSuccess()
    }

    suspend fun pushSchedule(onError: () -> Unit, onSuccess: () -> Unit) {
        val request = ScheduleHelper.buildSyncRequest()
        if (request == null) {
            onError()
            return
        }
        val response = NetworkHelper.syncSchedule(AppStore.loginToken, request)
        if (!response.first) {
            onError()
            return
        }
        AppStore.lastSync = TimeUtil.currentTimeMillis()
        onSuccess()
    }

    suspend fun pushDepositMonths(onError: () -> Unit, onSuccess: () -> Unit) {
        val request = DepositHelper.buildSyncRequest()
        if (request == null) {
            onError()
            return
        }
        val response = NetworkHelper.syncDepositMonths(AppStore.loginToken, request)
        if (!response.first) {
            onError()
            return
        }
        AppStore.lastSync = TimeUtil.currentTimeMillis()
        onSuccess()
    }
}