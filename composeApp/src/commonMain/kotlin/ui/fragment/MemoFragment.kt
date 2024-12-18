package ui.fragment

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import constant.RouteConstants
import extension.clickableNoRipple
import global.AppColors
import helper.SyncHelper
import helper.effect.EffectHelper
import helper.effect.MemoEffect
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import model.records.Memo
import moe.tlaster.precompose.molecule.rememberPresenter
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.widget.AutoSyncIndicator
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.edit
import zhoutools.composeapp.generated.resources.goals
import zhoutools.composeapp.generated.resources.ic_add
import zhoutools.composeapp.generated.resources.ic_pin
import zhoutools.composeapp.generated.resources.ic_todo
import zhoutools.composeapp.generated.resources.ic_todo_finished
import zhoutools.composeapp.generated.resources.mark_done
import zhoutools.composeapp.generated.resources.memo

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MemoFragment(navigator: Navigator) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()
    val (state, channel) = rememberPresenter(keys = listOf(scope)) {
        MemoPresenter(it, onGoEdit = {
            scope.launch {
                navigator.navigateForResult(RouteConstants.ROUTE_WRITE_MEMO.replace(RouteConstants.PARAM_EDIT, "true"))
            }
        })
    }

    LaunchedEffect(state.showBottomSheet) {
        if (state.showBottomSheet) {
            scaffoldState.bottomSheetState.expand()
        } else {
            scaffoldState.bottomSheetState.collapse()
        }
    }

    LaunchedEffect(Unit) {
        SyncHelper.autoPullMemo(onSuccess = {
            channel.trySend(MemoAction.RefreshDisplayList)
        })
    }

    EffectHelper.observeMemoEffect {
        when (it) {
            is MemoEffect.RefreshData -> {
                channel.trySend(MemoAction.RefreshDisplayList)
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .clickableNoRipple {
            channel.trySend(MemoAction.HideBottomSheet)
        }
    ) {
        BottomSheetScaffold(
            sheetContent = {
                BottomSheetContent(
                    onEdit = {
                        channel.trySend(MemoAction.ClickEdit)
                    },
                    onMarkDone = {
                        channel.trySend(MemoAction.MarkDone)
                    }
                )
            },
            scaffoldState = scaffoldState,
            sheetGesturesEnabled = false,
            sheetPeekHeight = 0.dp,
            sheetBackgroundColor = Color.White,
            sheetElevation = 0.dp,
            backgroundColor = Color.Transparent
        ) {
            Column(
                modifier = Modifier.padding(it),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.memo).uppercase(),
                        fontSize = 24.sp,
                        fontWeight = if (state.mode == MODE_MEMO) FontWeight.ExtraBold else FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                channel.trySend(MemoAction.SwitchMode(MODE_MEMO))
                            }
                            .alpha(if (state.mode == MODE_MEMO) 1f else 0.2f)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    AutoSyncIndicator()

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = stringResource(Res.string.goals).uppercase(),
                        fontSize = 24.sp,
                        fontWeight = if (state.mode == MODE_GOALS) FontWeight.ExtraBold else FontWeight.Bold,
                        modifier = Modifier
                            .clickable {
                                channel.trySend(MemoAction.SwitchMode(MODE_GOALS))
                            }
                            .alpha(if (state.mode == MODE_GOALS) 1f else 0.2f)
                    )
                }

                MemosLayout(state, channel, showBottomSpace = scaffoldState.bottomSheetState.isExpanded)
            }
        }

        if (scaffoldState.bottomSheetState.isCollapsed && scaffoldState.bottomSheetState.targetValue == scaffoldState.bottomSheetState.currentValue) {
            FloatingActionButton(
                backgroundColor = AppColors.Theme,
                onClick = {
                    navigator.navigate(route = RouteConstants.ROUTE_WRITE_MEMO)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 36.dp, end = 18.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun MemosLayout(state: MemoState, channel: Channel<MemoAction>, showBottomSpace: Boolean) {
    val lazyListState = rememberLazyListState()

    LazyColumn(state = lazyListState) {
        items(state.displayList) { memo ->
            MemoItem(memo, channel)
        }

        if (showBottomSpace) {
            item {
                Spacer(modifier = Modifier.height(148.dp))
            }
        }
    }

    LaunchedEffect(showBottomSpace) {
        if (showBottomSpace) {
            val selectIndex = state.displayList.indexOfFirst { it == state.curMemo }
            if (selectIndex in state.displayList.indices) {
                lazyListState.animateScrollToItem(selectIndex)
            }
        }
    }
}

@Composable
private fun MemoItem(memo: Memo, channel: Channel<MemoAction>) {
    Card(modifier = Modifier
        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(4.dp))
        .clickable {
            channel.trySend(MemoAction.ClickMemoItem(memo))
        }
    ) {
        Box(modifier = Modifier.padding(all = 8.dp)) {
            if (memo.isTodo) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(if (memo.isTodoFinished) Res.drawable.ic_todo_finished else Res.drawable.ic_todo),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier
                            .padding(start = 6.dp, end = 12.dp)
                            .size(16.dp)
                    )

                    Text(
                        memo.text,
                        textDecoration = if (memo.isTodoFinished) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            } else {
                Text(memo.text)
            }


            if (memo.isPin) {
                Icon(
                    painter = painterResource(Res.drawable.ic_pin),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@Composable
private fun BottomSheetContent(onEdit: () -> Unit, onMarkDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().clickableNoRipple {},
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 8.dp)
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = stringResource(Res.string.edit),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            )
        }

        Button(
            onClick = onMarkDone,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            Text(
                text = stringResource(Res.string.mark_done),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}