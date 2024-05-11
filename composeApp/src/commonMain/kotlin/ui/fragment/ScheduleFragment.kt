package ui.fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arch.AddScheduleEffect
import arch.EffectObservers
import arch.ScheduleEffect
import constant.RouteConstants
import constant.TimeConstants
import extension.clickableNoRipple
import extension.dayStartTime
import extension.toHourMinString
import global.AppColors
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import model.records.Schedule
import moe.tlaster.precompose.molecule.rememberPresenter
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import util.CalendarUtil
import util.ScheduleUtil
import util.TimeUtil
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.add_schedule
import zhoutools.composeapp.generated.resources.all_day
import zhoutools.composeapp.generated.resources.delete
import zhoutools.composeapp.generated.resources.edit
import zhoutools.composeapp.generated.resources.ic_add
import zhoutools.composeapp.generated.resources.ic_milestone
import zhoutools.composeapp.generated.resources.ic_next
import zhoutools.composeapp.generated.resources.ic_prev
import zhoutools.composeapp.generated.resources.schedule
import zhoutools.composeapp.generated.resources.today
import zhoutools.composeapp.generated.resources.x_days_since
import zhoutools.composeapp.generated.resources.x_days_until

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterialApi::class)
@Composable
fun ScheduleFragment(navigator: Navigator) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val (state, channel) = rememberPresenter(keys = listOf(scope)) { SchedulePresenter(it) }
    val scheduleList = remember { mutableStateListOf<Schedule>() }
    var selectItem by remember { mutableStateOf<Schedule?>(null) }

    fun refreshData() {
        scheduleList.clear()
        scheduleList.addAll(ScheduleUtil.getDisplayList())
    }

    fun onEdit() {
        selectItem?.let {
            EffectObservers.emitAddScheduleEffect(AddScheduleEffect.BeginEdit(schedule = it))
            navigator.navigate(RouteConstants.ROUTE_ADD_SCHEDULE)
            scope.launch {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

    fun onDelete() {
        selectItem?.let {
            ScheduleUtil.deleteSchedule(it)
            refreshData()
            scope.launch {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    EffectObservers.observeScheduleEffect {
        when (it) {
            is ScheduleEffect.RefreshData -> {
                ScheduleUtil.refreshData()
                scheduleList.clear()
                scheduleList.addAll(ScheduleUtil.getDisplayList())
            }
        }
    }

    BottomSheetScaffold(
        sheetContent = {
            BottomSheetContent(
                onEdit = ::onEdit,
                onDelete = ::onDelete
            )
        },
        scaffoldState = scaffoldState,
        sheetGesturesEnabled = true,
        sheetPeekHeight = 0.dp,
        sheetBackgroundColor = Color.White,
        sheetElevation = 0.dp,
        backgroundColor = Color.Transparent
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .clickableNoRipple {
                selectItem = null
                scope.launch {
                    scaffoldState.bottomSheetState.collapse()
                }
            }
        ) {
            Text(
                text = stringResource(Res.string.schedule).uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
            )

            MonthRow(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp),
                state = state,
                channel = channel
            )

            CalendarGrid(state, channel)

            Spacer(modifier = Modifier.height(32.dp))

            ScheduleCardList(
                list = scheduleList,
                modifier = Modifier.weight(1f),
                state = state,
                onClickCard = {
                    selectItem = it
                    scope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                }
            )

            AddScheduleButton(onClick = {
                EffectObservers.emitAddScheduleEffect(AddScheduleEffect.SetDate(
                    year = state.selectDate.first,
                    month = state.selectDate.second,
                    day = state.selectDate.third
                ))
                navigator.navigate(RouteConstants.ROUTE_ADD_SCHEDULE)
            })

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun MonthRow(modifier: Modifier = Modifier, state: ScheduleState, channel: Channel<ScheduleAction>) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_prev),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    channel.trySend(ScheduleAction.GoPrevMonth)
                }
                .padding(8.dp)
                .size(18.dp)
        )

        Text(
            text = "${state.getCurrMonthName()} ${state.currYear}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Icon(
            painter = painterResource(Res.drawable.ic_next),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    channel.trySend(ScheduleAction.GoNextMonth)
                }
                .padding(8.dp)
                .size(18.dp)
        )
    }
}

@Composable
private fun CalendarGrid(state: ScheduleState, channel: Channel<ScheduleAction>) {
    LazyVerticalGrid(
        modifier = Modifier
            .background(Color.White)
            .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
            .fillMaxWidth(),
        columns = GridCells.Fixed(7),
        content = {
            var prevMonthDays = 0
            if (state.currMonthDays.isNotEmpty()) {
                val firstDayOfWeek = state.currMonthDays.first().second
                prevMonthDays = if (firstDayOfWeek == DayOfWeek.SUNDAY) {
                    0
                } else {
                    firstDayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 1
                }
            }

            items(7) {
                Text(
                    text = CalendarUtil.getWeekDays()[it],
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }

            items(prevMonthDays) {
                val prevIndex = state.prevMonthDays.lastIndex - (prevMonthDays - it - 1)
                val prevMonthDay = if (prevIndex in state.prevMonthDays.indices) {
                    state.prevMonthDays[prevIndex].first
                } else 0
                Text(
                    text = "$prevMonthDay",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center,
                    color = Color.LightGray
                )
            }

            items(state.currMonthDays) {
                val isToday = state.isToday(dayOfMonth = it.first)
                val isSelect = state.isSelect(dayOfMonth = it.first)

                Text(
                    text = "${it.first}",
                    fontSize = if (isSelect) 18.sp else 14.sp,
                    fontWeight = if (isSelect) FontWeight.ExtraBold else FontWeight.Normal,
                    modifier = Modifier
                        .clickable {
                            channel.trySend(ScheduleAction.SelectDay(
                                Triple(state.currYear, state.currMonthOfYear, it.first)
                            ))
                        }
                        .background(if (isToday) AppColors.Theme else Color.Transparent)
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    color = if (isToday) Color.White else Color.Black
                )
            }
        }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AddScheduleButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.LightTheme)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(Res.string.add_schedule),
            color = Color.White
        )
    }
}

@Composable
private fun ScheduleCardList(
    modifier: Modifier = Modifier,
    list: List<Schedule>,
    state: ScheduleState,
    onClickCard: (Schedule) -> Unit
) {
    LazyColumn(modifier = modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp)
    ) {
        items(list) {
            if (it.isMilestone) {
                MilestoneCard(it, onClick = {
                    onClickCard(it)
                })
                Spacer(modifier = Modifier.height(16.dp))
            } else if (it.dayStartTime == TimeUtil.toEpochMillis(state.selectDate.first, state.selectDate.second, state.selectDate.third, 0, 0)) {
                NormalCard(it, onClick = {
                    onClickCard(it)
                })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun MilestoneCard(card: Schedule, onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(Color.White)
        .clickable {
            onClick()
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val todayStartTime = TimeUtil.currentTimeMillis().dayStartTime()
            val diffDays = (todayStartTime - card.dayStartTime) / TimeConstants.DAY_MILLIS
            val headerText = when {
                diffDays > 0 -> stringResource(Res.string.x_days_since, diffDays)
                diffDays < 0 -> stringResource(Res.string.x_days_until, -diffDays)
                else -> stringResource(Res.string.today)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = headerText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = card.text,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Icon(
            painter = painterResource(Res.drawable.ic_milestone),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp)
                .alpha(0.5f),
            tint = Color.Unspecified,
            contentDescription = null
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun NormalCard(card: Schedule, onClick: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(Color.White)
        .clickable {
            onClick()
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            val startTimeText = card.startingTime.toHourMinString()
            val endTimeText = card.endingTime.toHourMinString()
            val headerText = if (card.isAllDay) {
                stringResource(Res.string.all_day)
            } else {
                "$startTimeText - $endTimeText"
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = headerText,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = card.text,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun BottomSheetContent(onEdit: () -> Unit, onDelete: () -> Unit) {
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
            onClick = onDelete,
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            Text(
                text = stringResource(Res.string.delete),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}