import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tangping.zhoujiang.MainActivity
import java.io.File

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun createDataStore(): DataStore<Preferences>? {
    val context = MainActivity.context ?: return null
    return createDataStoreWithDefaults {
        File(context.applicationContext.filesDir, PREFERENCES_NAME).path
    }
}