package util

import androidx.compose.runtime.Composable

/**
 * iOS-specific implementation of BackHandler (no-op).
 */
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No-op on iOS, as there is no universal physical back button.
}
