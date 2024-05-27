package helper.effect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle

open class BaseEffectObserver<T> {
    private val effectFlow = MutableSharedFlow<T?>(replay = 1)

    fun emitSync(effect: T?) {
        runBlocking {
            effectFlow.emit(effect)
        }
    }

    suspend fun emit(effect: T?) {
        effectFlow.emit(effect)
    }

    @Composable
    fun observeComposable(onEffect: (T) -> Unit) {
        val effect = effectFlow.collectAsStateWithLifecycle(null).value

        LaunchedEffect(effect) {
            if (effect != null) {
                onEffect(effect)
                emit(null)
            }
        }
    }
}