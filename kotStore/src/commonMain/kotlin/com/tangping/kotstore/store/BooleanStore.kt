package com.tangping.kotstore.store

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

internal class BooleanStore(
    override val key: String,
    override val default: Boolean,
    override val syncSave: Boolean
) : AbstractStore<Boolean>() {
    override fun getPreferencesKey(): Preferences.Key<Boolean> {
        return booleanPreferencesKey(key)
    }
}