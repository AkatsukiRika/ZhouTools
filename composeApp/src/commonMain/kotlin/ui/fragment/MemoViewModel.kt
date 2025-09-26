package ui.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import extension.dayStartTime
import helper.DepositHelper
import helper.MemoHelper
import helper.ScheduleHelper
import helper.SyncHelper
import helper.effect.EffectHelper
import helper.effect.WriteMemoEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import logger
import model.display.IMemoDisplayItem
import model.records.DepositRecords
import model.records.GOAL_TYPE_DEPOSIT
import model.records.GOAL_TYPE_TIME
import model.records.Goal
import model.records.Memo
import store.AppStore
import ui.fragment.DepositState.Companion.toDeque
import util.TimeUtil

const val MODE_MEMO = 0
const val MODE_GOALS = 1

data class MemoState(
    val displayList: List<IMemoDisplayItem> = emptyList(),
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
    data object InitGoals : MemoAction
}

sealed interface MemoEvent {
    data object GoToEditScene : MemoEvent
}

class MemoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MemoState())
    val uiState: StateFlow<MemoState> = _uiState.asStateFlow()

    private val _memoEvent = MutableSharedFlow<MemoEvent>()
    val memoEvent: SharedFlow<MemoEvent> = _memoEvent.asSharedFlow()

    init {
        _uiState.update { it.copy(displayList = MemoHelper.getDisplayList()) }
    }

    fun dispatch(action: MemoAction) {
        when (action) {
            is MemoAction.ClickMemoItem -> {
                clickMemoItem(action.memo)
            }

            is MemoAction.ClickEdit -> {
                clickEdit()
                _uiState.update { it.copy(showBottomSheet = false) }
            }

            is MemoAction.MarkDone -> {
                _uiState.value.curMemo?.let { memo ->
                    MemoHelper.markDone(memo, !memo.isTodoFinished)
                    SyncHelper.autoPushMemo()
                    _uiState.update {
                        it.copy(
                            curMemo = memo.copy(isTodoFinished = !memo.isTodoFinished),
                            displayList = MemoHelper.getDisplayList()
                        )
                    }
                }
                _uiState.update { it.copy(showBottomSheet = false) }
            }

            is MemoAction.HideBottomSheet -> {
                _uiState.update { it.copy(showBottomSheet = false) }
            }

            is MemoAction.RefreshDisplayList -> {
                _uiState.update { it.copy(displayList = MemoHelper.getDisplayList()) }
            }

            is MemoAction.SwitchMode -> {
                if (action.newMode != _uiState.value.mode) {
                    _uiState.update { it.copy(showBottomSheet = false) }
                }
                _uiState.update { it.copy(mode = action.newMode) }
            }

            is MemoAction.InitGoals -> {
                initGoalList()
            }
        }
    }

    private fun initGoalList() {
        val tempGoalList = mutableListOf<Goal>()

        // Deposit Goal
        val depositMonths = DepositHelper.getMonths()
        val deque = DepositRecords(months = depositMonths).toDeque()
        deque.firstOrNull()?.let {
            val currentDeposit = it.currentAmount + it.extraDeposit
            val goalDeposit = AppStore.totalDepositGoal * 100L
            if (goalDeposit > 0) {
                tempGoalList.add(Goal(GOAL_TYPE_DEPOSIT, currentDeposit, goalDeposit))
            }
        }

        // Time Goals
        val scheduleList = ScheduleHelper.getDisplayList()
        scheduleList.forEach { schedule ->
            val todayStartTime = TimeUtil.currentTimeMillis().dayStartTime()
            val diffTime = todayStartTime - schedule.dayStartTime
            if (diffTime > 0 && schedule.milestoneGoal > 0) {
                tempGoalList.add(Goal(GOAL_TYPE_TIME, diffTime, schedule.milestoneGoal))
            }
        }

        _uiState.update { it.copy(goalList = tempGoalList) }
        logger.i { "goalList: $tempGoalList" }
    }

    private fun clickMemoItem(memo: Memo) {
        if (memo.isTodo) {
            _uiState.update { it.copy(curMemo = memo, showBottomSheet = true) }
        } else {
            EffectHelper.emitWriteMemoEffect(WriteMemoEffect.BeginEdit(memo))
            viewModelScope.launch { _memoEvent.emit(MemoEvent.GoToEditScene) }
        }
    }

    private fun clickEdit() {
        _uiState.value.curMemo?.let {
            EffectHelper.emitWriteMemoEffect(WriteMemoEffect.BeginEdit(it))
            viewModelScope.launch { _memoEvent.emit(MemoEvent.GoToEditScene) }
        }
    }
}
