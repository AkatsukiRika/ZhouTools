package ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import extension.toMonthYearString
import global.AppColors
import helper.DepositHelper
import kotlinx.coroutines.flow.Flow
import model.records.DepositMonth
import ui.widget.BarData

@Composable
fun DepositStatsPresenter(actionFlow: Flow<DepositStatsAction>): DepositStatsState {
    var depositMonths by remember { mutableStateOf(listOf<DepositMonth>()) }
    var totalDepositBarData by remember { mutableStateOf(listOf<BarData<Float>>()) }

    fun initTotalDepositBar() {
        val barData = mutableListOf<BarData<Float>>()
        depositMonths.forEach {
            val monthStr = it.monthStartTime.toMonthYearString()
            val partValues = LinkedHashMap<Color, Float>()
            partValues[AppColors.Theme] = it.currentAmount / 100f
            partValues[AppColors.LightGold] = it.extraDeposit / 100f
            barData.add(BarData(
                values = partValues,
                label = monthStr,
                valueToString = { t -> t.toInt().toString() }
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
    val totalDepositBarData: List<BarData<Float>> = emptyList()
)

sealed interface DepositStatsAction