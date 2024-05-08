package ui.scene

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import global.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger
import model.records.MemoRecords
import moe.tlaster.precompose.navigation.Navigator
import networkApi
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import store.AppStore
import ui.fragment.TimeCardEvent
import ui.fragment.TimeCardEventFlow
import util.MemoUtil
import util.TimeCardUtil
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.pull_failed
import zhoutools.composeapp.generated.resources.pull_success
import zhoutools.composeapp.generated.resources.pulling_memo
import zhoutools.composeapp.generated.resources.pulling_time_card
import zhoutools.composeapp.generated.resources.pushing_memo
import zhoutools.composeapp.generated.resources.pushing_time_card
import zhoutools.composeapp.generated.resources.sync_failed
import zhoutools.composeapp.generated.resources.sync_success
import kotlin.math.roundToInt

enum class ProcessState(val value: Int) {
    PUSHING_MEMO(0),
    PUSHING_TIME_CARD(1),
    PULLING_MEMO(10),
    PULLING_TIME_CARD(11),
    SYNC_FAILED(20),
    SYNC_SUCCESS(21),
    PULL_FAILED(22),
    PULL_SUCCESS(23)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SyncScene(navigator: Navigator, mode: String) {
    var progressValue by remember { mutableFloatStateOf(0f) }
    val progressState by animateFloatAsState(targetValue = progressValue)
    val processStates = remember { mutableStateListOf<Int>() }
    val memoUtil = remember { MemoUtil() }

    suspend fun goBack() {
        delay(1000)
        navigator.goBack()
    }

    fun onError() {
        if (mode == "push") {
            processStates.add(ProcessState.SYNC_FAILED.value)
        } else if (mode == "pull") {
            processStates.add(ProcessState.PULL_FAILED.value)
        }
    }

    fun onSuccess() {
        if (mode == "push") {
            processStates.add(ProcessState.SYNC_SUCCESS.value)
        } else if (mode == "pull") {
            processStates.add(ProcessState.PULL_SUCCESS.value)
        }
    }

    suspend fun pullTimeCard() {
        if (AppStore.loginToken.isNotBlank() && AppStore.loginUsername.isNotBlank()) {
            val serverData = networkApi.getServerTimeCards(AppStore.loginToken, AppStore.loginUsername)
            if (serverData == null) {
                onError()
                return
            }
            AppStore.timeCards = Json.encodeToString(serverData)
            logger.i { "pull success: ${AppStore.timeCards}" }
            AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
            TimeCardEventFlow.emit(TimeCardEvent.RefreshTodayState)
            onSuccess()
        } else {
            onError()
        }
    }

    suspend fun pushTimeCard() {
        val request = TimeCardUtil.buildSyncRequest()
        if (request == null) {
            onError()
            return
        }
        val response = networkApi.sync(AppStore.loginToken, request)
        if (!response.first) {
            onError()
            return
        }
        AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
        onSuccess()
    }

    suspend fun pullMemo() {
        if (AppStore.loginToken.isNotBlank() && AppStore.loginUsername.isNotBlank()) {
            val serverData = networkApi.getServerMemos(AppStore.loginToken, AppStore.loginUsername)
            if (serverData == null) {
                onError()
                return
            }
            val memoRecords = MemoRecords(memos = serverData.toMutableList())
            AppStore.memos = Json.encodeToString(memoRecords)
            logger.i { "pull success: ${AppStore.memos}" }
            AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
            onSuccess()
        } else {
            onError()
        }
    }

    suspend fun pushMemo() {
        val request = memoUtil.buildSyncRequest()
        if (request == null) {
            onError()
            return
        }
        val response = networkApi.syncMemo(AppStore.loginToken, request)
        if (!response.first) {
            onError()
            return
        }
        AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
        onSuccess()
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            if (mode == "push") {
                pushMemo()
                progressValue = 0.5f
                pushTimeCard()
                progressValue = 1f
            } else if (mode == "pull") {
                pullMemo()
                progressValue = 0.5f
                pullTimeCard()
                progressValue = 1f
            }
            goBack()
        }
    }

    LaunchedEffect(progressValue) {
        if (mode == "push") {
            if (progressValue >= 0f && ProcessState.PUSHING_MEMO.value !in processStates) {
                processStates.add(ProcessState.PUSHING_MEMO.value)
            }
            if (progressValue >= 0.5f && ProcessState.PUSHING_TIME_CARD.value !in processStates) {
                processStates.add(ProcessState.PUSHING_TIME_CARD.value)
            }
        } else if (mode == "pull") {
            if (progressValue >= 0f && ProcessState.PULLING_MEMO.value !in processStates) {
                processStates.add(ProcessState.PULLING_MEMO.value)
            }
            if (progressValue >= 0.5f && ProcessState.PULLING_TIME_CARD.value !in processStates) {
                processStates.add(ProcessState.PULLING_TIME_CARD.value)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(AppColors.SlightTheme, Color.White))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progressState },
                trackColor = AppColors.Divider,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 8.dp
            )

            Text(
                text = "${(progressState * 100).roundToInt()}%",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(modifier = Modifier
            .padding(top = 64.dp)
            .height(80.dp)
        ) {
            items(processStates) {
                val text = when (it) {
                    ProcessState.PUSHING_MEMO.value -> stringResource(Res.string.pushing_memo)
                    ProcessState.PUSHING_TIME_CARD.value -> stringResource(Res.string.pushing_time_card)
                    ProcessState.PULLING_MEMO.value -> stringResource(Res.string.pulling_memo)
                    ProcessState.PULLING_TIME_CARD.value -> stringResource(Res.string.pulling_time_card)
                    ProcessState.SYNC_FAILED.value -> stringResource(Res.string.sync_failed)
                    ProcessState.SYNC_SUCCESS.value -> stringResource(Res.string.sync_success)
                    ProcessState.PULL_FAILED.value -> stringResource(Res.string.pull_failed)
                    ProcessState.PULL_SUCCESS.value -> stringResource(Res.string.pull_success)
                    else -> ""
                }
                if (it in listOf(ProcessState.SYNC_FAILED.value, ProcessState.PULL_FAILED.value)) {
                    Text(
                        text,
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = AppColors.Red
                    )
                } else if (it in listOf(ProcessState.SYNC_SUCCESS.value, ProcessState.PULL_SUCCESS.value)) {
                    Text(
                        text,
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = AppColors.DarkGreen
                    )
                } else if (text.isNotEmpty()) {
                    Text(
                        text,
                        modifier = Modifier.padding(bottom = 4.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}