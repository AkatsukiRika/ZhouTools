package ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

const val VALUE_MODE_FULL = 0
const val VALUE_MODE_DIFF1 = 1      // Diff with the first data in group
const val VALUE_MODE_DIFF2 = 2      // Diff with the previous data
const val VALUE_MODE_NONE = 3
const val VALUE_MODE_COUNT = 4

@Composable
fun DepositStatsPresenter(actionFlow: Flow<DepositStatsAction>): DepositStatsState {
    var depositMonths by remember { mutableStateOf(listOf<DepositMonth>()) }
    var totalDepositBarData by remember { mutableStateOf(listOf<BarData<Float>>()) }
    var incomeLineData by remember { mutableStateOf(listOf<LineData<Float>>()) }
    var filterOptions by remember { mutableStateOf(LinkedHashMap<String, Boolean>()) }
    var valueMode by remember { mutableIntStateOf(VALUE_MODE_FULL) }
    var totalIncome by remember { mutableIntStateOf(0) }

    fun initTotalDepositBar() {
        val barData = mutableListOf<BarData<Float>>()
        var firstValue = 0f
        depositMonths.forEachIndexed { index, it ->
            val monthStr = it.monthStartTime.toMonthYearString()
            val partValues = LinkedHashMap<Color, Float>()
            partValues[AppColors.Theme] = it.currentAmount / 100f
            partValues[AppColors.LightGold] = it.extraDeposit / 100f
            barData.add(BarData(
                values = partValues,
                label = monthStr,
                valueToString = { value ->
                    when (valueMode) {
                        VALUE_MODE_NONE -> ""
                        VALUE_MODE_DIFF1 -> {
                            if (index == 0) {
                                firstValue = value
                                "0"
                            } else {
                                val intValue = (value - firstValue).toInt()
                                intValue.toString()
                            }
                        }
                        VALUE_MODE_DIFF2 -> {
                            if (index == 0) {
                                "0"
                            } else {
                                val intValue = (value - barData[index - 1].getTotalValue()).toInt()
                                if (intValue > 0) {
                                    "+$intValue"
                                } else {
                                    intValue.toString()
                                }
                            }
                        }
                        else -> {
                            value.toInt().toString()
                        }
                    }
                }
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

    fun refreshSelectedTotal() {
        totalIncome = (depositMonths.sumOf { it.monthlyIncome } / 100f).toInt()
    }

    fun initData() {
        depositMonths = DepositHelper.getMonths().sortedBy { it.monthStartTime }
        initTotalDepositBar()
        initIncomeLineChart()
        initFilterOptions()
        refreshSelectedTotal()
    }

    fun refreshData(newDepositMonths: List<DepositMonth>) {
        depositMonths = newDepositMonths.sortedBy { it.monthStartTime }
        initTotalDepositBar()
        initIncomeLineChart()
        refreshSelectedTotal()
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

    fun toggleValueMode() {
        valueMode = (valueMode + 1) % VALUE_MODE_COUNT
        initTotalDepositBar()
    }

    LaunchedEffect(Unit) {
        initData()
    }

    actionFlow.collectAction {
        when (this) {
            is DepositStatsAction.SelectOption -> {
                onSelectOption(option, select)
            }

            is DepositStatsAction.ToggleValueMode -> {
                toggleValueMode()
            }
        }
    }

    return DepositStatsState(totalDepositBarData, incomeLineData, filterOptions, valueMode, totalIncome)
}

data class DepositStatsState(
    val totalDepositBarData: List<BarData<Float>> = emptyList(),
    val incomeLineData: List<LineData<Float>> = emptyList(),
    val filterOptions: LinkedHashMap<String, Boolean> = LinkedHashMap(),
    val valueMode: Int = VALUE_MODE_FULL,
    val totalIncome: Int = 0
)

sealed interface DepositStatsAction {
    data class SelectOption(val option: String, val select: Boolean) : DepositStatsAction
    data object ToggleValueMode : DepositStatsAction
}