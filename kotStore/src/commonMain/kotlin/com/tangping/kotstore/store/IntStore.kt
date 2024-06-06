package com.tangping.kotstore.store

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey

internal class IntStore(
    override val key: String,
    override val default: Int,
    override val syncSave: Boolean
) : AbstractStore<Int>() {
    override fun getPreferencesKey(): Preferences.Key<Int> {
        return intPreferencesKey(key)
    }
}