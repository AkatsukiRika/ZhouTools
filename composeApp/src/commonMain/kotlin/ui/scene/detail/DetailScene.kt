package ui.scene.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import global.AppColors
import moe.tlaster.precompose.molecule.rememberPresenter
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.stringResource
import ui.widget.BaseImmersiveScene
import ui.widget.TitleBar
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.details
import zhoutools.composeapp.generated.resources.history
import zhoutools.composeapp.generated.resources.today

@Composable
fun DetailScene(navigator: Navigator) {
    val (state, channel) = rememberPresenter { DetailPresenter(it) }
    val tabs = mapOf(
        DETAIL_TAB_TODAY to stringResource(Res.string.today),
        DETAIL_TAB_HISTORY to stringResource(Res.string.history)
    )

    BaseImmersiveScene(modifier = Modifier
        .fillMaxSize()
        .background(AppColors.Background)
    ) {
        Column {
            TitleBar(
                navigator = navigator,
                title = stringResource(Res.string.details)
            )

            TabRow(
                selectedTabIndex = state.tab,
                backgroundColor = Color.White,
                contentColor = AppColors.Theme
            ) {
                tabs.forEach {
                    val index = it.key
                    val title = it.value
                    Tab(
                        selected = state.tab == index,
                        onClick = {
                            channel.trySend(DetailAction.ChangeTab(index))
                        },
                        text = {
                            Text(text = title)
                        }
                    )
                }
            }

            if (state.tab == DETAIL_TAB_HISTORY) {
                HistoryFragment(state.historyState)
            } else {
                TodayFragment(state.todayState)
            }
        }
    }
}