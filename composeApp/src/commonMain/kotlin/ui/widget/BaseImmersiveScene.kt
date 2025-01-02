package ui.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
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
fun BaseImmersiveScene(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    LaunchedEffect(Unit) {
        // Support for Android
        setStatusBarColor("#FFFFFF", isLight = true)
        setNavigationBarColor("#F4F4F4", isLight = true)
    }

    var rootModifier = modifier
    if (isIOS()) {
        // Support for iOS
        rootModifier = rootModifier.navigationBarsPadding()
    }

    Box(modifier = rootModifier) {
        content()
    }
}