package ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import helper.effect.ScheduleEffect
import extension.dayStartTime
import extension.getDayOfMonth
import extension.getHour
import extension.getMinute
import extension.getMonthOfYear
import extension.getYear
import helper.ScheduleHelper
import helper.effect.EffectObserveHelper
import kotlinx.coroutines.flow.Flow
import model.records.Schedule
import moe.tlaster.precompose.molecule.collectAction
import util.CalendarUtil
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
    var isEdit by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<Schedule?>(null) }

    fun setDate(dateTriple: Triple<Int, Int, Int>) {
        year = dateTriple.first
        monthOfYear = dateTriple.second
        dayOfMonth = dateTriple.third
        startTime = TimeUtil.toEpochMillis(year, monthOfYear, dayOfMonth, startTime.getHour(), startTime.getMinute())
        endTime = TimeUtil.toEpochMillis(year, monthOfYear, dayOfMonth, endTime.getHour(), endTime.getMinute())
    }

    fun initEditData(schedule: Schedule) {
        editItem = schedule
        year = schedule.dayStartTime.getYear()
        monthOfYear = schedule.dayStartTime.getMonthOfYear()
        dayOfMonth = schedule.dayStartTime.getDayOfMonth()
        text = schedule.text
        startTime = schedule.startingTime
        endTime = schedule.endingTime
        isAllDay = schedule.isAllDay
        isMilestone = schedule.isMilestone
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
        ScheduleHelper.addSchedule(schedule)
    }

    fun editSchedule() {
        editItem?.let {
            ScheduleHelper.modifySchedule(it, text, startTime, endTime, isAllDay, isMilestone)
        }
        EffectObserveHelper.emitScheduleEffect(ScheduleEffect.RefreshData)
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
                if (isEdit) {
                    editSchedule()
                } else {
                    addSchedule()
                }
            }

            is AddScheduleAction.BeginEdit -> {
                isEdit = true
                initEditData(schedule)
            }
        }
    }

    return AddScheduleState(year, monthOfYear, dayOfMonth, text, startTime, endTime, isAllDay, isMilestone, timeEditType, isEdit)
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
    val timeEditType: TimeEditType?,
    val isEdit: Boolean
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
    data class BeginEdit(val schedule: Schedule) : AddScheduleAction
}