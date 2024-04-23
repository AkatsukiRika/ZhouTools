import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(ExperimentalForeignApi::class)
actual fun createDataStore(): DataStore<Preferences>? {
    return createDataStoreWithDefaults {
        val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        (requireNotNull(documentDirectory).path + "/$PREFERENCES_NAME")
    }
}

actual fun getAppVersion(): String {
    val nsBundle = NSBundle.mainBundle()
    val version = nsBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? NSString
    return version?.toString() ?: ""
}