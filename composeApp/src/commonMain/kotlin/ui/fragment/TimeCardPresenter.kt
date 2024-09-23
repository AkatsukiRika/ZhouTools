package ui.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import helper.SyncHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import logger
import moe.tlaster.precompose.molecule.collectAction
import helper.TimeCardHelper
import util.TimeUtil

@Composable
fun TimeCardPresenter(actionFlow: Flow<TimeCardAction>): TimeCardState {
    var currentTime by remember { mutableLongStateOf(0L) }
    var todayTimeCard by remember { mutableLongStateOf(0L) }
    var todayWorkTime by remember { mutableLongStateOf(0L) }
    var todayRunTime by remember { mutableLongStateOf(0L) }

    fun refreshTodayState() {
        todayTimeCard = TimeCardHelper.todayTimeCard() ?: 0L
        todayRunTime = TimeCardHelper.todayTimeRun() ?: 0L
        logger.i { "todayTimeCard=$todayTimeCard, todayRunTime=$todayRunTime" }
    }

    LaunchedEffect(Unit) {
        // init
        refreshTodayState()
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
                SyncHelper.autoPushTimeCard()
            }

            is TimeCardAction.Run -> {
                if (TimeCardHelper.run()) {
                    refreshTodayState()
                    SyncHelper.autoPushTimeCard()
                }
            }

            is TimeCardAction.RefreshTodayState -> {
                refreshTodayState()
            }
        }
    }

    return TimeCardState(currentTime, todayTimeCard, todayWorkTime, todayRunTime)
}

data class TimeCardState(
    val currentTime: Long = 0L,
    val todayTimeCard: Long = 0L,
    val todayWorkTime: Long = 0L,
    val todayRunTime: Long = 0L
)

sealed interface TimeCardAction {
    data object PressTimeCard : TimeCardAction
    data object Run : TimeCardAction
    data object RefreshTodayState : TimeCardAction
}