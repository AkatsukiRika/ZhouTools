package ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import extension.toMonthYearString
import helper.DepositHelper
import kotlinx.coroutines.flow.Flow
import model.records.DepositMonth
import ui.widget.BarData

@Composable
fun DepositStatsPresenter(actionFlow: Flow<DepositStatsAction>): DepositStatsState {
    var depositMonths by remember { mutableStateOf(listOf<DepositMonth>()) }
    var totalDepositBarData by remember { mutableStateOf(listOf<BarData<Long>>()) }

    fun initTotalDepositBar() {
        val barData = mutableListOf<BarData<Long>>()
        depositMonths.forEach {
            val monthStr = it.monthStartTime.toMonthYearString()
            val value = it.currentAmount + it.extraDeposit
            barData.add(BarData(
                value = value,
                label = monthStr
            ))
        }
        totalDepositBarData = barData
    }

    fun initData() {
        depositMonths = DepositHelper.getMonths().sortedBy { it.monthStartTime }
        initTotalDepositBar()
    }

    LaunchedEffect(Unit) {
        initData()
    }

    return DepositStatsState(totalDepositBarData)
}

data class DepositStatsState(
    val totalDepositBarData: List<BarData<Long>> = emptyList()
)

sealed interface DepositStatsAction