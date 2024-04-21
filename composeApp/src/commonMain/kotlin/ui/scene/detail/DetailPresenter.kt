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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import logger
import moe.tlaster.precompose.molecule.collectAction
import networkApi
import store.AppStore
import util.TimeCardUtil
import util.TimeUtil
import kotlin.math.max

const val DETAIL_TAB_TODAY = 0
const val DETAIL_TAB_HISTORY = 1

@Composable
fun DetailPresenter(actionFlow: Flow<DetailAction>): DetailState {
    var tab by remember { mutableIntStateOf(DETAIL_TAB_TODAY) }
    var todayState by remember { mutableStateOf(DetailTodayState()) }
    var historyState by remember { mutableStateOf(DetailHistoryState()) }

    fun initTodayData() {
        val currentTime = TimeUtil.currentTimeMillis()
        val dayStartTime = currentTime.dayStartTime()
        val todayTimeCard = TimeCardUtil.todayTimeCard() ?: 0L
        val todayTimeRun = TimeCardUtil.todayTimeRun() ?: 0L
        val todayTimeWork = if (todayTimeRun == 0L) currentTime - todayTimeCard else todayTimeRun - todayTimeCard
        val countdownRun = max(0L, TimeCardUtil.MIN_WORKING_TIME - todayTimeWork)
        val countdownOT = max(0L, TimeCardUtil.MIN_OT_TIME - todayTimeWork)
        val progress = todayTimeWork.toFloat() / TimeCardUtil.MIN_OT_TIME
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
        val serverData = networkApi.getServerTimeCards(AppStore.loginToken, AppStore.loginUsername)
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

                for (dayStartTime in weekStartTime..weekEndTime step TimeConstants.DAY_MILLIS) {
                    val matchDay = days.find { it.dayStartTime == dayStartTime }
                    matchDay?.let {
                        val timeWork = if (it.latestTimeRun != null) it.latestTimeRun!! - it.latestTimeCard else 0L
                        val weekDay = DetailHistoryWeekDay(
                            dayStartTime = it.dayStartTime,
                            timeCard = it.latestTimeCard,
                            timeRun = it.latestTimeRun ?: 0L,
                            timeWork = timeWork,
                            isOT = timeWork >= TimeCardUtil.MIN_OT_TIME
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

            historyState = DetailHistoryState(weekList)
        }
    }

    fun refreshTodayData() {
        val currentTime = TimeUtil.currentTimeMillis()
        val todayTimeCard = TimeCardUtil.todayTimeCard() ?: 0L
        val todayTimeRun = TimeCardUtil.todayTimeRun() ?: 0L
        val todayTimeWork = if (todayTimeRun == 0L) currentTime - todayTimeCard else todayTimeRun - todayTimeCard
        val countdownRun = max(0L, TimeCardUtil.MIN_WORKING_TIME - todayTimeWork)
        val countdownOT = max(0L, TimeCardUtil.MIN_OT_TIME - todayTimeWork)
        val progress = todayTimeWork.toFloat() / TimeCardUtil.MIN_OT_TIME
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
        }
    }

    return DetailState(tab, todayState, historyState)
}

sealed interface DetailAction {
    data class ChangeTab(val tab: Int) : DetailAction
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
    val weekList: List<DetailHistoryWeek> = emptyList()
)

data class DetailHistoryWeek(
    val weekStartTime: Long = 0L,
    val weekEndTime: Long = 0L,
    val days: List<DetailHistoryWeekDay> = emptyList(),
    val otDays: Int = 0
)

data class DetailHistoryWeekDay(
    val dayStartTime: Long = 0L,
    val timeCard: Long = 0L,
    val timeRun: Long = 0L,
    val timeWork: Long = 0L,
    val isOT: Boolean = false
)