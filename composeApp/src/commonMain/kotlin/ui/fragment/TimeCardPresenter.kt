package ui.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import helper.NetworkHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger
import model.records.TimeCardRecords
import moe.tlaster.precompose.molecule.collectAction
import store.AppStore
import helper.TimeCardHelper
import util.TimeUtil

@Composable
fun TimeCardPresenter(actionFlow: Flow<TimeCardAction>): TimeCardState {
    var currentTime by remember { mutableLongStateOf(0L) }
    var todayTimeCard by remember { mutableLongStateOf(0L) }
    var todayWorkTime by remember { mutableLongStateOf(0L) }
    var todayRunTime by remember { mutableLongStateOf(0L) }
    var serverData by remember { mutableStateOf<TimeCardRecords?>(null) }

    fun refreshTodayState() {
        todayTimeCard = TimeCardHelper.todayTimeCard() ?: 0L
        todayRunTime = TimeCardHelper.todayTimeRun() ?: 0L
        logger.i { "todayTimeCard=$todayTimeCard, todayRunTime=$todayRunTime" }
    }

    fun useServerData() {
        serverData?.let {
            val encodeResult = Json.encodeToString(it)
            AppStore.timeCards = encodeResult
            logger.i { "AppStore.timeCards=${AppStore.timeCards}" }
            refreshTodayState()
        }
    }

    LaunchedEffect(Unit) {
        // init
        refreshTodayState()
        if (AppStore.loginToken.isNotBlank() && AppStore.loginUsername.isNotBlank()) {
            serverData = NetworkHelper.getServerTimeCards(AppStore.loginToken, AppStore.loginUsername)
        }
    }

    LaunchedEffect(Unit) {
        // update time
        while (true) {
            delay(500)
            currentTime = TimeUtil.currentTimeMillis()
            if (todayTimeCard != 0L) {
                todayWorkTime = if (todayRunTime == 0L) {
                    currentTime - todayTimeCard
                } else {
                    todayRunTime - todayTimeCard
                }
            }
        }
    }

    actionFlow.collectAction {
        when (this) {
            is TimeCardAction.PressTimeCard -> {
                TimeCardHelper.pressTimeCard()
                refreshTodayState()
            }

            is TimeCardAction.Run -> {
                if (TimeCardHelper.run()) {
                    refreshTodayState()
                }
            }

            is TimeCardAction.UseServerData -> {
                useServerData()
            }

            is TimeCardAction.RefreshTodayState -> {
                refreshTodayState()
            }
        }
    }

    return TimeCardState(currentTime, todayTimeCard, todayWorkTime, todayRunTime, serverData)
}

data class TimeCardState(
    val currentTime: Long = 0L,
    val todayTimeCard: Long = 0L,
    val todayWorkTime: Long = 0L,
    val todayRunTime: Long = 0L,
    val serverData: TimeCardRecords? = null
)

sealed interface TimeCardAction {
    data object PressTimeCard : TimeCardAction
    data object Run : TimeCardAction
    data object UseServerData : TimeCardAction
    data object RefreshTodayState : TimeCardAction
}