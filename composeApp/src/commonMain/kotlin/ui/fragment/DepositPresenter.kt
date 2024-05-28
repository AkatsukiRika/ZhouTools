package ui.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import extension.toMonthYearString
import helper.DepositHelper
import kotlinx.coroutines.flow.Flow
import model.records.DepositMonth
import model.records.DepositRecords
import moe.tlaster.precompose.molecule.collectAction
import ui.fragment.DepositState.Companion.toDeque

@Composable
fun DepositPresenter(actionFlow: Flow<DepositAction>): DepositState {
    var currentAmount by remember { mutableLongStateOf(0L) }
    var displayDeque by remember { mutableStateOf(ArrayDeque<DepositDisplayRecord>()) }

    fun refreshData() {
        val depositMonths = DepositHelper.getMonths()
        val deque = DepositRecords(months = depositMonths).toDeque()
        displayDeque = deque
        displayDeque.firstOrNull()?.let {
            currentAmount = it.currentAmount + it.extraDeposit
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    actionFlow.collectAction {
        when (this) {
            is DepositAction.AddMonth -> {
                DepositHelper.addMonth(month)
                refreshData()
            }
        }
    }

    return DepositState(currentAmount, displayDeque)
}

data class DepositState(
    val currentAmount: Long = 0L,
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
)

sealed interface DepositAction {
    data class AddMonth(val month: DepositMonth) : DepositAction
}