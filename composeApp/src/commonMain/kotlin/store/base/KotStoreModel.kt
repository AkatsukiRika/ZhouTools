package store.base

import createDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import store.StringStore
import kotlin.properties.ReadWriteProperty

abstract class KotStoreModel(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
) {
    internal val dataStore by lazy {
        createDataStore()
    }

    open val syncSaveAllProperties: Boolean = false

    protected fun stringStore(
        key: String,
        default: String = "",
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, String> = StringStore(key, default, syncSave)
}
