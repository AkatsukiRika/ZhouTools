import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath

internal const val PREFERENCES_NAME = "zhoutools.preferences_pb"

expect fun isIOS(): Boolean

internal fun createDataStoreWithDefaults(
    corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    migrations: List<DataMigration<Preferences>> = emptyList(),
    path: () -> String
) = PreferenceDataStoreFactory.createWithPath(
    corruptionHandler = corruptionHandler,
    scope = coroutineScope,
    migrations = migrations,
    produceFile = {
        path().toPath()
    }
)

expect fun createDataStore(): DataStore<Preferences>?

expect fun getAppVersion(): String

expect fun setClipboardContent(text: String)

expect fun hideSoftwareKeyboard()

expect fun setStatusBarColor(colorStr: String, isLight: Boolean)

expect fun setNavigationBarColor(colorStr: String, isLight: Boolean)