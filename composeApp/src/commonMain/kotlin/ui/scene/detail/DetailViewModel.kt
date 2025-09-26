package ui.scene.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import constant.TimeConstants
import extension.dayStartTime
import extension.getNextMonthStartTime
import extension.getNextQuarterStartTime
import extension.getNextWeekStartTime
import extension.getNextYearStartTime
import helper.NetworkHelper
import helper.TimeCardHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logger
import store.AppStore
import util.TimeUtil
import kotlin.math.max

const val DETAIL_TAB_TODAY = 0
const val DETAIL_TAB_HISTORY = 1

enum class DetailFoldType {
    WEEK,
    MONTH,
    QUARTER,
    YEAR
}

sealed interface DetailAction {
    data class ChangeTab(val tab: Int) : DetailAction
    data class ChangeFoldType(val foldType: DetailFoldType) : DetailAction
}

data class DetailState(
    val tab: Int = DETAIL_TAB_TODAY,
    val todayState: DetailTodayState = DetailTodayState(),
    val historyState: DetailHistoryState = DetailHistoryState()
)

data class DetailTodayState(
    val dayStartTime: Long = 0L,
    val timeCard: Long = 0L,
    val timeRun: Long = 0L,
    val timeWork: Long = 0L,
    val countdownRun: Long = 0L,
    val countdownOT: Long = 0L,
    val progress: Float = 0f
)

data class DetailHistoryState(
    val weekList: List<DetailHistoryWeek> = emptyList(),
    val foldType: DetailFoldType? = null,
    val foldPeriodList: List<DetailHistoryFoldPeriod>? = null
)

data class DetailHistoryWeek(
    val weekStartTime: Long = 0L,
    val weekEndTime: Long = 0L,
    val days: List<DetailHistoryWeekDay> = emptyList(),
    val otDays: Int = 0
)

data class DetailHistoryFoldPeriod(
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val totalWorkDays: Int = 0,
    val totalWorkingTime: Long = 0L,
    val maxWorkingTime: Long = 0L,
    val minWorkingTime: Long = 0L,
    val totalOvertimeDays: Int = 0
) {
    fun getAverageWorkingTime(): Long {
        return if (totalWorkDays > 0) {
            totalWorkingTime / totalWorkDays
        } else {
            0L
        }
    }
}

data class DetailHistoryWeekDay(
    val dayStartTime: Long = 0L,
    val timeCard: Long = 0L,
    val timeRun: Long = 0L,
    val timeWork: Long = 0L,
    val isOT: Boolean = false
)

class DetailViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DetailState())
    val uiState: StateFlow<DetailState> = _uiState.asStateFlow()

    init {
        initTodayData()
        viewModelScope.launch {
            initHistoryData()
        }

        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(500)
                refreshTodayData()
            }
        }
    }

    fun dispatch(action: DetailAction) {
        when (action) {
            is DetailAction.ChangeTab -> {
                if (action.tab in listOf(DETAIL_TAB_TODAY, DETAIL_TAB_HISTORY)) {
                    _uiState.update { it.copy(tab = action.tab) }
                }
            }

            is DetailAction.ChangeFoldType -> {
                if (_uiState.value.historyState.foldType == action.foldType) {
                    updateFoldPeriod(null)
                } else {
                    updateFoldPeriod(action.foldType)
                }
            }
        }
    }

    private fun initTodayData() {
        val currentTime = TimeUtil.currentTimeMillis()
        val dayStartTime = currentTime.dayStartTime()
        val todayTimeCard = TimeCardHelper.todayTimeCard() ?: 0L
        val todayTimeRun = TimeCardHelper.todayTimeRun() ?: 0L
        val todayTimeWork = if (todayTimeCard == 0L) 0L else if (todayTimeRun == 0L) currentTime - todayTimeCard else todayTimeRun - todayTimeCard
        val countdownRun = max(0L, TimeCardHelper.getMinWorkingTimeMillis() - todayTimeWork)
        val countdownOT = max(0L, TimeCardHelper.getMinOvertimeMillis() - todayTimeWork)
        val progress = if (TimeCardHelper.getMinOvertimeMillis() == 0L) 0f else todayTimeWork.toFloat() / TimeCardHelper.getMinOvertimeMillis()
        val newTodayState = DetailTodayState(
            dayStartTime = dayStartTime,
            timeCard = todayTimeCard,
            timeRun = todayTimeRun,
            timeWork = todayTimeWork,
            countdownRun = countdownRun,
            countdownOT = countdownOT,
            progress = progress
        )
        _uiState.update { it.copy(todayState = newTodayState) }
        logger.i { "initTodayData, todayState: $newTodayState" }
    }

    private suspend fun initHistoryData() {
        val serverData = NetworkHelper.getServerTimeCards(AppStore.loginToken, AppStore.loginUsername)
        if (serverData == null) {
            logger.e { "initHistoryData failed: serverData is null" }
            return
        }
        val days = serverData.days
        if (days.isNotEmpty()) {
            val minDayStartTime = days.minBy { it.dayStartTime }.dayStartTime
            val maxDayStartTime = days.maxBy { it.dayStartTime }.dayStartTime
            val weekList = mutableListOf<DetailHistoryWeek>()

            var currentWeekStartTime = minDayStartTime.dayStartTime()
            while(currentWeekStartTime <= maxDayStartTime) {
                val weekEndTime = currentWeekStartTime + TimeConstants.WEEK_MILLIS
                val weekDays = mutableListOf<DetailHistoryWeekDay>()
                var otDays = 0

                val daysInWeek = days.filter { it.dayStartTime >= currentWeekStartTime && it.dayStartTime < weekEndTime }

                for (day in daysInWeek) {
                    val timeWork = if (day.latestTimeRun != null) day.latestTimeRun!! - day.latestTimeCard else 0L
                    val weekDay = DetailHistoryWeekDay(
                        dayStartTime = day.dayStartTime,
                        timeCard = day.latestTimeCard,
                        timeRun = day.latestTimeRun ?: 0L,
                        timeWork = timeWork,
                        isOT = timeWork >= TimeCardHelper.getMinOvertimeMillis()
                    )
                    weekDays.add(weekDay)
                    if (weekDay.isOT) {
                        otDays++
                    }
                }

                if (weekDays.isNotEmpty()) {
                     val week = DetailHistoryWeek(
                        weekStartTime = currentWeekStartTime,
                        weekEndTime = weekEndTime,
                        days = weekDays.sortedBy { it.dayStartTime },
                        otDays = otDays
                    )
                    weekList.add(week)
                }
                currentWeekStartTime = weekEndTime
            }

            _uiState.update { it.copy(historyState = it.historyState.copy(weekList = weekList.reversed())) }
        }
    }

    private fun updateFoldPeriod(foldType: DetailFoldType?) {
        val currentHistoryState = _uiState.value.historyState
        if (foldType == null) {
            _uiState.update { it.copy(historyState = currentHistoryState.copy(foldType = null, foldPeriodList = null)) }
            return
        }

        val days = currentHistoryState.weekList.flatMap { it.days }.sortedBy { it.dayStartTime }
        if (days.isEmpty()) {
            _uiState.update { it.copy(historyState = currentHistoryState.copy(foldType = null, foldPeriodList = null)) }
            return
        }

        val minDayStartTime = days.first().dayStartTime
        val maxDayStartTime = days.last().dayStartTime
        var periodStartTime = minDayStartTime
        val periodList = mutableListOf<DetailHistoryFoldPeriod>()
        while (periodStartTime <= maxDayStartTime) {
            val startTime = periodStartTime
            val endTime = when (foldType) {
                DetailFoldType.WEEK -> periodStartTime.getNextWeekStartTime()
                DetailFoldType.MONTH -> periodStartTime.getNextMonthStartTime()
                DetailFoldType.QUARTER -> periodStartTime.getNextQuarterStartTime()
                DetailFoldType.YEAR -> periodStartTime.getNextYearStartTime()
            }
            val daysInPeriod = days.filter { it.dayStartTime in startTime until endTime }
            val workingDays = daysInPeriod.filter { it.timeWork > 0L }
            val totalWorkingTime = workingDays.sumOf { it.timeWork }
            val maxWorkingTime = workingDays.maxOfOrNull { it.timeWork } ?: 0L
            val minWorkingTime = workingDays.minOfOrNull { it.timeWork } ?: 0L
            val totalOvertimeDays = workingDays.count { it.isOT }
            val period = DetailHistoryFoldPeriod(
                startTime = startTime,
                endTime = endTime,
                totalWorkDays = workingDays.size,
                totalWorkingTime = totalWorkingTime,
                maxWorkingTime = maxWorkingTime,
                minWorkingTime = minWorkingTime,
                totalOvertimeDays = totalOvertimeDays
            )
            periodList.add(period)
            periodStartTime = endTime
        }
        _uiState.update { it.copy(historyState = currentHistoryState.copy(foldType = foldType, foldPeriodList = periodList.reversed())) }
    }

    private fun refreshTodayData() {
        val todayState = _uiState.value.todayState
        if (todayState.timeCard == 0L) return

        val currentTime = TimeUtil.currentTimeMillis()
        val todayTimeRun = TimeCardHelper.todayTimeRun() ?: 0L
        val todayTimeWork = if (todayTimeRun == 0L) currentTime - todayState.timeCard else todayTimeRun - todayState.timeCard
        val countdownRun = max(0L, TimeCardHelper.getMinWorkingTimeMillis() - todayTimeWork)
        val countdownOT = max(0L, TimeCardHelper.getMinOvertimeMillis() - todayTimeWork)
        val progress = if (TimeCardHelper.getMinOvertimeMillis() == 0L) 0f else todayTimeWork.toFloat() / TimeCardHelper.getMinOvertimeMillis()
        _uiState.update {
            it.copy(
                todayState = todayState.copy(
                    timeWork = todayTimeWork,
                    countdownRun = countdownRun,
                    countdownOT = countdownOT,
                    progress = progress,
                    timeRun = todayTimeRun
                )
            )
        }
    }
}
