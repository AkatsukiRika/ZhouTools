package ui.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import isIOS
import setNavigationBarColor
import setStatusBarColor

/**
 * Scene for independent pages
 * Immersive status & navigation bar support for Android and iOS.
 */
@Composable
fun BaseImmersiveScene(
    modifier: Modifier = Modifier,
    statusBarColorStr: String = "#FFFFFF",
    navigationBarColorStr: String = "#F4F4F4",
    statusBarPadding: Boolean = false,
    navigationBarPadding: Boolean = true,
    content: @Composable () -> Unit
) {
    LaunchedEffect(Unit) {
        // Support for Android
        setStatusBarColor(statusBarColorStr, isLight = true)
        if (navigationBarColorStr.isNotEmpty()) {
            setNavigationBarColor(navigationBarColorStr, isLight = true)
        }
    }

    var rootModifier = modifier
    if (isIOS()) {
        // Support for iOS
        if (statusBarPadding) {
            rootModifier = rootModifier.statusBarsPadding()
        }
        if (navigationBarPadding) {
            rootModifier = rootModifier.navigationBarsPadding()
        }
    }

    Box(modifier = rootModifier) {
        content()
    }
}