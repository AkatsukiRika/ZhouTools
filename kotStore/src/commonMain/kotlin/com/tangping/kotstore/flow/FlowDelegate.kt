package com.tangping.kotstore.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class FlowDelegate<TYPE> internal constructor(
    data: Flow<TYPE>,
    private val onSave: suspend (TYPE) -> Unit
) : Flow<TYPE> by data {
    suspend fun emit(value: TYPE) {
        onSave(value)
    }

    fun emitIn(scope: CoroutineScope, value: TYPE) {
        scope.launch {
            emit(value)
        }
    }
}