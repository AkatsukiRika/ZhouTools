internal const val PREFERENCES_NAME = "zhoutools.preferences_pb"

expect fun isIOS(): Boolean

expect fun getAppVersion(): String

expect fun setClipboardContent(text: String)

expect fun hideSoftwareKeyboard()

expect fun setStatusBarColor(colorStr: String, isLight: Boolean)

expect fun setNavigationBarColor(colorStr: String, isLight: Boolean)