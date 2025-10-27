package ui.widget

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import constant.TabConstants
import global.AppColors
import helper.SyncHelper
import org.jetbrains.compose.resources.stringResource
import store.AppFlowStore
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.downloading
import zhoutools.composeapp.generated.resources.retry_upload
import zhoutools.composeapp.generated.resources.uploading

@Composable
fun AutoSyncIndicator(homeTabId: Int) {
    var textAlpha by remember { mutableFloatStateOf(1f) }
    val isPulling = SyncHelper.isAutoPulling.collectAsState(initial = false).value
    val isPushing = SyncHelper.isAutoPushing.collectAsState(initial = false).value
    val pushStatus = when (homeTabId) {
        TabConstants.TAB_TIME_CARD -> AppFlowStore.lastPushTimeCardStatus.collectAsState(AppFlowStore.STATUS_NONE).value
        TabConstants.TAB_SCHEDULE -> AppFlowStore.lastPushScheduleStatus.collectAsState(AppFlowStore.STATUS_NONE).value
        TabConstants.TAB_MEMO -> AppFlowStore.lastPushMemoStatus.collectAsState(AppFlowStore.STATUS_NONE).value
        TabConstants.TAB_DEPOSIT -> AppFlowStore.lastPushDepositStatus.collectAsState(AppFlowStore.STATUS_NONE).value
        else -> AppFlowStore.STATUS_NONE
    }

    if (pushStatus == AppFlowStore.STATUS_FAIL) {
        val annotatedString = buildAnnotatedString {
            append("❌ ")
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                append(stringResource(Res.string.retry_upload))
            }
        }
        Text(
            text = annotatedString,
            color = AppColors.Red,
            modifier = Modifier
                .padding(start = 4.dp)
                .clickable {}
                .padding(horizontal = 4.dp),
            fontSize = 14.sp
        )
    } else if (isPulling || isPushing) {
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