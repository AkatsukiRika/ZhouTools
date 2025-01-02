package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import global.AppColors
import isIOS
import kotlinx.coroutines.channels.Channel
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

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                FilterRow(
                    modifier = Modifier.padding(top = 4.dp),
                    state = state,
                    channel = channel
                )
            }

            item {
                Text(
                    text = stringResource(Res.string.total_deposit),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            item {
                BarChart(
                    modifier = Modifier.padding(top = 16.dp),
                    data = state.totalDepositBarData
                )
            }

            item {
                Text(
                    text = stringResource(Res.string.monthly_income),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 32.dp),
                    textAlign = TextAlign.Center
                )
            }

            item {
                LineChart(
                    modifier = Modifier.padding(top = 16.dp),
                    data = state.incomeLineData,
                    pointSpacing = 64.dp
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FilterRow(modifier: Modifier = Modifier, state: DepositStatsState, channel: Channel<DepositStatsAction>) {
    LazyRow(modifier = modifier) {
        item {
            Spacer(modifier = Modifier.width(16.dp))
        }

        items(state.filterOptions.keys.toList()) {
            ElevatedFilterChip(
                selected = state.filterOptions[it] ?: false,
                onClick = {
                    val currentSelected = state.filterOptions[it] ?: false
                    channel.trySend(DepositStatsAction.SelectOption(it, !currentSelected))
                },
                label = {
                    Text(text = it)
                },
                colors = AppColors.getChipColors()
            )

            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}