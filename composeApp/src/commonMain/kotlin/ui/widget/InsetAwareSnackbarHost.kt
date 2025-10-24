package ui.widget

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A wrapper for [androidx.compose.material.SnackbarHost] that applies navigation bar padding.
 * This is useful for scenes that don't have a bottom bar, to prevent the snackbar from overlapping with the system navigation bar.
 */
@Composable
fun InsetAwareSnackbarHost(
    hostState: androidx.compose.material.SnackbarHostState,
    modifier: Modifier = Modifier
) {
    androidx.compose.material.SnackbarHost(
        hostState = hostState,
        modifier = modifier.navigationBarsPadding()
    )
}

/**
 * A wrapper for [androidx.compose.material3.SnackbarHost] that applies navigation bar padding.
 * This is useful for scenes that don't have a bottom bar, to prevent the snackbar from overlapping with the system navigation bar.
 */
@Composable
fun InsetAwareSnackbarHost(
    hostState: androidx.compose.material3.SnackbarHostState,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.SnackbarHost(
        hostState = hostState,
        modifier = modifier.navigationBarsPadding()
    )
}