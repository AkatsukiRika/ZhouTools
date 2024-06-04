package store.base

import createDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import store.FloatStore
import store.LongStore
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

    protected fun longStore(
        key: String,
        default: Long = 0L,
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, Long> = LongStore(key, default, syncSave)

    protected fun floatStore(
        key: String,
        default: Float = 0f,
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, Float> = FloatStore(key, default, syncSave)
}
