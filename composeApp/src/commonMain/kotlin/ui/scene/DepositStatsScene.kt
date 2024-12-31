package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import global.AppColors
import isIOS
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.stringResource
import setNavigationBarColor
import setStatusBarColor
import ui.widget.TitleBar
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.stats

@Composable
fun DepositStatsScene(navigator: Navigator) {
    LaunchedEffect(Unit) {
        setStatusBarColor("#FFFFFF", isLight = true)
        setNavigationBarColor("#F4F4F4", isLight = true)
    }

    var rootModifier = Modifier
        .fillMaxSize()
        .background(AppColors.Background)
    if (isIOS()) {
        rootModifier = rootModifier.navigationBarsPadding()
    }
    Column(modifier = rootModifier) {
        TitleBar(
            navigator = navigator,
            title = stringResource(Res.string.stats)
        )
    }
}