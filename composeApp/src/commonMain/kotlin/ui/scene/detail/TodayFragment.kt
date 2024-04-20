package ui.scene.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import extension.toTimeString
import global.AppColors
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.widget.VerticalDivider
import util.TimeCardUtil
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.countdown
import zhoutools.composeapp.generated.resources.countdown_ot
import zhoutools.composeapp.generated.resources.countdown_run
import zhoutools.composeapp.generated.resources.ic_countdown
import zhoutools.composeapp.generated.resources.ic_empty
import zhoutools.composeapp.generated.resources.ic_time_card
import zhoutools.composeapp.generated.resources.no_today_data
import zhoutools.composeapp.generated.resources.run
import zhoutools.composeapp.generated.resources.time_card
import zhoutools.composeapp.generated.resources.time_details
import zhoutools.composeapp.generated.resources.time_run
import zhoutools.composeapp.generated.resources.working_time

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TodayFragment(state: DetailTodayState) {
    if (state.timeCard == 0L) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp)
                .alpha(0.5f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_empty),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.no_today_data),
                fontSize = 16.sp
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_time_card),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(Res.string.time_details),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(stringResource(Res.string.time_card), state.timeCard.toTimeString())
            VerticalDivider()

            InfoRow(
                stringResource(Res.string.time_run),
                if (state.timeRun != 0L) state.timeRun.toTimeString() else "N/A"
            )
            VerticalDivider()

            InfoRow(stringResource(Res.string.working_time), state.timeWork.toTimeString(utc = true))

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.padding(start = 16.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_countdown),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(Res.string.countdown),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            InfoRow(stringResource(Res.string.countdown_run), state.countdownRun.toTimeString(utc = true))
            VerticalDivider()

            InfoRow(stringResource(Res.string.countdown_ot), state.countdownOT.toTimeString(utc = true))

            Spacer(modifier = Modifier.height(48.dp))

            ProgressBar(state)
        }
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            modifier = Modifier.padding(all = 12.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(all = 12.dp)
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ProgressBar(state: DetailTodayState) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(28.dp)
        .padding(horizontal = 12.dp)
    ) {
        LinearProgressIndicator(
            progress = state.progress,
            modifier = Modifier
                .fillMaxSize()
                .border(width = 1.dp, color = AppColors.Divider, shape = RoundedCornerShape(6.dp))
                .clip(RoundedCornerShape(6.dp)),
            color = when {
                state.timeWork < TimeCardUtil.MIN_WORKING_TIME -> AppColors.Theme
                state.timeWork >= TimeCardUtil.MIN_WORKING_TIME && state.timeWork < TimeCardUtil.MIN_OT_TIME -> AppColors.LightGreen
                else -> AppColors.DarkGreen
            }
        )

        Row(modifier = Modifier.fillMaxWidth().offset(y = 24.dp)) {
            Spacer(modifier = Modifier.weight(TimeCardUtil.MIN_WORKING_TIME.toFloat()))

            Text(
                text = stringResource(Res.string.run).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight((TimeCardUtil.MIN_OT_TIME - TimeCardUtil.MIN_WORKING_TIME).toFloat()))
        }
    }
}