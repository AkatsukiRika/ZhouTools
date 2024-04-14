package scene

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import constant.TabConstants
import fragment.SettingsFragment
import fragment.TimeCardFragment
import global.AppColors
import moe.tlaster.precompose.navigation.BackHandler
import moe.tlaster.precompose.navigation.Navigator
import widget.BottomBar

@Composable
fun HomeScene(navigator: Navigator) {
    Scaffold(modifier = Modifier
        .imePadding()
        .fillMaxSize()
        .background(AppColors.Background)
    ) {
        var selectIndex by remember { mutableIntStateOf(TabConstants.TAB_TIME_CARD) }

        Column(modifier = Modifier.fillMaxSize()) {
            when (selectIndex) {
                TabConstants.TAB_TIME_CARD -> {
                    TimeCardFragment(modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                    )
                }
                TabConstants.TAB_SETTINGS -> {
                    SettingsFragment(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        navigator = navigator
                    )
                }
            }

            BottomBar(
                selectIndex = selectIndex,
                onSelect = {
                    selectIndex = it
                }
            )
        }

        BackHandler {
            // Do nothing.
        }
    }
}