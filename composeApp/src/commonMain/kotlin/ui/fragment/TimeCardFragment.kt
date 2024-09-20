package ui.fragment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import constant.RouteConstants
import extension.toDateString
import extension.toTimeString
import global.AppColors
import helper.effect.EffectHelper
import helper.effect.TimeCardEffect
import moe.tlaster.precompose.molecule.rememberPresenter
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import helper.TimeCardHelper
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.ic_details
import zhoutools.composeapp.generated.resources.ic_overtime
import zhoutools.composeapp.generated.resources.ic_work_enough
import zhoutools.composeapp.generated.resources.ot_run
import zhoutools.composeapp.generated.resources.press_time_card
import zhoutools.composeapp.generated.resources.run_now
import zhoutools.composeapp.generated.resources.time_card
import zhoutools.composeapp.generated.resources.working_time

@Composable
fun TimeCardFragment(
    modifier: Modifier = Modifier,
    navigator: Navigator
) {
    val (state, channel) = rememberPresenter { TimeCardPresenter(actionFlow = it) }

    EffectHelper.observeTimeCardEffect {
        when (it) {
            is TimeCardEffect.RefreshTodayState -> {
                channel.trySend(TimeCardAction.RefreshTodayState)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleLayout(onDetailsClick = {
            navigator.navigate(RouteConstants.ROUTE_DETAILS)
        })

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
                enabled = state.todayRunTime == 0L
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
                isRun = state.todayRunTime != 0L,
                onRun = {
                    channel.trySend(TimeCardAction.Run)
                }
            )
        }
    }
}

@Composable
fun TitleLayout(onDetailsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.time_card).uppercase(),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            painter = painterResource(Res.drawable.ic_details),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable {
                    onDetailsClick()
                }
        )
    }
}

@Composable
fun HasTodayCardLayout(workingTime: Long, isRun: Boolean, onRun: () -> Unit) {
    val isEnoughWork = workingTime >= TimeCardHelper.getMinWorkingTimeMillis()
    val isEnoughOT = workingTime >= TimeCardHelper.getMinOvertimeMillis()

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