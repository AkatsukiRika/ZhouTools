package com.tangping.kotstore.flow

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.tangping.kotstore.model.KotStoreFlowModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class FlowStore<TYPE, STORE> internal constructor(
    private val dataStore: DataStore<Preferences>,
    private val key: String,
    private val defaultValue: TYPE,
    preferenceKeyFactory: (String) -> Preferences.Key<TYPE>
) : ReadOnlyProperty<KotStoreFlowModel<STORE>, Flow<TYPE>> {
    private val preferenceKey: Preferences.Key<TYPE> by lazy {
        preferenceKeyFactory(key)
    }

    override fun getValue(thisRef: KotStoreFlowModel<STORE>, property: KProperty<*>): FlowDelegate<TYPE> {
        return FlowDelegate(
            dataStore.data.map { it[preferenceKey] ?: defaultValue }
        ) {
            dataStore.edit { settings ->
                settings[preferenceKey] = it
            }
        }
    }
}