package helper.effect

import model.records.Memo
import model.records.Schedule

class TimeCardEffectObserver : BaseEffectObserver<TimeCardEffect>()

class WriteMemoEffectObserver : BaseEffectObserver<WriteMemoEffect>()

class AddScheduleEffectObserver : BaseEffectObserver<AddScheduleEffect>()

class ScheduleEffectObserver : BaseEffectObserver<ScheduleEffect>()

class DepositEffectObserver : BaseEffectObserver<DepositEffect>()

sealed interface TimeCardEffect {
    data object RefreshTodayState : TimeCardEffect
}

sealed interface WriteMemoEffect {
    data class BeginEdit(val memo: Memo) : WriteMemoEffect
}

sealed interface AddScheduleEffect {
    data class SetDate(val year: Int, val month: Int, val day: Int) : AddScheduleEffect
    data class BeginEdit(val schedule: Schedule) : AddScheduleEffect
}

sealed interface ScheduleEffect {
    data object RefreshData : ScheduleEffect
}

sealed interface DepositEffect {
    data object RefreshData : DepositEffect
}