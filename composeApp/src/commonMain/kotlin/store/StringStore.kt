package store

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey

internal class StringStore(
    override val key: String,
    override val default: String,
    override val syncSave: Boolean
) : AbstractStore<String>() {
    override fun getPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey(key)
    }
}
