package ui.fragment

import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import constant.RouteConstants
import extension.clickableNoRipple
import extension.roundToDecimalPlaces
import extension.toDays
import extension.toMoneyDisplayStr
import global.AppColors
import helper.SyncHelper
import helper.effect.EffectHelper
import helper.effect.MemoEffect
import model.display.GroupDisplayItem
import model.display.MemoDisplayItem
import model.records.GOAL_TYPE_DEPOSIT
import model.records.GOAL_TYPE_TIME
import model.records.Goal
import model.records.Memo
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.widget.AutoSyncIndicator
import ui.widget.ShimmerProgressBar
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.days
import zhoutools.composeapp.generated.resources.edit
import zhoutools.composeapp.generated.resources.goals
import zhoutools.composeapp.generated.resources.ic_add
import zhoutools.composeapp.generated.resources.ic_deposit_goal
import zhoutools.composeapp.generated.resources.ic_pin
import zhoutools.composeapp.generated.resources.ic_time_card
import zhoutools.composeapp.generated.resources.ic_todo
import zhoutools.composeapp.generated.resources.ic_todo_finished
import zhoutools.composeapp.generated.resources.mark_done
import zhoutools.composeapp.generated.resources.memo

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MemoFragment(navigator: Navigator) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val viewModel: MemoViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.dispatch(MemoAction.InitGoals)
    }

    LaunchedEffect(Unit) {
        viewModel.memoEvent.collect { event ->
            when (event) {
                is MemoEvent.GoToEditScene -> {
                    navigator.navigateForResult(RouteConstants.ROUTE_WRITE_MEMO.replace(RouteConstants.PARAM_EDIT, "true"))
                }
            }
        }
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
            viewModel.dispatch(MemoAction.RefreshDisplayList)
        })
    }

    EffectHelper.observeMemoEffect {
        when (it) {
            is MemoEffect.RefreshData -> {
                viewModel.dispatch(MemoAction.RefreshDisplayList)
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .clickableNoRipple {
            viewModel.dispatch(MemoAction.HideBottomSheet)
        }
    ) {
        BottomSheetScaffold(
            sheetContent = {
                BottomSheetContent(
                    onEdit = {
                        viewModel.dispatch(MemoAction.ClickEdit)
                    },
                    onMarkDone = {
                        viewModel.dispatch(MemoAction.MarkDone)
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
                                viewModel.dispatch(MemoAction.SwitchMode(MODE_MEMO))
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
                                viewModel.dispatch(MemoAction.SwitchMode(MODE_GOALS))
                            }
                            .alpha(if (state.mode == MODE_GOALS) 1f else 0.2f)
                    )
                }

                if (state.mode == MODE_MEMO) {
                    MemosLayout(state, viewModel::dispatch, showBottomSpace = scaffoldState.bottomSheetState.isExpanded)
                } else {
                    GoalsLayout(state, showBottomSpace = scaffoldState.bottomSheetState.isExpanded)
                }
            }
        }

        if (state.mode == MODE_MEMO && scaffoldState.bottomSheetState.isCollapsed && scaffoldState.bottomSheetState.targetValue == scaffoldState.bottomSheetState.currentValue) {
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
private fun MemosLayout(state: MemoState, onAction: (MemoAction) -> Unit, showBottomSpace: Boolean) {
    val lazyListState = rememberLazyListState()

    LazyColumn(state = lazyListState) {
        items(state.displayList) { item ->
            if (item is GroupDisplayItem) {
                GroupItem(item.name)
            } else if (item is MemoDisplayItem) {
                MemoItem(item.memo, onAction)
            }
        }

        if (showBottomSpace) {
            item {
                Spacer(modifier = Modifier.height(148.dp))
            }
        }
    }

    LaunchedEffect(showBottomSpace) {
        if (showBottomSpace) {
            val selectIndex = state.displayList.indexOfFirst {
                it is MemoDisplayItem && it.memo == state.curMemo
            }
            if (selectIndex in state.displayList.indices) {
                lazyListState.animateScrollToItem(selectIndex)
            }
        }
    }
}

@Composable
private fun GoalsLayout(state: MemoState, showBottomSpace: Boolean) {
    val lazyListState = rememberLazyListState()

    LazyColumn(state = lazyListState) {
        val depositGoalsList = state.goalList.filter { it.type == GOAL_TYPE_DEPOSIT }

        if (depositGoalsList.isNotEmpty()) {
            item {
                Icon(
                    painter = painterResource(Res.drawable.ic_deposit_goal),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                        .size(28.dp),
                    tint = Color.Gray
                )
            }

            items(depositGoalsList) { goal ->
                GoalItem(goal)
            }
        }

        val timeGoalsList = state.goalList.filter { it.type == GOAL_TYPE_TIME }

        if (timeGoalsList.isNotEmpty()) {
            item {
                Icon(
                    painter = painterResource(Res.drawable.ic_time_card),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
                        .size(28.dp),
                    tint = Color.Gray
                )
            }

            items(timeGoalsList) { goal ->
                GoalItem(goal)
            }
        }

        if (showBottomSpace) {
            item {
                Spacer(modifier = Modifier.height(148.dp))
            }
        }
    }
}

@Composable
private fun GroupItem(name: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(AppColors.Divider)
        )

        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Box(modifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(AppColors.Divider)
        )
    }
}

@Composable
private fun MemoItem(memo: Memo, onAction: (MemoAction) -> Unit) {
    Card(modifier = Modifier
        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(4.dp))
        .clickable {
            onAction(MemoAction.ClickMemoItem(memo))
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
private fun GoalItem(goal: Goal) {
    Card(modifier = Modifier
        .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
    ) {
        var height by remember { mutableIntStateOf(0) }

        Box(modifier = Modifier.onSizeChanged { height = it.height }) {
            val progress = goal.getProgress()

            ShimmerProgressBar(
                progress = progress,
                color = if (goal.type == GOAL_TYPE_DEPOSIT) AppColors.LightGold.copy(0.5f) else AppColors.LightTheme.copy(0.5f),
                backgroundColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(with(LocalDensity.current) {
                        height.toDp()
                    })
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .padding(all = 10.dp)
                    .align(Alignment.CenterStart)
            ) {
                val currentValueStr = if (goal.type == GOAL_TYPE_DEPOSIT) {
                    goal.currentValue.toMoneyDisplayStr()
                } else {
                    goal.currentValue.toDays().toString()
                }

                val goalValueStr = if (goal.type == GOAL_TYPE_DEPOSIT) {
                    goal.goalValue.toMoneyDisplayStr()
                } else {
                    "${goal.goalValue.toDays()} ${stringResource(Res.string.days)}"
                }

                Text(
                    text = currentValueStr,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = 18.sp
                )

                Text(
                    text = " / $goalValueStr",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    lineHeight = 12.sp
                )
            }

            Text(
                text = "${(progress * 100).roundToDecimalPlaces(4)}%",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(all = 10.dp)
                    .align(Alignment.CenterEnd),
                color = Color.DarkGray,
                fontSize = 20.sp
            )
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
