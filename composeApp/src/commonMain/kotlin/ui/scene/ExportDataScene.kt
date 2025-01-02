package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import global.AppColors
import isIOS
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import setClipboardContent
import setNavigationBarColor
import setStatusBarColor
import store.AppStore
import ui.widget.TitleBar
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.copied_to_clipboard
import zhoutools.composeapp.generated.resources.deposit
import zhoutools.composeapp.generated.resources.export_data
import zhoutools.composeapp.generated.resources.ic_copy
import zhoutools.composeapp.generated.resources.memo
import zhoutools.composeapp.generated.resources.schedule
import zhoutools.composeapp.generated.resources.time_card

@Composable
fun ExportDataScene(navigator: Navigator) {
    var exportType by remember { mutableIntStateOf(ExportType.TIME_CARD.ordinal) }
    var text by remember { mutableStateOf(AppStore.timeCards) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    suspend fun copyText() {
        setClipboardContent(text)
        snackbarHostState.showSnackbar(getString(Res.string.copied_to_clipboard))
    }

    LaunchedEffect(Unit) {
        setStatusBarColor("#FFFFFF", isLight = true)
        setNavigationBarColor("#FFFBFE", isLight = true)
    }

    LaunchedEffect(exportType) {
        text = when (exportType) {
            ExportType.SCHEDULE.ordinal -> AppStore.schedules
            ExportType.MEMO.ordinal -> AppStore.memos
            ExportType.DEPOSIT.ordinal -> AppStore.depositMonths
            else -> AppStore.timeCards
        }
    }

    var rootModifier = Modifier
        .imePadding()
        .fillMaxSize()
        .background(Color(0xFFFFFBFE))
    if (isIOS()) {
        rootModifier = rootModifier.navigationBarsPadding()
    }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = rootModifier
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val chipColors = AppColors.getChipColors()

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

                item {
                    ElevatedFilterChip(
                        selected = exportType == ExportType.DEPOSIT.ordinal,
                        onClick = {
                            exportType = ExportType.DEPOSIT.ordinal
                        },
                        label = {
                            Text(text = stringResource(Res.string.deposit))
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
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    fontFamily = FontFamily.Monospace
                )

                Icon(
                    painter = painterResource(Res.drawable.ic_copy),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 8.dp, end = 8.dp)
                        .size(24.dp)
                        .clickable {
                            scope.launch {
                                copyText()
                            }
                        },
                    tint = Color.Unspecified
                )
            }
        }
    }
}

private enum class ExportType {
    TIME_CARD, SCHEDULE, MEMO, DEPOSIT
}