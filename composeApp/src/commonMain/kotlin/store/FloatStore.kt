package store

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.floatPreferencesKey

internal class FloatStore(
    override val key: String,
    override val default: Float,
    override val syncSave: Boolean
) : AbstractStore<Float>() {
    override fun getPreferencesKey(): Preferences.Key<Float> {
        return floatPreferencesKey(key)
    }
}