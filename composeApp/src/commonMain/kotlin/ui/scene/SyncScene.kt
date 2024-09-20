package ui.scene

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import global.AppColors
import helper.SyncHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.stringResource
import setStatusBarColor
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.pull_failed
import zhoutools.composeapp.generated.resources.pull_success
import zhoutools.composeapp.generated.resources.pulling_deposit
import zhoutools.composeapp.generated.resources.pulling_memo
import zhoutools.composeapp.generated.resources.pulling_schedule
import zhoutools.composeapp.generated.resources.pulling_time_card
import zhoutools.composeapp.generated.resources.pushing_deposit
import zhoutools.composeapp.generated.resources.pushing_memo
import zhoutools.composeapp.generated.resources.pushing_schedule
import zhoutools.composeapp.generated.resources.pushing_time_card
import zhoutools.composeapp.generated.resources.sync_failed
import zhoutools.composeapp.generated.resources.sync_success
import kotlin.math.roundToInt

enum class ProcessState(val value: Int) {
    PUSHING_MEMO(0),
    PUSHING_TIME_CARD(1),
    PUSHING_SCHEDULE(2),
    PUSHING_DEPOSIT(3),
    PULLING_MEMO(10),
    PULLING_TIME_CARD(11),
    PULLING_SCHEDULE(12),
    PULLING_DEPOSIT(13),
    SYNC_FAILED(20),
    SYNC_SUCCESS(21),
    PULL_FAILED(22),
    PULL_SUCCESS(23)
}

@Composable
fun SyncScene(navigator: Navigator, mode: String) {
    var progressValue by remember { mutableFloatStateOf(0f) }
    val progressState by animateFloatAsState(targetValue = progressValue)
    val processStates = remember { mutableStateListOf<Int>() }

    suspend fun goBack() {
        delay(1000)
        navigator.goBack()
    }

    fun onError() {
        if (mode == "push") {
            processStates.add(ProcessState.SYNC_FAILED.value)
        } else if (mode == "pull") {
            processStates.add(ProcessState.PULL_FAILED.value)
        }
    }

    fun onSuccess() {
        if (mode == "push") {
            processStates.add(ProcessState.SYNC_SUCCESS.value)
        } else if (mode == "pull") {
            processStates.add(ProcessState.PULL_SUCCESS.value)
        }
    }

    LaunchedEffect(Unit) {
        setStatusBarColor("#FFEAE3", isLight = true)

        withContext(Dispatchers.IO) {
            if (mode == "push") {
                SyncHelper.pushMemo(::onSuccess, ::onError)
                progressValue = 1 / 4f
                SyncHelper.pushTimeCard(::onSuccess, ::onError)
                progressValue = 2 / 4f
                SyncHelper.pushSchedule(::onSuccess, ::onError)
                progressValue = 3 / 4f
                SyncHelper.pushDepositMonths(::onSuccess, ::onError)
                progressValue = 1f
            } else if (mode == "pull") {
                SyncHelper.pullMemo(::onSuccess, ::onError)
                progressValue = 1 / 4f
                SyncHelper.pullTimeCard(::onSuccess, ::onError)
                progressValue = 2 / 4f
                SyncHelper.pullSchedule(::onSuccess, ::onError)
                progressValue = 3 / 4f
                SyncHelper.pullDepositMonths(::onSuccess, ::onError)
                progressValue = 1f
            }
            goBack()
        }
    }

    LaunchedEffect(progressValue) {
        if (mode == "push") {
            if (progressValue >= 0f && ProcessState.PUSHING_MEMO.value !in processStates) {
                processStates.add(ProcessState.PUSHING_MEMO.value)
            }
            if (progressValue >= 1 / 4f && ProcessState.PUSHING_TIME_CARD.value !in processStates) {
                processStates.add(ProcessState.PUSHING_TIME_CARD.value)
            }
            if (progressValue >= 2 / 4f && ProcessState.PUSHING_SCHEDULE.value !in processStates) {
                processStates.add(ProcessState.PUSHING_SCHEDULE.value)
            }
            if (progressValue >= 3 / 4f && ProcessState.PUSHING_DEPOSIT.value !in processStates) {
                processStates.add(ProcessState.PUSHING_DEPOSIT.value)
            }
        } else if (mode == "pull") {
            if (progressValue >= 0f && ProcessState.PULLING_MEMO.value !in processStates) {
                processStates.add(ProcessState.PULLING_MEMO.value)
            }
            if (progressValue >= 1 / 4f && ProcessState.PULLING_TIME_CARD.value !in processStates) {
                processStates.add(ProcessState.PULLING_TIME_CARD.value)
            }
            if (progressValue >= 2 / 4f && ProcessState.PULLING_SCHEDULE.value !in processStates) {
                processStates.add(ProcessState.PULLING_SCHEDULE.value)
            }
            if (progressValue >= 3 / 4f && ProcessState.PULLING_DEPOSIT.value !in processStates) {
                processStates.add(ProcessState.PULLING_DEPOSIT.value)
            }
        }
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
                progress = { progressState },
                trackColor = AppColors.Divider,
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 8.dp
            )

            Text(
                text = "${(progressState * 100).roundToInt()}%",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold
            )
        }

        val annotatedString = buildAnnotatedString {
            processStates.forEach {
                val text = when (it) {
                    ProcessState.PUSHING_MEMO.value -> stringResource(Res.string.pushing_memo)
                    ProcessState.PUSHING_TIME_CARD.value -> stringResource(Res.string.pushing_time_card)
                    ProcessState.PUSHING_SCHEDULE.value -> stringResource(Res.string.pushing_schedule)
                    ProcessState.PUSHING_DEPOSIT.value -> stringResource(Res.string.pushing_deposit)
                    ProcessState.PULLING_MEMO.value -> stringResource(Res.string.pulling_memo)
                    ProcessState.PULLING_TIME_CARD.value -> stringResource(Res.string.pulling_time_card)
                    ProcessState.PULLING_SCHEDULE.value -> stringResource(Res.string.pulling_schedule)
                    ProcessState.PULLING_DEPOSIT.value -> stringResource(Res.string.pulling_deposit)
                    ProcessState.SYNC_FAILED.value -> stringResource(Res.string.sync_failed)
                    ProcessState.SYNC_SUCCESS.value -> stringResource(Res.string.sync_success)
                    ProcessState.PULL_FAILED.value -> stringResource(Res.string.pull_failed)
                    ProcessState.PULL_SUCCESS.value -> stringResource(Res.string.pull_success)
                    else -> ""
                }
                val textColor = if (it in listOf(ProcessState.SYNC_FAILED.value, ProcessState.PULL_FAILED.value)) {
                    AppColors.Red
                } else if (it in listOf(ProcessState.SYNC_SUCCESS.value, ProcessState.PULL_SUCCESS.value)) {
                    AppColors.DarkGreen
                } else {
                    Color.Black.copy(alpha = 0.6f)
                }
                withStyle(SpanStyle(color = textColor)) {
                    appendLine(text)
                }
            }
        }

        val longestAnnotatedString = buildAnnotatedString {
            repeat(8) {
                withStyle(SpanStyle(color = AppColors.DarkGreen)) {
                    appendLine(stringResource(Res.string.pulling_schedule))
                }
            }
        }

        Box(modifier = Modifier.padding(top = 64.dp)) {
            Text(text = annotatedString)

            Text(
                text = longestAnnotatedString,
                modifier = Modifier.alpha(0f)
            )
        }
    }
}