package com.tangping.kotstore.model

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tangping.kotstore.flow.FlowStore

abstract class KotStoreFlowModel<STORE>(storeName: String) : KotStoreModel(storeName = storeName) {
    private fun <TYPE> flowStore(
        defaultValue: TYPE,
        key: String,
        preferenceKeyFactory: (String) -> Preferences.Key<TYPE>
    ) = FlowStore<TYPE, STORE>(
        dataStore,
        key,
        defaultValue,
        preferenceKeyFactory
    )

    fun intFlowStore(key: String, default: Int = 0): FlowStore<Int, STORE> =
        flowStore(default, key, ::intPreferencesKey)

    fun longFlowStore(key: String, default: Long = 0L): FlowStore<Long, STORE> =
        flowStore(default, key, ::longPreferencesKey)

    fun floatFlowStore(key: String, default: Float = 0f): FlowStore<Float, STORE> =
        flowStore(default, key, ::floatPreferencesKey)

    fun doubleFlowStore(key: String, default: Double = 0.0): FlowStore<Double, STORE> =
        flowStore(default, key, ::doublePreferencesKey)

    fun booleanFlowStore(key: String, default: Boolean = false): FlowStore<Boolean, STORE> =
        flowStore(default, key, ::booleanPreferencesKey)

    fun stringFlowStore(key: String, default: String = ""): FlowStore<String, STORE> =
        flowStore(default, key, ::stringPreferencesKey)
}