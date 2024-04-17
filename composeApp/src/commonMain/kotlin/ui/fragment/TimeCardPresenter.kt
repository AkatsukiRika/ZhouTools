package ui.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import extension.isBlankJson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger
import model.TimeCardRecords
import moe.tlaster.precompose.molecule.collectAction
import networkApi
import store.AppStore
import util.TimeCardUtil
import util.TimeUtil

@Composable
fun TimeCardPresenter(actionFlow: Flow<TimeCardAction>): TimeCardState {
    var currentTime by remember { mutableLongStateOf(0L) }
    var todayTimeCard by remember { mutableLongStateOf(0L) }
    var todayWorkTime by remember { mutableLongStateOf(0L) }
    var serverData by remember { mutableStateOf<TimeCardRecords?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var hasTodayRun by remember { mutableStateOf(false) }

    fun refreshTodayState() {
        todayTimeCard = TimeCardUtil.todayTimeCard() ?: 0L
        hasTodayRun = TimeCardUtil.hasTodayRun()
    }

    fun useServerData() {
        serverData?.let {
            val encodeResult = Json.encodeToString(it)
            AppStore.timeCards = encodeResult
            logger.i { "AppStore.timeCards=${AppStore.timeCards}" }
            refreshTodayState()
            showDialog = false
        }
    }

    LaunchedEffect(Unit) {
        // init
        refreshTodayState()
        if (AppStore.loginToken.isNotBlank() && AppStore.loginUsername.isNotBlank()) {
            serverData = networkApi.getServerTimeCards(AppStore.loginToken, AppStore.loginUsername)
            logger.i { "serverData=$serverData" }
            if (AppStore.timeCards.isBlankJson() && serverData != null) {
                showDialog = true
            }
        }
    }

    LaunchedEffect(Unit) {
        // update time
        while (true) {
            delay(500)
            currentTime = TimeUtil.currentTimeMillis()
            if (todayTimeCard != 0L && !hasTodayRun) {
                todayWorkTime = currentTime - todayTimeCard
            }
        }
    }

    actionFlow.collectAction {
        when (this) {
            is TimeCardAction.PressTimeCard -> {
                TimeCardUtil.pressTimeCard()
                refreshTodayState()
            }

            is TimeCardAction.Run -> {
                hasTodayRun = TimeCardUtil.run()
            }

            is TimeCardAction.CloseDialog -> {
                showDialog = false
            }

            is TimeCardAction.UseServerData -> {
                useServerData()
            }
        }
    }

    return TimeCardState(currentTime, todayTimeCard, todayWorkTime, serverData, showDialog, hasTodayRun)
}

data class TimeCardState(
    val currentTime: Long = 0L,
    val todayTimeCard: Long = 0L,
    val todayWorkTime: Long = 0L,
    val serverData: TimeCardRecords? = null,
    val showDialog: Boolean = false,
    val hasTodayRun: Boolean = false
)

sealed interface TimeCardAction {
    data object PressTimeCard : TimeCardAction
    data object Run : TimeCardAction
    data object CloseDialog : TimeCardAction
    data object UseServerData : TimeCardAction
}