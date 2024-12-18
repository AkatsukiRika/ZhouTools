package ui.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import extension.dayStartTime
import helper.DepositHelper
import helper.MemoHelper
import helper.ScheduleHelper
import helper.SyncHelper
import helper.effect.EffectHelper
import helper.effect.WriteMemoEffect
import kotlinx.coroutines.flow.Flow
import logger
import model.records.DepositRecords
import model.records.GOAL_TYPE_DEPOSIT
import model.records.GOAL_TYPE_TIME
import model.records.Goal
import model.records.Memo
import moe.tlaster.precompose.molecule.collectAction
import store.AppStore
import ui.fragment.DepositState.Companion.toDeque
import util.TimeUtil

const val MODE_MEMO = 0

const val MODE_GOALS = 1

@Composable
fun MemoPresenter(actionFlow: Flow<MemoAction>, onGoEdit: () -> Unit): MemoState {
    var displayList by remember { mutableStateOf(MemoHelper.getDisplayList()) }
    var curMemo by remember { mutableStateOf<Memo?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var mode by remember { mutableIntStateOf(MODE_MEMO) }
    val goalList = mutableStateListOf<Goal>()

    fun initGoalList() {
        goalList.clear()

        // Deposit Goal
        val depositMonths = DepositHelper.getMonths()
        val deque = DepositRecords(months = depositMonths).toDeque()
        deque.firstOrNull()?.let {
            val currentDeposit = it.currentAmount + it.extraDeposit
            val goalDeposit = AppStore.totalDepositGoal * 100L
            goalList.add(Goal(GOAL_TYPE_DEPOSIT, currentDeposit, goalDeposit))
        }

        // Time Goals
        val scheduleList = ScheduleHelper.getDisplayList()
        scheduleList.forEach { schedule ->
            val todayStartTime = TimeUtil.currentTimeMillis().dayStartTime()
            val diffTime = todayStartTime - schedule.dayStartTime
            if (diffTime > 0 && schedule.milestoneGoal > 0) {
                goalList.add(Goal(GOAL_TYPE_TIME, diffTime, schedule.milestoneGoal))
            }
        }

        logger.i { "goalList: ${goalList.toList()}" }
    }

    fun clickMemoItem(memo: Memo) {
        if (memo.isTodo) {
            curMemo = memo
            showBottomSheet = true
        } else {
            EffectHelper.emitWriteMemoEffect(WriteMemoEffect.BeginEdit(memo))
            onGoEdit()
        }
    }

    fun clickEdit() {
        curMemo?.let {
            EffectHelper.emitWriteMemoEffect(WriteMemoEffect.BeginEdit(it))
            onGoEdit()
        }
    }

    LaunchedEffect(Unit) {
        initGoalList()
    }

    actionFlow.collectAction {
        when (this) {
            is MemoAction.ClickMemoItem -> {
                clickMemoItem(memo)
            }

            is MemoAction.ClickEdit -> {
                clickEdit()
                showBottomSheet = false
            }

            is MemoAction.MarkDone -> {
                curMemo?.let {
                    MemoHelper.markDone(it, !it.isTodoFinished)
                    SyncHelper.autoPushMemo()
                    curMemo = it.copy(isTodoFinished = !it.isTodoFinished)
                    displayList = MemoHelper.getDisplayList()
                }
                showBottomSheet = false
            }

            is MemoAction.HideBottomSheet -> {
                showBottomSheet = false
            }

            is MemoAction.RefreshDisplayList -> {
                displayList = MemoHelper.getDisplayList()
            }

            is MemoAction.SwitchMode -> {
                mode = newMode
            }
        }
    }

    return MemoState(displayList, curMemo, showBottomSheet, mode, goalList)
}

data class MemoState(
    val displayList: List<Memo> = emptyList(),
    val curMemo: Memo? = null,
    val showBottomSheet: Boolean = false,
    val mode: Int = MODE_MEMO,
    val goalList: List<Goal> = emptyList()
)

sealed interface MemoAction {
    data class ClickMemoItem(val memo: Memo) : MemoAction
    data object ClickEdit : MemoAction
    data object MarkDone : MemoAction
    data object HideBottomSheet : MemoAction
    data object RefreshDisplayList : MemoAction
    data class SwitchMode(val newMode: Int) : MemoAction
}