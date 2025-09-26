package ui.scene.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import global.AppColors
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
    val viewModel: DetailViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
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
                tabs.forEach { (index, title) ->
                    Tab(
                        selected = state.tab == index,
                        onClick = {
                            viewModel.dispatch(DetailAction.ChangeTab(index))
                        },
                        text = {
                            Text(text = title)
                        }
                    )
                }
            }

            if (state.tab == DETAIL_TAB_HISTORY) {
                HistoryFragment(state.historyState, viewModel::dispatch)
            } else {
                TodayFragment(state.todayState)
            }
        }
    }
}
