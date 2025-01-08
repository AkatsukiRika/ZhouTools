package ui.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
    var rootModifier = modifier
    if (statusBarPadding) {
        rootModifier = rootModifier.statusBarsPadding()
    }
    if (navigationBarPadding) {
        rootModifier = rootModifier.navigationBarsPadding()
    }

    Box(modifier = rootModifier) {
        content()
    }
}