package ui.fragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import extension.toMonthYearString
import helper.DepositHelper
import helper.SyncHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.records.DepositMonth
import model.records.DepositRecords
import store.AppFlowStore
import ui.fragment.DepositState.Companion.toDeque
import util.TimeUtil

class DepositViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DepositState())
    val uiState: StateFlow<DepositState> = _uiState.asStateFlow()

    private val depositGoal = AppFlowStore.totalDepositGoalFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    init {
        refreshData()
        viewModelScope.launch {
            depositGoal.collect { goal ->
                val currentAmount = _uiState.value.currentAmount
                val newProgress = if (goal == 0L) {
                    0f
                } else {
                    currentAmount.toFloat() / (goal.toFloat() * 100)
                }
                _uiState.update { it.copy(progress = newProgress) }
            }
        }
    }

    fun dispatch(action: DepositAction) {
        when (action) {
            is DepositAction.AddMonth -> {
                DepositHelper.addMonth(action.month)
                SyncHelper.autoPushDeposit()
                refreshData()
            }

            is DepositAction.RemoveMonth -> {
                DepositHelper.removeMonth(action.month)
                SyncHelper.autoPushDeposit()
                refreshData()
            }

            is DepositAction.RefreshData -> {
                refreshData()
            }
        }
    }

    private fun refreshData() {
        val depositMonths = DepositHelper.getMonths()
        val deque = DepositRecords(months = depositMonths).toDeque()
        val newCurrentAmount = deque.firstOrNull()?.let { it.currentAmount + it.extraDeposit } ?: 0L
        val depositGoalValue = depositGoal.value
        val newProgress = if (depositGoalValue == 0L) {
            0f
        } else {
            newCurrentAmount.toFloat() / (depositGoalValue.toFloat() * 100)
        }

        _uiState.update {
            it.copy(
                displayDeque = deque,
                currentAmount = newCurrentAmount,
                progress = newProgress
            )
        }
    }
}


data class DepositState(
    val currentAmount: Long = 0L,
    val progress: Float = 0f,
    val displayDeque: ArrayDeque<DepositDisplayRecord> = ArrayDeque()
) {
    companion object {
        fun DepositRecords.toDeque(): ArrayDeque<DepositDisplayRecord> {
            val sortedMonths = this.months.sortedByDescending { it.monthStartTime }
            val deque = ArrayDeque<DepositDisplayRecord>()
            sortedMonths.forEachIndexed { index, month ->
                val monthStr = month.monthStartTime.toMonthYearString()
                val currAmount = month.currentAmount
                val monthIncome = month.monthlyIncome
                val balance = currAmount - monthIncome
                val extraAmount = month.extraDeposit
                var balanceDiff: Long? = null
                if (index < sortedMonths.lastIndex) {
                    val nextMonth = sortedMonths[index + 1]
                    val nextMonthBalance = nextMonth.currentAmount - nextMonth.monthlyIncome
                    balanceDiff = balance - nextMonthBalance
                }
                deque.add(DepositDisplayRecord(monthStr, currAmount, monthIncome, balance, extraAmount, balanceDiff))
            }
            return deque
        }
    }
}

data class DepositDisplayRecord(
    val monthStr: String = "",
    val currentAmount: Long = 0L,
    val monthlyIncome: Long = 0L,
    val balance: Long = 0L,
    val extraDeposit: Long = 0L,
    val balanceDiff: Long? = null
) {
    fun toDepositMonth(): DepositMonth? {
        val monthStartTime = TimeUtil.monthYearStringToMonthStartTime(monthStr)
        return if (monthStartTime != null) {
            DepositMonth(monthStartTime, currentAmount, monthlyIncome, extraDeposit)
        } else {
            null
        }
    }
}

sealed interface DepositAction {
    data class AddMonth(val month: DepositMonth) : DepositAction
    data class RemoveMonth(val month: DepositMonth) : DepositAction
    data object RefreshData : DepositAction
}