import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

internal const val PREFERENCES_NAME = "zhoutools.preferences_pb"
internal const val FLOW_PREFERENCES_NAME = "zhoutools_flow.preferences_pb"

expect fun isIOS(): Boolean

expect fun getAppVersion(): String

expect fun setClipboardContent(text: String)

expect fun hideSoftwareKeyboard()

expect fun setStatusBarColor(colorStr: String, isLight: Boolean)

expect fun setNavigationBarColor(colorStr: String, isLight: Boolean)

@Composable
expect fun rememberKeyboardVisibilityState(): State<Boolean>