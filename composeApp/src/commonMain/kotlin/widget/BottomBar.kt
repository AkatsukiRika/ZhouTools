package widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import constant.TabConstants
import extension.clickableNoRipple
import global.AppColors
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.ic_settings
import zhoutools.composeapp.generated.resources.ic_time_card
import zhoutools.composeapp.generated.resources.settings
import zhoutools.composeapp.generated.resources.time_card

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    selectIndex: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(Color.White),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickableNoRipple {
                    onSelect(TabConstants.TAB_TIME_CARD)
                }
                .padding(horizontal = 4.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_time_card),
                modifier = Modifier.height(32.dp).offset(y = 4.dp),
                contentDescription = null,
                tint = if (selectIndex == TabConstants.TAB_TIME_CARD) AppColors.Theme else Color.Unspecified
            )

            Text(
                text = stringResource(Res.string.time_card),
                fontSize = 11.sp,
                color = if (selectIndex == TabConstants.TAB_TIME_CARD) AppColors.Theme else Color.Black
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickableNoRipple {
                    onSelect(TabConstants.TAB_SETTINGS)
                }
                .padding(horizontal = 4.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_settings),
                modifier = Modifier.height(32.dp).offset(y = 4.dp),
                contentDescription = null,
                tint = if (selectIndex == TabConstants.TAB_SETTINGS) AppColors.Theme else Color.Unspecified
            )

            Text(
                text = stringResource(Res.string.settings),
                fontSize = 11.sp,
                color = if (selectIndex == TabConstants.TAB_SETTINGS) AppColors.Theme else Color.Black
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}