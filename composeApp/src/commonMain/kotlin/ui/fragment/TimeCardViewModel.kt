package ui.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import helper.SyncHelper
import helper.TimeCardHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logger
import util.TimeUtil

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

class TimeCardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TimeCardState())
    val uiState: StateFlow<TimeCardState> = _uiState.asStateFlow()

    init {
        refreshTodayState()

        viewModelScope.launch {
            while (true) {
                delay(500)
                val newTime = TimeUtil.currentTimeMillis()
                _uiState.update { currentState ->
                    val workTime = if (currentState.todayTimeCard != 0L) {
                        if (currentState.todayRunTime == 0L) {
                            newTime - currentState.todayTimeCard
                        } else {
                            currentState.todayRunTime - currentState.todayTimeCard
                        }
                    } else {
                        0L
                    }
                    currentState.copy(
                        currentTime = newTime,
                        todayWorkTime = workTime
                    )
                }
            }
        }
    }

    fun dispatch(action: TimeCardAction) {
        when (action) {
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

    private fun refreshTodayState() {
        val timeCard = TimeCardHelper.todayTimeCard() ?: 0L
        val timeRun = TimeCardHelper.todayTimeRun() ?: 0L
        logger.i { "todayTimeCard=$timeCard, todayRunTime=$timeRun" }
        _uiState.update {
            it.copy(
                todayTimeCard = timeCard,
                todayRunTime = timeRun
            )
        }
    }
}
