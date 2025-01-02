package ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import extension.getYear
import extension.toMonthYearString
import global.AppColors
import helper.DepositHelper
import kotlinx.coroutines.flow.Flow
import model.records.DepositMonth
import moe.tlaster.precompose.molecule.collectAction
import ui.widget.BarData
import ui.widget.LineData

@Composable
fun DepositStatsPresenter(actionFlow: Flow<DepositStatsAction>): DepositStatsState {
    var depositMonths by remember { mutableStateOf(listOf<DepositMonth>()) }
    var totalDepositBarData by remember { mutableStateOf(listOf<BarData<Float>>()) }
    var incomeLineData by remember { mutableStateOf(listOf<LineData<Float>>()) }
    var filterOptions by remember { mutableStateOf(LinkedHashMap<String, Boolean>()) }

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

    fun initIncomeLineChart() {
        val lineData = mutableListOf<LineData<Float>>()
        depositMonths.forEach {
            val monthStr = it.monthStartTime.toMonthYearString()
            val values = LinkedHashMap<Color, Float>().apply {
                put(AppColors.Theme, it.monthlyIncome / 100f)
            }
            lineData.add(LineData(
                values = values,
                label = monthStr,
                valueToString = { t -> t.toInt().toString() }
            ))
        }
        incomeLineData = lineData
    }

    fun initFilterOptions() {
        var yearSet = mutableSetOf<Int>()
        depositMonths.forEach {
            yearSet.add(it.monthStartTime.getYear())
        }
        yearSet = yearSet.sortedByDescending { it }.toMutableSet()
        val options = LinkedHashMap<String, Boolean>()
        yearSet.forEach {
            options[it.toString()] = true
        }
        filterOptions = options
    }

    fun initData() {
        depositMonths = DepositHelper.getMonths().sortedBy { it.monthStartTime }
        initTotalDepositBar()
        initIncomeLineChart()
        initFilterOptions()
    }

    fun refreshData(newDepositMonths: List<DepositMonth>) {
        depositMonths = newDepositMonths.sortedBy { it.monthStartTime }
        initTotalDepositBar()
        initIncomeLineChart()
    }

    fun onSelectOption(option: String, select: Boolean) {
        val newFilterOptions = LinkedHashMap<String, Boolean>()
        filterOptions.forEach {
            if (it.key == option) {
                newFilterOptions[it.key] = select
            } else {
                newFilterOptions[it.key] = filterOptions[it.key] ?: false
            }
        }
        filterOptions = newFilterOptions
        val selectedYears = filterOptions.filter { it.value }.map { it.key.toInt() }
        refreshData(DepositHelper.getMonths().filter { selectedYears.contains(it.monthStartTime.getYear()) })
    }

    LaunchedEffect(Unit) {
        initData()
    }

    actionFlow.collectAction {
        when (this) {
            is DepositStatsAction.SelectOption -> {
                onSelectOption(option, select)
            }
        }
    }

    return DepositStatsState(totalDepositBarData, incomeLineData, filterOptions)
}

data class DepositStatsState(
    val totalDepositBarData: List<BarData<Float>> = emptyList(),
    val incomeLineData: List<LineData<Float>> = emptyList(),
    val filterOptions: LinkedHashMap<String, Boolean> = LinkedHashMap()
)

sealed interface DepositStatsAction {
    data class SelectOption(val option: String, val select: Boolean) : DepositStatsAction
}