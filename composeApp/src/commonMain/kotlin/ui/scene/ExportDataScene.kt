package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import global.AppColors
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import store.AppStore
import ui.widget.TitleBar
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.export_data
import zhoutools.composeapp.generated.resources.memo
import zhoutools.composeapp.generated.resources.schedule
import zhoutools.composeapp.generated.resources.time_card

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ExportDataScene(navigator: Navigator) {
    var exportType by remember { mutableIntStateOf(ExportType.TIME_CARD.ordinal) }
    var text by remember { mutableStateOf(AppStore.timeCards) }

    LaunchedEffect(exportType) {
        text = when (exportType) {
            ExportType.SCHEDULE.ordinal -> AppStore.schedules
            ExportType.MEMO.ordinal -> AppStore.memos
            else -> AppStore.timeCards
        }
    }

    Column(modifier = Modifier
        .imePadding()
        .fillMaxSize()
        .background(AppColors.Background)
    ) {
        val chipColors = FilterChipDefaults.elevatedFilterChipColors(
            containerColor = AppColors.SlightTheme,
            selectedContainerColor = AppColors.Theme,
            selectedLabelColor = Color.White
        )

        TitleBar(
            navigator = navigator,
            title = stringResource(Res.string.export_data)
        )

        LazyRow {
            item {
                Spacer(modifier = Modifier.width(16.dp))
            }

            item {
                ElevatedFilterChip(
                    selected = exportType == ExportType.TIME_CARD.ordinal,
                    onClick = {
                        exportType = ExportType.TIME_CARD.ordinal
                    },
                    label = {
                        Text(text = stringResource(Res.string.time_card))
                    },
                    colors = chipColors
                )

                Spacer(modifier = Modifier.width(16.dp))
            }

            item {
                ElevatedFilterChip(
                    selected = exportType == ExportType.SCHEDULE.ordinal,
                    onClick = {
                        exportType = ExportType.SCHEDULE.ordinal
                    },
                    label = {
                        Text(text = stringResource(Res.string.schedule))
                    },
                    colors = chipColors
                )

                Spacer(modifier = Modifier.width(16.dp))
            }

            item {
                ElevatedFilterChip(
                    selected = exportType == ExportType.MEMO.ordinal,
                    onClick = {
                        exportType = ExportType.MEMO.ordinal
                    },
                    label = {
                        Text(text = stringResource(Res.string.memo))
                    },
                    colors = chipColors
                )

                Spacer(modifier = Modifier.width(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
            .background(AppColors.DarkBackground)
        ) {
            Text(
                text = text,
                color = Color.White,
                modifier = Modifier
                    .padding(all = 8.dp)
                    .fillMaxSize(),
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private enum class ExportType {
    TIME_CARD, SCHEDULE, MEMO
}