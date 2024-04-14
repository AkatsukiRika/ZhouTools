package fragment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TimeCardFragment(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Text(text = "TimeCardFragment")
    }
}