package ui.scene

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import extension.dayStartTime
import extension.getDayOfMonth
import extension.getHour
import extension.getMinute
import extension.getMonthOfYear
import extension.getYear
import helper.ScheduleHelper
import helper.SyncHelper
import helper.effect.EffectHelper
import helper.effect.ScheduleEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.records.Schedule
import util.CalendarUtil
import util.TimeUtil

enum class TimeEditType {
    START_TIME, END_TIME
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
    val isEdit: Boolean,
    val milestoneGoalMillis: Long,
    val editItem: Schedule? = null // Added this
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
    data class SetTimeEditType(val editType: TimeEditType?) : AddScheduleAction
    data class Confirm(val text: String, val milestoneGoalMillis: Long) : AddScheduleAction
    data class BeginEdit(val schedule: Schedule) : AddScheduleAction
    data object Reset : AddScheduleAction
}

sealed interface AddScheduleEvent {
    data object GoBack : AddScheduleEvent
}

class AddScheduleViewModel : ViewModel() {

    private val _uiState: MutableStateFlow<AddScheduleState>
    val uiState: StateFlow<AddScheduleState>

    private val _addScheduleEvent = MutableSharedFlow<AddScheduleEvent>()
    val addScheduleEvent: SharedFlow<AddScheduleEvent> = _addScheduleEvent.asSharedFlow()

    init {
        val now = TimeUtil.currentTimeMillis()
        val initialState = AddScheduleState(
            year = now.getYear(),
            monthOfYear = now.getMonthOfYear(),
            dayOfMonth = now.getDayOfMonth(),
            text = "",
            startTime = now,
            endTime = now,
            isAllDay = false,
            isMilestone = false,
            timeEditType = null,
            isEdit = false,
            milestoneGoalMillis = 0L,
            editItem = null
        )
        _uiState = MutableStateFlow(initialState)
        uiState = _uiState.asStateFlow()
    }

    fun dispatch(action: AddScheduleAction) {
        when (action) {
            is AddScheduleAction.Reset -> {
                resetState()
            }
            is AddScheduleAction.SetDate -> {
                setDate(action.dateTriple)
            }
            is AddScheduleAction.SetAllDay -> {
                _uiState.update { it.copy(isAllDay = action.newValue) }
            }
            is AddScheduleAction.SetMilestone -> {
                _uiState.update { it.copy(isMilestone = action.newValue) }
            }
            is AddScheduleAction.SetStartTime -> {
                val s = _uiState.value
                val millis = TimeUtil.toEpochMillis(s.year, s.monthOfYear, s.dayOfMonth, action.hour, action.minute)
                _uiState.update { it.copy(startTime = millis) }
            }
            is AddScheduleAction.SetEndTime -> {
                val s = _uiState.value
                val millis = TimeUtil.toEpochMillis(s.year, s.monthOfYear, s.dayOfMonth, action.hour, action.minute)
                _uiState.update { it.copy(endTime = millis) }
            }
            is AddScheduleAction.SetTimeEditType -> {
                _uiState.update { it.copy(timeEditType = action.editType) }
            }
            is AddScheduleAction.Confirm -> {
                _uiState.update { it.copy(text = action.text, milestoneGoalMillis = action.milestoneGoalMillis) }
                if (_uiState.value.isEdit) {
                    editSchedule()
                } else {
                    addSchedule()
                }
                SyncHelper.autoPushSchedule()
                viewModelScope.launch {
                    _addScheduleEvent.emit(AddScheduleEvent.GoBack)
                }
            }
            is AddScheduleAction.BeginEdit -> {
                initEditData(action.schedule)
            }
        }
    }

    private fun resetState() {
        val now = TimeUtil.currentTimeMillis()
        _uiState.value = AddScheduleState(
            year = now.getYear(),
            monthOfYear = now.getMonthOfYear(),
            dayOfMonth = now.getDayOfMonth(),
            text = "",
            startTime = now,
            endTime = now,
            isAllDay = false,
            isMilestone = false,
            timeEditType = null,
            isEdit = false,
            milestoneGoalMillis = 0L,
            editItem = null
        )
    }

    private fun setDate(dateTriple: Triple<Int, Int, Int>) {
        val year = dateTriple.first
        val monthOfYear = dateTriple.second
        val dayOfMonth = dateTriple.third
        val currentStartTime = _uiState.value.startTime
        val currentEndTime = _uiState.value.endTime

        val newStartTime = TimeUtil.toEpochMillis(year, monthOfYear, dayOfMonth, currentStartTime.getHour(), currentStartTime.getMinute())
        val newEndTime = TimeUtil.toEpochMillis(year, monthOfYear, dayOfMonth, currentEndTime.getHour(), currentEndTime.getMinute())

        _uiState.update {
            it.copy(
                year = year,
                monthOfYear = monthOfYear,
                dayOfMonth = dayOfMonth,
                startTime = newStartTime,
                endTime = newEndTime
            )
        }
    }

    private fun initEditData(schedule: Schedule) {
        _uiState.update {
            it.copy(
                editItem = schedule,
                isEdit = true,
                year = schedule.dayStartTime.getYear(),
                monthOfYear = schedule.dayStartTime.getMonthOfYear(),
                dayOfMonth = schedule.dayStartTime.getDayOfMonth(),
                text = schedule.text,
                startTime = schedule.startingTime,
                endTime = schedule.endingTime,
                isAllDay = schedule.isAllDay,
                isMilestone = schedule.isMilestone,
                milestoneGoalMillis = schedule.milestoneGoal
            )
        }
    }

    private fun addSchedule() {
        val s = _uiState.value
        val schedule = Schedule(
            text = s.text,
            dayStartTime = s.startTime.dayStartTime(),
            startingTime = s.startTime,
            endingTime = s.endTime,
            isAllDay = s.isAllDay,
            isMilestone = s.isMilestone,
            milestoneGoal = s.milestoneGoalMillis
        )
        ScheduleHelper.addSchedule(schedule)
    }

    private fun editSchedule() {
        val s = _uiState.value
        s.editItem?.let {
            ScheduleHelper.modifySchedule(it, s.text, s.startTime, s.endTime, s.isAllDay, s.isMilestone, s.milestoneGoalMillis)
        }
        EffectHelper.emitScheduleEffect(ScheduleEffect.RefreshData)
    }
}
