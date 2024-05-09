import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.view.inputmethod.InputMethodManager
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

actual fun getAppVersion(): String {
    val context = MainActivity.context ?: return ""
    return context.packageManager.getPackageInfo(context.packageName, 0).versionName
}

actual fun setClipboardContent(text: String) {
    val context = MainActivity.context ?: return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
}

actual fun hideSoftwareKeyboard() {
    val context = MainActivity.context ?: return
    val imeManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocus = (context as? Activity)?.currentFocus
    val windowToken = currentFocus?.windowToken
    windowToken?.let {
        imeManager.hideSoftInputFromWindow(windowToken, 0)
    }
}