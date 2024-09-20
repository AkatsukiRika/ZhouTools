package helper

import helper.effect.DepositEffect
import helper.effect.EffectHelper
import helper.effect.TimeCardEffect
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.records.DepositRecords
import model.records.MemoRecords
import model.records.ScheduleRecords
import store.AppStore

object SyncHelper {
    suspend fun pullMemo(onSuccess: () -> Unit, onError: () -> Unit) {
        if (AppStore.loginToken.isNotBlank() && AppStore.loginUsername.isNotBlank()) {
            val serverData = NetworkHelper.getServerMemos(AppStore.loginToken, AppStore.loginUsername)
            if (serverData == null) {
                onError()
                return
            }
            val memoRecords = MemoRecords(memos = serverData.toMutableList())
            AppStore.memos = Json.encodeToString(memoRecords)
            AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
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
            AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
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
            AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
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
            AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
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
        AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
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
        AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
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
        AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
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
        AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
        onSuccess()
    }
}