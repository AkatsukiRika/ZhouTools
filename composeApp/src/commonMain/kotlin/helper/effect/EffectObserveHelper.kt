package helper.effect

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object EffectObserveHelper {
    private val timeCardEffectObserver by lazy {
        TimeCardEffectObserver()
    }

    private val writeMemoEffectObserver by lazy {
        WriteMemoEffectObserver()
    }

    private val addScheduleEffectObserver by lazy {
        AddScheduleEffectObserver()
    }

    private val scheduleEffectObserver by lazy {
        ScheduleEffectObserver()
    }

    fun emitTimeCardEffect(effect: TimeCardEffect, scope: CoroutineScope? = null) {
        if (scope == null) {
            timeCardEffectObserver.emitSync(effect)
        } else {
            scope.launch {
                timeCardEffectObserver.emit(effect)
            }
        }
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
    fun observeTimeCardEffect(onEffect: (TimeCardEffect) -> Unit) {
        timeCardEffectObserver.observeComposable(onEffect)
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