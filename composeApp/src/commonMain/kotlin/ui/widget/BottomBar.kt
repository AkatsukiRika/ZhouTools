package ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.agent
import zhoutools.composeapp.generated.resources.ic_agent
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

@Composable
fun BottomBar(
    modifier: Modifier = Modifier,
    tabs: List<Int>,
    selectIndex: Int,
    onSelect: (Int) -> Unit
) {
    val rootModifier = modifier
        .background(Color.White)
        .navigationBarsPadding()
        .fillMaxWidth()
        .height(58.dp)

    Row(
        modifier = rootModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        tabs.forEachIndexed { index, tabId ->
            Spacer(modifier = Modifier.weight(1f))

            BottomBarItem(
                index = index,
                selectIndex = selectIndex,
                icon = getTabIcon(tabId),
                name = getTabName(tabId)
            ) {
                onSelect(it)
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

private fun getTabIcon(tabId: Int): DrawableResource {
    return when (tabId) {
        TabConstants.TAB_TIME_CARD -> Res.drawable.ic_time_card
        TabConstants.TAB_SCHEDULE -> Res.drawable.ic_schedule
        TabConstants.TAB_MEMO -> Res.drawable.ic_memo
        TabConstants.TAB_DEPOSIT -> Res.drawable.ic_deposit
        TabConstants.TAB_SETTINGS -> Res.drawable.ic_settings
        TabConstants.TAB_AGENT -> Res.drawable.ic_agent
        else -> Res.drawable.ic_time_card
    }
}

private fun getTabName(tabId: Int): StringResource {
    return when (tabId) {
        TabConstants.TAB_TIME_CARD -> Res.string.time_card
        TabConstants.TAB_SCHEDULE -> Res.string.schedule
        TabConstants.TAB_MEMO -> Res.string.memo
        TabConstants.TAB_DEPOSIT -> Res.string.deposit
        TabConstants.TAB_SETTINGS -> Res.string.settings
        TabConstants.TAB_AGENT -> Res.string.agent
        else -> Res.string.time_card
    }
}

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