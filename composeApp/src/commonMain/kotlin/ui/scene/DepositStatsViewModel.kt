package ui.scene

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import extension.getYear
import extension.toMonthYearString
import global.AppColors
import helper.DepositHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.records.DepositMonth
import ui.widget.BarData
import ui.widget.LineData

const val VALUE_MODE_FULL = 0
const val VALUE_MODE_DIFF1 = 1      // Diff with the first data in group
const val VALUE_MODE_DIFF2 = 2      // Diff with the previous data
const val VALUE_MODE_NONE = 3
const val VALUE_MODE_COUNT = 4

data class DepositStatsState(
    val totalDepositBarData: List<BarData<Float>> = emptyList(),
    val incomeLineData: List<LineData<Float>> = emptyList(),
    val filterOptions: LinkedHashMap<String, Boolean> = LinkedHashMap(),
    val valueMode: Int = VALUE_MODE_FULL,
    val totalIncome: Int = 0,
    val showExtraDeposit: Boolean = true,
    val depositMonths: List<DepositMonth> = emptyList()
)

sealed interface DepositStatsAction {
    data class SelectOption(val option: String, val select: Boolean) : DepositStatsAction
    data object ToggleValueMode : DepositStatsAction
    data object ToggleShowExtraDeposit : DepositStatsAction
    data object Reset : DepositStatsAction
}

class DepositStatsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DepositStatsState())
    val uiState = _uiState.asStateFlow()

    init {
        initData()
    }

    fun dispatch(action: DepositStatsAction) {
        when (action) {
            is DepositStatsAction.SelectOption -> onSelectOption(action.option, action.select)
            is DepositStatsAction.ToggleValueMode -> toggleValueMode()
            is DepositStatsAction.ToggleShowExtraDeposit -> toggleShowExtraDeposit()
            is DepositStatsAction.Reset -> resetState()
        }
    }

    private fun resetState() {
        _uiState.value = DepositStatsState()
        initData()
    }

    private fun initData() {
        viewModelScope.launch {
            val depositMonths = DepositHelper.getMonths().sortedBy { it.monthStartTime }
            _uiState.update {
                it.copy(depositMonths = depositMonths)
            }
            initTotalDepositBar()
            initIncomeLineChart()
            initFilterOptions()
            refreshSelectedTotal()
        }
    }

    private fun refreshData(newDepositMonths: List<DepositMonth>) {
        _uiState.update {
            it.copy(depositMonths = newDepositMonths.sortedBy { it.monthStartTime })
        }
        initTotalDepositBar()
        initIncomeLineChart()
        refreshSelectedTotal()
    }

    private fun initTotalDepositBar() {
        val currentState = _uiState.value
        val barData = mutableListOf<BarData<Float>>()
        var firstValue = 0f
        currentState.depositMonths.forEachIndexed { index, it ->
            val monthStr = it.monthStartTime.toMonthYearString()
            val partValues = LinkedHashMap<Color, Float>()
            partValues[AppColors.Theme] = it.currentAmount / 100f
            if (currentState.showExtraDeposit) {
                partValues[AppColors.LightGold] = it.extraDeposit / 100f
            }
            barData.add(BarData(
                values = partValues,
                label = monthStr,
                valueToString = { value ->
                    when (currentState.valueMode) {
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
                        else -> value.toInt().toString()
                    }
                }
            ))
        }
        _uiState.update { it.copy(totalDepositBarData = barData) }
    }

    private fun initIncomeLineChart() {
        val lineData = mutableListOf<LineData<Float>>()
        _uiState.value.depositMonths.forEach {
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
        _uiState.update { it.copy(incomeLineData = lineData) }
    }

    private fun initFilterOptions() {
        val yearSet = mutableSetOf<Int>()
        _uiState.value.depositMonths.forEach {
            yearSet.add(it.monthStartTime.getYear())
        }
        val sortedYears = yearSet.sortedByDescending { it }
        val options = LinkedHashMap<String, Boolean>()
        sortedYears.forEach {
            options[it.toString()] = true
        }
        _uiState.update { it.copy(filterOptions = options) }
    }

    private fun refreshSelectedTotal() {
        val totalIncome = (_uiState.value.depositMonths.sumOf { it.monthlyIncome } / 100f).toInt()
        _uiState.update { it.copy(totalIncome = totalIncome) }
    }

    private fun onSelectOption(option: String, select: Boolean) {
        val newFilterOptions = LinkedHashMap(_uiState.value.filterOptions)
        newFilterOptions[option] = select
        _uiState.update { it.copy(filterOptions = newFilterOptions) }

        val selectedYears = newFilterOptions.filter { it.value }.map { it.key.toInt() }
        refreshData(DepositHelper.getMonths().filter { selectedYears.contains(it.monthStartTime.getYear()) })
    }

    private fun toggleValueMode() {
        val newValueMode = (_uiState.value.valueMode + 1) % VALUE_MODE_COUNT
        _uiState.update { it.copy(valueMode = newValueMode) }
        initTotalDepositBar()
    }

    private fun toggleShowExtraDeposit() {
        val newShowExtraDeposit = !_uiState.value.showExtraDeposit
        _uiState.update { it.copy(showExtraDeposit = newShowExtraDeposit) }
        initTotalDepositBar()
    }
}
