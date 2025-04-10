package ui.scene.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import constant.TimeConstants
import extension.dayStartTime
import extension.getNextMonthStartTime
import extension.getNextQuarterStartTime
import extension.getNextWeekStartTime
import extension.getNextYearStartTime
import helper.NetworkHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import logger
import moe.tlaster.precompose.molecule.collectAction
import store.AppStore
import helper.TimeCardHelper
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

@Composable
fun DetailPresenter(actionFlow: Flow<DetailAction>): DetailState {
    var tab by remember { mutableIntStateOf(DETAIL_TAB_TODAY) }
    var todayState by remember { mutableStateOf(DetailTodayState()) }
    var historyState by remember { mutableStateOf(DetailHistoryState()) }

    fun initTodayData() {
        val currentTime = TimeUtil.currentTimeMillis()
        val dayStartTime = currentTime.dayStartTime()
        val todayTimeCard = TimeCardHelper.todayTimeCard() ?: 0L
        val todayTimeRun = TimeCardHelper.todayTimeRun() ?: 0L
        val todayTimeWork = if (todayTimeRun == 0L) currentTime - todayTimeCard else todayTimeRun - todayTimeCard
        val countdownRun = max(0L, TimeCardHelper.getMinWorkingTimeMillis().toLong() - todayTimeWork)
        val countdownOT = max(0L, TimeCardHelper.getMinOvertimeMillis().toLong() - todayTimeWork)
        val progress = todayTimeWork.toFloat() / TimeCardHelper.getMinOvertimeMillis()
        todayState = DetailTodayState(
            dayStartTime = dayStartTime,
            timeCard = todayTimeCard,
            timeRun = todayTimeRun,
            timeWork = todayTimeWork,
            countdownRun = countdownRun,
            countdownOT = countdownOT,
            progress = progress
        )
        logger.i { "initTodayData, todayState: $todayState" }
    }

    suspend fun initHistoryData() {
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

            for (weekStartTime in minDayStartTime..maxDayStartTime step TimeConstants.WEEK_MILLIS) {
                val weekEndTime = weekStartTime + TimeConstants.WEEK_MILLIS
                val weekDays = mutableListOf<DetailHistoryWeekDay>()
                var otDays = 0

                for (dayStartTime in weekStartTime until weekEndTime step TimeConstants.DAY_MILLIS) {
                    val matchDay = days.find { it.dayStartTime == dayStartTime }
                    matchDay?.let {
                        val timeWork = if (it.latestTimeRun != null) it.latestTimeRun!! - it.latestTimeCard else 0L
                        val weekDay = DetailHistoryWeekDay(
                            dayStartTime = it.dayStartTime,
                            timeCard = it.latestTimeCard,
                            timeRun = it.latestTimeRun ?: 0L,
                            timeWork = timeWork,
                            isOT = timeWork >= TimeCardHelper.getMinOvertimeMillis()
                        )
                        weekDays.add(weekDay)
                        if (weekDay.isOT) {
                            otDays++
                        }
                    }
                }

                val week = DetailHistoryWeek(
                    weekStartTime = weekStartTime,
                    weekEndTime = weekEndTime,
                    days = weekDays,
                    otDays = otDays
                )
                weekList.add(week)
            }

            historyState = DetailHistoryState(weekList.reversed())
        }
    }

    fun updateFoldPeriod(foldType: DetailFoldType?) {
        if (foldType == null) {
            historyState = historyState.copy(foldType = null, foldPeriodList = null)
            return
        }

        val days = historyState.weekList.flatMap { it.days }.sortedBy { it.dayStartTime }
        if (days.isEmpty()) {
            historyState = historyState.copy(foldType = null, foldPeriodList = null)
            return
        }

        val minDayStartTime = days.minBy { it.dayStartTime }.dayStartTime
        val maxDayStartTime = days.maxBy { it.dayStartTime }.dayStartTime
        var periodStartTime = minDayStartTime
        val periodList = mutableListOf<DetailHistoryFoldPeriod>()
        while (periodStartTime < maxDayStartTime) {
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
            // Go to the next period
            periodList.add(period)
            periodStartTime = endTime
        }
        historyState = historyState.copy(foldType = foldType, foldPeriodList = periodList.reversed())
    }

    fun refreshTodayData() {
        val currentTime = TimeUtil.currentTimeMillis()
        val todayTimeCard = TimeCardHelper.todayTimeCard() ?: 0L
        val todayTimeRun = TimeCardHelper.todayTimeRun() ?: 0L
        val todayTimeWork = if (todayTimeRun == 0L) currentTime - todayTimeCard else todayTimeRun - todayTimeCard
        val countdownRun = max(0L, TimeCardHelper.getMinWorkingTimeMillis().toLong() - todayTimeWork)
        val countdownOT = max(0L, TimeCardHelper.getMinOvertimeMillis().toLong() - todayTimeWork)
        val progress = todayTimeWork.toFloat() / TimeCardHelper.getMinOvertimeMillis()
        todayState = todayState.copy(
            timeWork = todayTimeWork,
            countdownRun = countdownRun,
            countdownOT = countdownOT,
            progress = progress
        )
    }

    LaunchedEffect(Unit) {
        initTodayData()
        initHistoryData()

        launch(Dispatchers.IO) {
            while (true) {
                delay(500)
                refreshTodayData()
            }
        }
    }

    actionFlow.collectAction {
        when (this) {
            is DetailAction.ChangeTab -> {
                if (this.tab in listOf(DETAIL_TAB_TODAY, DETAIL_TAB_HISTORY)) {
                    tab = this.tab
                }
            }

            is DetailAction.ChangeFoldType -> {
                if (historyState.foldType == foldType) {
                    updateFoldPeriod(null)
                } else {
                    updateFoldPeriod(foldType)
                }
            }
        }
    }

    return DetailState(tab, todayState, historyState)
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