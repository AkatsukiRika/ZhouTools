package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import global.AppColors
import kotlinx.coroutines.channels.Channel
import moe.tlaster.precompose.molecule.rememberPresenter
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.stringResource
import ui.widget.BarChart
import ui.widget.BaseImmersiveScene
import ui.widget.EmptyLayout
import ui.widget.LineChart
import ui.widget.TitleBar
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.extra_deposit
import zhoutools.composeapp.generated.resources.monthly_income
import zhoutools.composeapp.generated.resources.no_data
import zhoutools.composeapp.generated.resources.stats
import zhoutools.composeapp.generated.resources.total_deposit

@Composable
fun DepositStatsScene(navigator: Navigator) {
    val (state, channel) = rememberPresenter { DepositStatsPresenter(it) }

    BaseImmersiveScene(modifier = Modifier
        .fillMaxSize()
        .background(AppColors.Background)
    ) {
        Column {
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
                    if (state.totalDepositBarData.isEmpty() && state.incomeLineData.isEmpty()) {
                        EmptyLayout(description = stringResource(Res.string.no_data))
                    } else {
                        MainCharts(state)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun MainCharts(state: DepositStatsState) {
    Column {
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

        BarChartLegend(modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 12.dp)
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
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 32.dp),
            textAlign = TextAlign.Center
        )

        LineChart(
            modifier = Modifier.padding(top = 16.dp),
            data = state.incomeLineData,
            pointSpacing = 64.dp
        )
    }
}

@Composable
private fun BarChartLegend(modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(12.dp)
            .background(AppColors.LightGold)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = stringResource(Res.string.extra_deposit),
            fontSize = 11.sp
        )
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