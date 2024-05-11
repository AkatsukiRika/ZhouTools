package arch

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import model.records.Memo
import model.records.Schedule

object EffectObservers {
    private val writeMemoEffectObserver by lazy {
        WriteMemoEffectObserver()
    }

    private val addScheduleEffectObserver by lazy {
        AddScheduleEffectObserver()
    }

    private val scheduleEffectObserver by lazy {
        ScheduleEffectObserver()
    }

    fun emitWriteMemoEffect(effect: WriteMemoEffect, scope: CoroutineScope? = null) {
        if (scope == null) {
            writeMemoEffectObserver.emitSync(effect)
        } else {
            scope.launch {
                writeMemoEffectObserver.emit(effect)
            }
        }
    }

    fun emitAddScheduleEffect(effect: AddScheduleEffect, scope: CoroutineScope? = null) {
        if (scope == null) {
            addScheduleEffectObserver.emitSync(effect)
        } else {
            scope.launch {
                addScheduleEffectObserver.emit(effect)
            }
        }
    }

    fun emitScheduleEffect(effect: ScheduleEffect, scope: CoroutineScope? = null) {
        if (scope == null) {
            scheduleEffectObserver.emitSync(effect)
        } else {
            scope.launch {
                scheduleEffectObserver.emit(effect)
            }
        }
    }

    @Composable
    fun observeWriteMemoEffect(onEffect: (WriteMemoEffect) -> Unit) {
        writeMemoEffectObserver.observeComposable(onEffect)
    }

    @Composable
    fun observeAddScheduleEffect(onEffect: (AddScheduleEffect) -> Unit) {
        addScheduleEffectObserver.observeComposable(onEffect)
    }

    @Composable
    fun observeScheduleEffect(onEffect: (ScheduleEffect) -> Unit) {
        scheduleEffectObserver.observeComposable(onEffect)
    }
}

class WriteMemoEffectObserver : BaseEffectObserver<WriteMemoEffect>()

class AddScheduleEffectObserver : BaseEffectObserver<AddScheduleEffect>()

class ScheduleEffectObserver : BaseEffectObserver<ScheduleEffect>()

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