package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import global.AppColors
import isIOS
import moe.tlaster.precompose.molecule.rememberPresenter
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.stringResource
import setNavigationBarColor
import setStatusBarColor
import ui.widget.BarChart
import ui.widget.LineChart
import ui.widget.TitleBar
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.monthly_income
import zhoutools.composeapp.generated.resources.stats
import zhoutools.composeapp.generated.resources.total_deposit

@Composable
fun DepositStatsScene(navigator: Navigator) {
    val (state, channel) = rememberPresenter { DepositStatsPresenter(it) }

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

        Text(
            text = stringResource(Res.string.total_deposit),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        )

        BarChart(
            modifier = Modifier.padding(top = 16.dp),
            data = state.totalDepositBarData
        )

        Text(
            text = stringResource(Res.string.monthly_income),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp)
        )

        LineChart(
            modifier = Modifier.padding(top = 16.dp),
            data = state.incomeLineData,
            pointSpacing = 64.dp
        )
    }
}