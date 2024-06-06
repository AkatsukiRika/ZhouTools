package com.tangping.kotstore.model

import com.tangping.kotstore.createDataStore
import com.tangping.kotstore.store.BooleanStore
import com.tangping.kotstore.store.DoubleStore
import com.tangping.kotstore.store.FloatStore
import com.tangping.kotstore.store.IntStore
import com.tangping.kotstore.store.LongStore
import com.tangping.kotstore.store.StringStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlin.properties.ReadWriteProperty

abstract class KotStoreModel(
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    val storeName: String
) {
    internal val dataStore by lazy {
        createDataStore(storeName)
    }

    open val syncSaveAllProperties: Boolean = false

    protected fun stringStore(
        key: String,
        default: String = "",
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, String> = StringStore(key, default, syncSave)

    protected fun intStore(
        key: String,
        default: Int = 0,
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, Int> = IntStore(key, default, syncSave)

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

    protected fun booleanStore(
        key: String,
        default: Boolean = false,
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, Boolean> = BooleanStore(key, default, syncSave)

    protected fun doubleStore(
        key: String,
        default: Double = 0.0,
        syncSave: Boolean = syncSaveAllProperties
    ): ReadWriteProperty<KotStoreModel, Double> = DoubleStore(key, default, syncSave)
}