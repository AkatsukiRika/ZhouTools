package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import global.AppColors
import logger
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
import zhoutools.composeapp.generated.resources.selected_total
import zhoutools.composeapp.generated.resources.stats
import zhoutools.composeapp.generated.resources.total_deposit
import zhoutools.composeapp.generated.resources.value_mode
import zhoutools.composeapp.generated.resources.value_mode_diff1
import zhoutools.composeapp.generated.resources.value_mode_diff2
import zhoutools.composeapp.generated.resources.value_mode_full
import zhoutools.composeapp.generated.resources.value_mode_none

@Composable
fun DepositStatsScene(navigator: Navigator) {
    val viewModel = viewModel { DepositStatsViewModel() }
    val state by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                logger.i { "DepositStatsScene onResume" }
                viewModel.dispatch(DepositStatsAction.Reset)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                        dispatch = viewModel::dispatch
                    )
                }

                item {
                    if (state.totalDepositBarData.isEmpty() && state.incomeLineData.isEmpty()) {
                        EmptyLayout(description = stringResource(Res.string.no_data))
                    } else {
                        MainCharts(state, viewModel::dispatch)
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
private fun MainCharts(state: DepositStatsState, dispatch: (DepositStatsAction) -> Unit) {
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

        BarChartLegend(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 6.dp)
                .clip(RoundedCornerShape(32.dp))
                .clickable {
                    dispatch(DepositStatsAction.ToggleShowExtraDeposit)
                }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            state
        )

        BarChart(
            modifier = Modifier.padding(top = 10.dp),
            data = state.totalDepositBarData
        )

        SettingsLayout(state = state, dispatch = dispatch)

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

        LineChartLegend(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 12.dp),
            state = state
        )

        LineChart(
            modifier = Modifier.padding(top = 16.dp),
            data = state.incomeLineData,
            pointSpacing = 64.dp
        )
    }
}

@Composable
private fun BarChartLegend(modifier: Modifier = Modifier, state: DepositStatsState) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(12.dp)
            .background(if (state.showExtraDeposit) AppColors.LightGold else Color.Transparent)
            .border(1.dp, AppColors.LightGold)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = stringResource(Res.string.extra_deposit),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun LineChartLegend(modifier: Modifier = Modifier, state: DepositStatsState) {
    val annotatedString = buildAnnotatedString {
        withStyle(SpanStyle(fontSize = 12.sp)) {
            append(stringResource(Res.string.selected_total))
        }

        withStyle(SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)) {
            append(state.totalIncome.toString())
        }
    }

    Text(
        text = annotatedString,
        modifier = modifier
    )
}

@Composable
private fun FilterRow(modifier: Modifier = Modifier, state: DepositStatsState, dispatch: (DepositStatsAction) -> Unit) {
    LazyRow(modifier = modifier) {
        item {
            Spacer(modifier = Modifier.width(16.dp))
        }

        items(state.filterOptions.keys.toList()) {
            ElevatedFilterChip(
                selected = state.filterOptions[it] ?: false,
                onClick = {
                    val currentSelected = state.filterOptions[it] ?: false
                    dispatch(DepositStatsAction.SelectOption(it, !currentSelected))
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

@Composable
private fun SettingsLayout(modifier: Modifier = Modifier, state: DepositStatsState, dispatch: (DepositStatsAction) -> Unit) {
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, top = 24.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color.White)
    ) {
        val valueModeStr = when (state.valueMode) {
            VALUE_MODE_NONE -> stringResource(Res.string.value_mode_none)
            VALUE_MODE_DIFF1 -> stringResource(Res.string.value_mode_diff1)
            VALUE_MODE_DIFF2 -> stringResource(Res.string.value_mode_diff2)
            else -> stringResource(Res.string.value_mode_full)
        }

        Row(
            modifier = Modifier
                .clickable {
                    dispatch(DepositStatsAction.ToggleValueMode)
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.value_mode),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = valueModeStr,
                fontSize = 16.sp
            )
        }
    }
}
