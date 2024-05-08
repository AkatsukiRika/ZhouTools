package ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import extension.dayStartTime
import extension.getHour
import extension.getMinute
import kotlinx.coroutines.flow.Flow
import model.records.Schedule
import moe.tlaster.precompose.molecule.collectAction
import util.CalendarUtil
import util.ScheduleUtil
import util.TimeUtil

enum class TimeEditType {
    START_TIME, END_TIME
}

@Composable
fun AddSchedulePresenter(actionFlow: Flow<AddScheduleAction>): AddScheduleState {
    var year by remember { mutableIntStateOf(0) }
    var monthOfYear by remember { mutableIntStateOf(0) }
    var dayOfMonth by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    var startTime by remember { mutableLongStateOf(TimeUtil.currentTimeMillis()) }
    var endTime by remember { mutableLongStateOf(TimeUtil.currentTimeMillis()) }
    var isAllDay by remember { mutableStateOf(false) }
    var isMilestone by remember { mutableStateOf(false) }
    var timeEditType by remember { mutableStateOf<TimeEditType?>(null) }
    val util = remember { ScheduleUtil() }

    fun setDate(dateTriple: Triple<Int, Int, Int>) {
        year = dateTriple.first
        monthOfYear = dateTriple.second
        dayOfMonth = dateTriple.third
        startTime = TimeUtil.toEpochMillis(year, monthOfYear, dayOfMonth, startTime.getHour(), startTime.getMinute())
        endTime = TimeUtil.toEpochMillis(year, monthOfYear, dayOfMonth, endTime.getHour(), endTime.getMinute())
    }

    fun addSchedule() {
        val schedule = Schedule(
            text = text,
            dayStartTime = startTime.dayStartTime(),
            startingTime = startTime,
            endingTime = endTime,
            isAllDay = isAllDay,
            isMilestone = isMilestone
        )
        util.addSchedule(schedule)
    }

    actionFlow.collectAction {
        when (this) {
            is AddScheduleAction.SetDate -> {
                setDate(dateTriple)
            }

            is AddScheduleAction.SetAllDay -> {
                isAllDay = newValue
            }

            is AddScheduleAction.SetMilestone -> {
                isMilestone = newValue
            }

            is AddScheduleAction.SetStartTime -> {
                val millis = TimeUtil.toEpochMillis(year, monthOfYear, dayOfMonth, hour, minute)
                startTime = millis
            }

            is AddScheduleAction.SetEndTime -> {
                val millis = TimeUtil.toEpochMillis(year, monthOfYear, dayOfMonth, hour, minute)
                endTime = millis
            }

            is AddScheduleAction.SetTimeEditType -> {
                timeEditType = editType
            }

            is AddScheduleAction.Confirm -> {
                text = this.text
                addSchedule()
            }
        }
    }

    return AddScheduleState(year, monthOfYear, dayOfMonth, text, startTime, endTime, isAllDay, isMilestone, timeEditType)
}

data class AddScheduleState(
    val year: Int,
    val monthOfYear: Int,
    val dayOfMonth: Int,
    val text: String,
    val startTime: Long,
    val endTime: Long,
    val isAllDay: Boolean,
    val isMilestone: Boolean,
    val timeEditType: TimeEditType?
) {
    suspend fun getDateString(): String {
        if (monthOfYear - 1 in CalendarUtil.getMonthNamesNonComposable().indices) {
            val monthName = CalendarUtil.getMonthNamesNonComposable()[monthOfYear - 1]
            return "$monthName $dayOfMonth, $year"
        }
        return ""
    }
}

sealed interface AddScheduleAction {
    data class SetDate(val dateTriple: Triple<Int, Int, Int>) : AddScheduleAction
    data class SetAllDay(val newValue: Boolean) : AddScheduleAction
    data class SetMilestone(val newValue: Boolean) : AddScheduleAction
    data class SetStartTime(val hour: Int, val minute: Int) : AddScheduleAction
    data class SetEndTime(val hour: Int, val minute: Int) : AddScheduleAction
    data class SetTimeEditType(val editType: TimeEditType) : AddScheduleAction
    data class Confirm(val text: String) : AddScheduleAction
}