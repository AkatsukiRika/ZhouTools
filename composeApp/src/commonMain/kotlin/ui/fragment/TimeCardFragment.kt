package ui.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import extension.toDateString
import extension.toTimeString
import global.AppColors
import moe.tlaster.precompose.molecule.rememberPresenter
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.dialog.ConfirmDialog
import util.TimeCardUtil
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.ic_overtime
import zhoutools.composeapp.generated.resources.ic_work_enough
import zhoutools.composeapp.generated.resources.ot_run
import zhoutools.composeapp.generated.resources.press_time_card
import zhoutools.composeapp.generated.resources.run_now
import zhoutools.composeapp.generated.resources.server_data_confirm_content
import zhoutools.composeapp.generated.resources.server_data_confirm_title
import zhoutools.composeapp.generated.resources.working_time

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TimeCardFragment(modifier: Modifier = Modifier) {
    val (state, channel) = rememberPresenter { TimeCardPresenter(actionFlow = it) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = state.currentTime.toDateString(),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = state.currentTime.toTimeString(),
            fontSize = 32.sp
        )

        if (state.todayTimeCard == 0L) {
            Button(
                onClick = {
                    channel.trySend(TimeCardAction.PressTimeCard)
                },
                modifier = Modifier
                    .padding(top = 32.dp)
                    .width(300.dp)
                    .height(200.dp),
                enabled = state.hasTodayRun.not()
            ) {
                Text(
                    text = stringResource(Res.string.press_time_card).uppercase(),
                    fontSize = 32.sp,
                    color = Color.White,
                    lineHeight = 48.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            HasTodayCardLayout(
                workingTime = state.todayWorkTime,
                isRun = state.hasTodayRun,
                onRun = {
                    channel.trySend(TimeCardAction.Run)
                }
            )
        }
    }

    if (state.showDialog) {
        ConfirmDialog(
            title = stringResource(Res.string.server_data_confirm_title),
            content = stringResource(Res.string.server_data_confirm_content),
            onCancel = {
                channel.trySend(TimeCardAction.CloseDialog)
            },
            onConfirm = {
                channel.trySend(TimeCardAction.UseServerData)
            }
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun HasTodayCardLayout(workingTime: Long, isRun: Boolean, onRun: () -> Unit) {
    val isEnoughWork = workingTime >= TimeCardUtil.MIN_WORKING_TIME
    val isEnoughOT = workingTime >= TimeCardUtil.MIN_OT_TIME

    Text(
        text = stringResource(Res.string.working_time),
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 16.dp)
    )

    Text(
        text = workingTime.toTimeString(utc = true),
        fontSize = 32.sp
    )

    Button(
        onClick = onRun,
        modifier = Modifier
            .padding(top = 32.dp)
            .width(300.dp)
            .height(200.dp),
        enabled = isEnoughWork && !isRun,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isEnoughOT) AppColors.Theme else AppColors.LightTheme
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(
                    if (isEnoughOT) Res.drawable.ic_overtime else Res.drawable.ic_work_enough
                ),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = stringResource(
                    if (isEnoughOT) Res.string.ot_run else Res.string.run_now
                ).uppercase(),
                fontSize = 32.sp,
                color = Color.White,
                lineHeight = 48.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}