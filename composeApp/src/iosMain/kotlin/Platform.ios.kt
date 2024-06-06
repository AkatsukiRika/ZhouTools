import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.endEditing

actual fun isIOS(): Boolean = true

actual fun getAppVersion(): String {
    val nsBundle = NSBundle.mainBundle()
    val version = nsBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? NSString
    return version?.toString() ?: ""
}

actual fun setClipboardContent(text: String) {
    val pasteboard = UIPasteboard.generalPasteboard()
    pasteboard.string = text
}

actual fun hideSoftwareKeyboard() {
    val sharedApp = UIApplication.sharedApplication()
    val window = sharedApp.keyWindow
    window?.endEditing(true)
}

actual fun setStatusBarColor(colorStr: String, isLight: Boolean) {}

actual fun setNavigationBarColor(colorStr: String, isLight: Boolean) {}