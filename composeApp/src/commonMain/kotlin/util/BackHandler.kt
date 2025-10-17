package util

import androidx.compose.runtime.Composable

/**
 * Expect declaration for a composable that handles back button presses.
 */
@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
