import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import com.tangping.zhoujiang.MainActivity

actual fun isIOS(): Boolean = false

actual fun getAppVersion(): String {
    val context = MainActivity.context ?: return ""
    return context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
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

actual fun setStatusBarColor(colorStr: String, isLight: Boolean) {
    val window = MainActivity.window ?: return
    window.statusBarColor = Color.parseColor(colorStr)
    if (isLight) {
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    } else {
        window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
    }
}

actual fun setNavigationBarColor(colorStr: String, isLight: Boolean) {
    val window = MainActivity.window ?: return
    window.navigationBarColor = Color.parseColor(colorStr)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (isLight) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        window.navigationBarDividerColor = Color.parseColor(colorStr)
    }
}

@Composable
actual fun rememberKeyboardVisibilityState(): State<Boolean> {
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val isImeVisible = remember {
        derivedStateOf {
            imeInsets.getBottom(density) > 0
        }
    }
    return isImeVisible
}