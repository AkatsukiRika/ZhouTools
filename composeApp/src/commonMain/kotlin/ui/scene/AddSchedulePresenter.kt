package ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow
import logger
import moe.tlaster.precompose.molecule.collectAction
import util.TimeUtil

@Composable
fun AddSchedulePresenter(actionFlow: Flow<AddScheduleAction>): AddScheduleState {
    var year by remember { mutableIntStateOf(0) }
    var monthOfYear by remember { mutableIntStateOf(0) }
    var dayOfMonth by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    var startTime by remember { mutableLongStateOf(TimeUtil.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(TimeUtil.currentTimeMillis()) }
    var isAllDay by remember { mutableStateOf(false) }

    fun setDate(dateTriple: Triple<Int, Int, Int>) {
        year = dateTriple.first
        monthOfYear = dateTriple.second
        dayOfMonth = dateTriple.third
        logger.i { "setDate year=$year, monthOfYear=$monthOfYear, dayOfMonth=$dayOfMonth" }
    }

    actionFlow.collectAction {
        when (this) {
            is AddScheduleAction.SetDate -> {
                setDate(dateTriple)
            }

            is AddScheduleAction.SetAllDay -> {
                isAllDay = newValue
            }
        }
    }

    return AddScheduleState(year, monthOfYear, dayOfMonth, text, startTime, endTime, isAllDay)
}

data class AddScheduleState(
    val year: Int,
    val monthOfYear: Int,
    val dayOfMonth: Int,
    val text: String,
    val startTime: Long,
    val endTime: Long,
    val isAllDay: Boolean
)

sealed interface AddScheduleAction {
    data class SetDate(val dateTriple: Triple<Int, Int, Int>) : AddScheduleAction
    data class SetAllDay(val newValue: Boolean) : AddScheduleAction
}