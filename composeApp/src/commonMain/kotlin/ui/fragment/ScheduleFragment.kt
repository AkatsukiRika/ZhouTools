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
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import constant.RouteConstants
import constant.TimeConstants
import extension.dayStartTime
import global.AppColors
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import model.records.Schedule
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.molecule.rememberPresenter
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.scene.AddScheduleEvent
import ui.scene.AddScheduleObject
import util.CalendarUtil
import util.ScheduleUtil
import util.TimeUtil
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.add_schedule
import zhoutools.composeapp.generated.resources.ic_add
import zhoutools.composeapp.generated.resources.ic_milestone
import zhoutools.composeapp.generated.resources.ic_next
import zhoutools.composeapp.generated.resources.ic_prev
import zhoutools.composeapp.generated.resources.schedule
import zhoutools.composeapp.generated.resources.today
import zhoutools.composeapp.generated.resources.x_days_since
import zhoutools.composeapp.generated.resources.x_days_until

object ScheduleObject {
    private val _eventFlow = MutableSharedFlow<ScheduleEvent?>(replay = 1)

    val eventFlow: SharedFlow<ScheduleEvent?>
        get() = _eventFlow

    fun emitSync(event: ScheduleEvent?) {
        runBlocking {
            _eventFlow.emit(event)
        }
    }
}

sealed interface ScheduleEvent {
    data object RefreshData : ScheduleEvent
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ScheduleFragment(navigator: Navigator) {
    val scope = rememberCoroutineScope()
    val (state, channel) = rememberPresenter(keys = listOf(scope)) { SchedulePresenter(it) }
    val util = remember { ScheduleUtil() }
    val scheduleList = remember { mutableStateListOf<Schedule>() }
    val event = ScheduleObject.eventFlow.collectAsStateWithLifecycle(initial = null).value

    LaunchedEffect(Unit) {
        scheduleList.clear()
        scheduleList.addAll(util.getScheduleList())
    }

    LaunchedEffect(event) {
        if (event != null) {
            when (event) {
                is ScheduleEvent.RefreshData -> {
                    util.refreshData()
                    scheduleList.clear()
                    scheduleList.addAll(util.getScheduleList())
                }
            }
            ScheduleObject.emitSync(null)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
            modifier = Modifier.weight(1f)
        )

        AddScheduleButton(onClick = {
            AddScheduleObject.emitSync(AddScheduleEvent.SetDate(
                year = state.selectDate.first,
                month = state.selectDate.second,
                day = state.selectDate.third
            ))
            navigator.navigate(RouteConstants.ROUTE_ADD_SCHEDULE)
        })

        Spacer(modifier = Modifier.height(16.dp))
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
                    fontSize = if (isSelect) 16.sp else 14.sp,
                    fontWeight = if (isSelect) FontWeight.Bold else FontWeight.Normal,
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
private fun ScheduleCardList(modifier: Modifier = Modifier, list: List<Schedule>) {
    LazyColumn(modifier = modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp)
    ) {
        items(list) {
            if (it.isMilestone) {
                MilestoneCard(it)

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun MilestoneCard(card: Schedule) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(Color.White)
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