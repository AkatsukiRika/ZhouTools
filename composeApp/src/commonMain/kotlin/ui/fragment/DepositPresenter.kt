package ui.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import extension.toMonthYearString
import kotlinx.coroutines.flow.Flow
import model.records.DepositMonth
import model.records.DepositRecords
import ui.fragment.DepositState.Companion.toDeque

@Composable
fun DepositPresenter(actionFlow: Flow<DepositAction>): DepositState {
    var currentAmountCents by remember { mutableLongStateOf(0L) }
    var displayDeque by remember { mutableStateOf(ArrayDeque<DepositDisplayRecord>()) }

    LaunchedEffect(Unit) {
        // For test purpose
        currentAmountCents = 1234567890L
        val mockRecords = DepositRecords(months = listOf(
            DepositMonth(monthStartTime = 1693497600000L, currentAmount = 38214124L, monthlyIncome = 1225302L, extraDeposit = 0L),
            DepositMonth(monthStartTime = 1696089600000L, currentAmount = 38840774L, monthlyIncome = 1451085L, extraDeposit = 0L),
            DepositMonth(monthStartTime = 1698768000000L, currentAmount = 39640942L, monthlyIncome = 1443325L, extraDeposit = 0L),
            DepositMonth(monthStartTime = 1701360000000L, currentAmount = 39596957L, monthlyIncome = 1374110L, extraDeposit = 0L),
            DepositMonth(monthStartTime = 1704038400000L, currentAmount = 41309128L, monthlyIncome = 1447205L, extraDeposit = 0L),
            DepositMonth(monthStartTime = 1706716800000L, currentAmount = 42305969L, monthlyIncome = 1525775L, extraDeposit = 0L),
            DepositMonth(monthStartTime = 1709222400000L, currentAmount = 43047583L, monthlyIncome = 1447205L, extraDeposit = 8407877L),
            DepositMonth(monthStartTime = 1711900800000L, currentAmount = 44556034L, monthlyIncome = 2008165L, extraDeposit = 8414012L),
            DepositMonth(monthStartTime = 1714492800000L, currentAmount = 44991012L, monthlyIncome = 1418450L, extraDeposit = 8883255L)
        ))
        val deque = mockRecords.toDeque()
        displayDeque = deque
    }

    return DepositState(currentAmountCents, displayDeque)
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
                deque.addFirst(DepositDisplayRecord(monthStr, currAmount, monthIncome, balance, extraAmount, balanceDiff))
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

sealed interface DepositAction