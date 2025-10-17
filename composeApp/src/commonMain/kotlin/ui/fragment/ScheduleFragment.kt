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
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import constant.RouteConstants
import constant.TimeConstants
import extension.clickableNoRipple
import extension.dayStartTime
import extension.toHourMinString
import global.AppColors
import helper.ScheduleHelper
import helper.SyncHelper
import helper.effect.AddScheduleEffect
import helper.effect.EffectHelper
import helper.effect.ScheduleEffect
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import model.calendar.MonthDay
import model.records.Schedule
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.widget.FragmentHeader
import util.CalendarUtil
import util.TimeUtil
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.add_schedule
import zhoutools.composeapp.generated.resources.all_day
import zhoutools.composeapp.generated.resources.day_off
import zhoutools.composeapp.generated.resources.day_work
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
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScheduleFragment(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val viewModel = viewModel { ScheduleViewModel() }
    val state by viewModel.uiState.collectAsState()
    val scheduleList = remember { mutableStateListOf<Schedule>() }
    var selectItem by remember { mutableStateOf<Schedule?>(null) }

    fun refreshData() {
        scheduleList.clear()
        scheduleList.addAll(ScheduleHelper.getDisplayList())
    }

    fun onEdit() {
        selectItem?.let {
            EffectHelper.emitAddScheduleEffect(AddScheduleEffect.BeginEdit(schedule = it))
            navController.navigate(RouteConstants.ROUTE_ADD_SCHEDULE)
            scope.launch {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

    fun onDelete() {
        selectItem?.let {
            ScheduleHelper.deleteSchedule(it)
            refreshData()
            SyncHelper.autoPushSchedule()
            scope.launch {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
        SyncHelper.autoPullSchedule(onSuccess = {
            refreshData()
        })
    }

    EffectHelper.observeScheduleEffect {
        when (it) {
            is ScheduleEffect.RefreshData -> {
                scheduleList.clear()
                scheduleList.addAll(ScheduleHelper.getDisplayList())
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
            FragmentHeader(title = stringResource(Res.string.schedule))

            MonthRow(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 8.dp),
                state = state,
                onAction = viewModel::dispatch
            )

            CalendarGrid(state, viewModel::dispatch)

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
                EffectHelper.emitAddScheduleEffect(
                    AddScheduleEffect.SetDate(
                    year = state.selectDate.first,
                    month = state.selectDate.second,
                    day = state.selectDate.third
                ))
                navController.navigate(RouteConstants.ROUTE_ADD_SCHEDULE)
            })

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MonthRow(modifier: Modifier = Modifier, state: ScheduleState, onAction: (ScheduleAction) -> Unit) {
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
                    onAction(ScheduleAction.GoPrevMonth)
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
                    onAction(ScheduleAction.GoNextMonth)
                }
                .padding(8.dp)
                .size(18.dp)
        )
    }
}

@Composable
private fun CalendarGrid(state: ScheduleState, onAction: (ScheduleAction) -> Unit) {
    LazyVerticalGrid(
        modifier = Modifier
            .background(Color.White)
            .padding(start = 12.dp, end = 12.dp, bottom = 6.dp)
            .fillMaxWidth(),
        columns = GridCells.Fixed(7),
        content = {
            var prevMonthDays = 0
            if (state.currMonthDays.isNotEmpty()) {
                val firstDayOfWeek = state.currMonthDays.first().dayOfWeek
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
                    state.prevMonthDays[prevIndex].day
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
                CurrentMonthDay(state, it, onAction)
            }
        }
    )
}

@Composable
private fun CurrentMonthDay(state: ScheduleState, dayOfMonth: MonthDay, onAction: (ScheduleAction) -> Unit) {
    val isToday = state.isToday(dayOfMonth.day)
    val isSelect = state.isSelect(dayOfMonth.day)
    val isHoliday = dayOfMonth.isHoliday

    Box(modifier = Modifier
        .clickable {
            onAction(ScheduleAction.SelectDay(
                Triple(state.currYear, state.currMonthOfYear, dayOfMonth.day)
            ))
        }
        .background(if (isToday) AppColors.Theme else Color.Transparent)
    ) {
        Text(
            text = "${dayOfMonth.day}",
            fontSize = if (isSelect) 18.sp else 14.sp,
            fontWeight = if (isSelect) FontWeight.ExtraBold else FontWeight.Normal,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(8.dp),
            textAlign = TextAlign.Center,
            color = if (isToday) Color.White else Color.Black
        )

        if (isHoliday != CalendarUtil.NOT_HOLIDAY) {
            // Text in Material3 comes with no default padding
            androidx.compose.material3.Text(
                text = stringResource(
                    if (isHoliday == CalendarUtil.WORK_DAY) Res.string.day_work else Res.string.day_off
                ).uppercase(),
                modifier = Modifier.align(Alignment.BottomCenter),
                fontSize = 9.sp,
                color = if (isToday) Color.White else Color.Gray
            )
        }
    }
}

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
        val todayStartTime = TimeUtil.currentTimeMillis().dayStartTime()
        val diffDays = (todayStartTime - card.dayStartTime) / TimeConstants.DAY_MILLIS

        Column(modifier = Modifier.fillMaxSize()) {
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

        if (diffDays > 0 && card.milestoneGoal > 0) {
            LinearProgressIndicator(
                progress = (diffDays * TimeConstants.DAY_MILLIS) / card.milestoneGoal.toFloat(),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

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