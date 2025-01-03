package ui.scene

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import constant.TabConstants
import ui.fragment.SettingsFragment
import ui.fragment.TimeCardFragment
import global.AppColors
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.BackHandler
import moe.tlaster.precompose.navigation.Navigator
import ui.fragment.DepositFragment
import ui.fragment.MemoFragment
import ui.fragment.ScheduleFragment
import ui.widget.BaseImmersiveScene
import ui.widget.BottomBar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScene(navigator: Navigator) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun showSnackbar(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    BaseImmersiveScene(
        statusBarColorStr = "#F4F4F4",
        navigationBarColorStr = "#FFFFFF",
        statusBarPadding = true,
        navigationBarPadding = false,
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
            val pagerState = rememberPagerState(initialPage = TabConstants.TAB_TIME_CARD, pageCount = { TabConstants.TAB_COUNT })

            Column(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (it) {
                        TabConstants.TAB_TIME_CARD -> TimeCardFragment(
                            modifier = Modifier.fillMaxSize(),
                            navigator = navigator
                        )
                        TabConstants.TAB_SETTINGS -> SettingsFragment(
                            modifier = Modifier.fillMaxSize(),
                            navigator = navigator,
                            showSnackbar = ::showSnackbar
                        )
                        TabConstants.TAB_MEMO -> MemoFragment(navigator = navigator)
                        TabConstants.TAB_SCHEDULE -> ScheduleFragment(navigator = navigator)
                        TabConstants.TAB_DEPOSIT -> DepositFragment(navigator = navigator)
                        else -> TimeCardFragment(
                            modifier = Modifier.fillMaxSize(),
                            navigator = navigator
                        )
                    }
                }

                BottomBar(
                    selectIndex = pagerState.currentPage,
                    onSelect = {
                        scope.launch {
                            pagerState.scrollToPage(page = it)
                        }
                    }
                )
            }

            BackHandler {
                // Do nothing.
            }
        }
    }
}