package ui.widget

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
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.deposit
import zhoutools.composeapp.generated.resources.ic_deposit
import zhoutools.composeapp.generated.resources.ic_memo
import zhoutools.composeapp.generated.resources.ic_schedule
import zhoutools.composeapp.generated.resources.ic_settings
import zhoutools.composeapp.generated.resources.ic_time_card
import zhoutools.composeapp.generated.resources.memo
import zhoutools.composeapp.generated.resources.schedule
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

        BottomBarItem(
            index = TabConstants.TAB_TIME_CARD,
            selectIndex = selectIndex,
            icon = Res.drawable.ic_time_card,
            name = Res.string.time_card
        ) {
            onSelect(it)
        }

        Spacer(modifier = Modifier.weight(1f))

        BottomBarItem(
            index = TabConstants.TAB_SCHEDULE,
            selectIndex = selectIndex,
            icon = Res.drawable.ic_schedule,
            name = Res.string.schedule
        ) {
            onSelect(it)
        }

        Spacer(modifier = Modifier.weight(1f))

        BottomBarItem(
            index = TabConstants.TAB_MEMO,
            selectIndex = selectIndex,
            icon = Res.drawable.ic_memo,
            name = Res.string.memo
        ) {
            onSelect(it)
        }

        Spacer(modifier = Modifier.weight(1f))

        BottomBarItem(
            index = TabConstants.TAB_DEPOSIT,
            selectIndex = selectIndex,
            icon = Res.drawable.ic_deposit,
            name = Res.string.deposit
        ) {
            onSelect(it)
        }

        Spacer(modifier = Modifier.weight(1f))

        BottomBarItem(
            index = TabConstants.TAB_SETTINGS,
            selectIndex = selectIndex,
            icon = Res.drawable.ic_settings,
            name = Res.string.settings
        ) {
            onSelect(it)
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun BottomBarItem(index: Int, selectIndex: Int, icon: DrawableResource, name: StringResource, onSelect: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickableNoRipple {
                onSelect(index)
            }
            .padding(horizontal = 4.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            modifier = Modifier.height(32.dp).offset(y = 4.dp),
            contentDescription = null,
            tint = if (selectIndex == index) AppColors.Theme else Color.Unspecified
        )

        Text(
            text = stringResource(name),
            fontSize = 11.sp,
            color = if (selectIndex == index) AppColors.Theme else Color.Black
        )
    }
}