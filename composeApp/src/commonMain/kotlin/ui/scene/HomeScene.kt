package ui.scene

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.navigation.NavHostController
import constant.TabConstants
import global.AppColors
import kotlinx.coroutines.launch
import store.CurrentProcessStore
import ui.fragment.DepositFragment
import ui.fragment.MemoFragment
import ui.fragment.ScheduleFragment
import ui.fragment.SettingsFragment
import ui.fragment.TimeCardFragment
import ui.widget.BaseImmersiveScene
import ui.widget.BottomBar
import util.BackHandler

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScene(navController: NavHostController) {
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
        val pagerState = rememberPagerState(initialPage = TabConstants.TAB_TIME_CARD, pageCount = { TabConstants.TAB_COUNT })
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                BottomBar(
                    selectIndex = pagerState.currentPage,
                    onSelect = {
                        scope.launch {
                            pagerState.scrollToPage(page = it)
                        }
                    }
                )
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .onSizeChanged {
                        CurrentProcessStore.screenWidthPixels.value = it.width
                    }
            ) {
                when (it) {
                    TabConstants.TAB_TIME_CARD -> TimeCardFragment(
                        modifier = Modifier.fillMaxSize(),
                        navController
                    )
                    TabConstants.TAB_SETTINGS -> SettingsFragment(
                        modifier = Modifier.fillMaxSize(),
                        navController,
                        showSnackbar = ::showSnackbar
                    )
                    TabConstants.TAB_MEMO -> MemoFragment(navController)
                    TabConstants.TAB_SCHEDULE -> ScheduleFragment(navController)
                    TabConstants.TAB_DEPOSIT -> DepositFragment(navController)
                    else -> TimeCardFragment(
                        modifier = Modifier.fillMaxSize(),
                        navController
                    )
                }
            }

            BackHandler {
                // Do nothing.
            }
        }
    }
}