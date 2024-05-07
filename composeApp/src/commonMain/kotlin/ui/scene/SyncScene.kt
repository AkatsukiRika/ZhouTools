package ui.scene

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import global.AppColors
import kotlinx.coroutines.delay
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.pulling_memo
import zhoutools.composeapp.generated.resources.pulling_time_card
import zhoutools.composeapp.generated.resources.pushing_memo
import zhoutools.composeapp.generated.resources.pushing_time_card
import kotlin.math.roundToInt

enum class ProcessState(val value: Int) {
    PUSHING_MEMO(0),
    PUSHING_TIME_CARD(1),
    PULLING_MEMO(10),
    PULLING_TIME_CARD(11)
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SyncScene(navigator: Navigator, mode: String) {
    var progress by remember { mutableFloatStateOf(0f) }
    val progressStates = remember { mutableStateListOf<Int>() }

    LaunchedEffect(Unit) {
        animate(0f, 1f, animationSpec = tween(durationMillis = 2000)) { value, _ ->
            progress = value
            if (mode == "push") {
                if (progress >= 0f && ProcessState.PUSHING_MEMO.value !in progressStates) {
                    progressStates.add(ProcessState.PUSHING_MEMO.value)
                }
                if (progress >= 0.5f && ProcessState.PUSHING_TIME_CARD.value !in progressStates) {
                    progressStates.add(ProcessState.PUSHING_TIME_CARD.value)
                }
            } else if (mode == "pull") {
                if (progress >= 0f && ProcessState.PULLING_MEMO.value !in progressStates) {
                    progressStates.add(ProcessState.PULLING_MEMO.value)
                }
                if (progress >= 0.5f && ProcessState.PULLING_TIME_CARD.value !in progressStates) {
                    progressStates.add(ProcessState.PULLING_TIME_CARD.value)
                }
            }
        }
        delay(300)
        navigator.goBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(AppColors.SlightTheme, Color.White))),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                trackColor = AppColors.Divider,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 8.dp
            )

            Text(
                text = "${(progress * 100).roundToInt()}%",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(modifier = Modifier
            .padding(top = 32.dp)
            .height(64.dp)
        ) {
            items(progressStates) {
                val text = when (it) {
                    ProcessState.PUSHING_MEMO.value -> stringResource(Res.string.pushing_memo)
                    ProcessState.PUSHING_TIME_CARD.value -> stringResource(Res.string.pushing_time_card)
                    ProcessState.PULLING_MEMO.value -> stringResource(Res.string.pulling_memo)
                    ProcessState.PULLING_TIME_CARD.value -> stringResource(Res.string.pulling_time_card)
                    else -> ""
                }
                if (text.isNotEmpty()) {
                    Text(text)
                }
            }
        }
    }
}