package ui.widget

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helper.SyncHelper
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.downloading
import zhoutools.composeapp.generated.resources.uploading

@Composable
fun AutoSyncIndicator() {
    var textAlpha by remember { mutableFloatStateOf(1f) }
    val isPulling = SyncHelper.isAutoPulling.collectAsState(initial = false).value
    val isPushing = SyncHelper.isAutoPushing.collectAsState(initial = false).value

    if (isPulling || isPushing) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(20.dp),
                color = Color.LightGray,
                strokeWidth = 2.dp
            )

            Text(
                text = stringResource(
                    if (isPulling) Res.string.downloading else Res.string.uploading
                ),
                color = Color.Gray,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .alpha(textAlpha),
                fontSize = 14.sp
            )
        }

        LaunchedEffect(Unit) {
            // Text blinking effect
            while (true) {
                animate(1f, 0f, animationSpec = tween(500, delayMillis = 500, easing = LinearEasing)) { value, _ ->
                    textAlpha = value
                }
                animate(0f, 1f, animationSpec = tween(500, easing = LinearEasing)) { value, _ ->
                    textAlpha = value
                }
            }
        }
    }
}