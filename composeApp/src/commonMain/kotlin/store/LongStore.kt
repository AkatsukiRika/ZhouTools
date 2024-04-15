package store

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey

internal class LongStore(
    override val key: String,
    override val default: Long,
    override val syncSave: Boolean
) : AbstractStore<Long>() {
    override fun getPreferencesKey(): Preferences.Key<Long> {
        return longPreferencesKey(key)
    }
}