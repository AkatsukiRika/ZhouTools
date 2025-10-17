package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import extension.clickableNoRipple
import extension.daysToMillis
import extension.getHour
import extension.getMinute
import extension.toDays
import extension.toHourMinString
import global.AppColors
import helper.effect.AddScheduleEffect
import helper.effect.EffectHelper
import hideSoftwareKeyboard
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import ui.widget.BaseImmersiveScene
import ui.widget.TitleBar
import ui.widget.VerticalDivider
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.add_schedule
import zhoutools.composeapp.generated.resources.all_day
import zhoutools.composeapp.generated.resources.confirm
import zhoutools.composeapp.generated.resources.date
import zhoutools.composeapp.generated.resources.days
import zhoutools.composeapp.generated.resources.edit_schedule
import zhoutools.composeapp.generated.resources.end_time
import zhoutools.composeapp.generated.resources.invalid_number
import zhoutools.composeapp.generated.resources.milestone_goal
import zhoutools.composeapp.generated.resources.save
import zhoutools.composeapp.generated.resources.set_as_milestone
import zhoutools.composeapp.generated.resources.start_time

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AddScheduleScene(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel = viewModel { AddScheduleViewModel() }
    val state by viewModel.uiState.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState()
    var text by remember(state.text) { mutableStateOf(state.text) }
    var timePickerState by remember {
        mutableStateOf(TimePickerState(0, 0, true))
    }
    var milestoneGoalStr by remember(state.milestoneGoalMillis) { mutableStateOf(state.milestoneGoalMillis.toDays().toString()) }

    LaunchedEffect(Unit) {
        viewModel.addScheduleEvent.collect { event ->
            when (event) {
                is AddScheduleEvent.GoBack -> navController.popBackStack()
            }
        }
    }

    EffectHelper.observeAddScheduleEffect {
        when (it) {
            is AddScheduleEffect.SetDate -> {
                viewModel.dispatch(AddScheduleAction.Reset)
                viewModel.dispatch(AddScheduleAction.SetDate(Triple(it.year, it.month, it.day)))
            }

            is AddScheduleEffect.BeginEdit -> {
                viewModel.dispatch(AddScheduleAction.BeginEdit(it.schedule))
            }
        }
    }

    BaseImmersiveScene(
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
            .background(AppColors.Background),
        navigationBarPadding = false
    ) {
        BottomSheetScaffold(
            sheetContent = {
                BottomSheetContent(timePickerState, scaffoldState, state, viewModel::dispatch)
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            scaffoldState = scaffoldState,
            sheetPeekHeight = 0.dp
        ) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                TitleBar(
                    navigator = navController,
                    title = stringResource(if (state.isEdit) Res.string.edit_schedule else Res.string.add_schedule)
                )

                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                    },
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        backgroundColor = Color.White
                    ),
                    minLines = 3,
                    maxLines = 3
                )

                SettingsLayout(
                    state,
                    viewModel::dispatch,
                    milestoneGoalStr,
                    onShowBottomSheet = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    },
                    onSetPickerTime = { hour, minute ->
                        timePickerState = TimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
                    },
                    onSetMilestoneGoalStr = {
                        milestoneGoalStr = it
                    }
                )

                Button(
                    onClick = {
                        if (milestoneGoalStr.toLongOrNull() == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar(getString(Res.string.invalid_number))
                            }
                            return@Button
                        }
                        viewModel.dispatch(AddScheduleAction.Confirm(text, milestoneGoalStr.daysToMillis()))
                    },
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    enabled = text.isNotEmpty()
                ) {
                    Text(
                        text = stringResource(Res.string.confirm).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsLayout(
    state: AddScheduleState,
    onAction: (AddScheduleAction) -> Unit,
    milestoneGoalStr: String,
    onShowBottomSheet: () -> Unit,
    onSetPickerTime: (hour: Int, minute: Int) -> Unit,
    onSetMilestoneGoalStr: (String) -> Unit
) {
    Column(modifier = Modifier
        .clickableNoRipple {
            hideSoftwareKeyboard()
        }
        .fillMaxWidth()
        .padding(all = 16.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.date),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = runBlocking { state.getDateString() },
                fontSize = 16.sp
            )
        }

        VerticalDivider()

        Row(
            modifier = Modifier
                .clickable {
                    hideSoftwareKeyboard()
                    onSetPickerTime(state.startTime.getHour(), state.startTime.getMinute())
                    onShowBottomSheet()
                    onAction(AddScheduleAction.SetTimeEditType(TimeEditType.START_TIME))
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.start_time),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = state.startTime.toHourMinString(),
                fontSize = 16.sp
            )
        }

        VerticalDivider()

        Row(
            modifier = Modifier
                .clickable {
                    hideSoftwareKeyboard()
                    onSetPickerTime(state.endTime.getHour(), state.endTime.getMinute())
                    onShowBottomSheet()
                    onAction(AddScheduleAction.SetTimeEditType(TimeEditType.END_TIME))
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.end_time),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = state.endTime.toHourMinString(),
                fontSize = 16.sp
            )
        }

        VerticalDivider()

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.all_day),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Switch(
                checked = state.isAllDay,
                onCheckedChange = {
                    onAction(AddScheduleAction.SetAllDay(it))
                },
                colors = SwitchDefaults.colors(checkedThumbColor = AppColors.Theme, checkedTrackColor = AppColors.LightTheme)
            )
        }

        VerticalDivider()

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.set_as_milestone),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Switch(
                checked = state.isMilestone,
                onCheckedChange = {
                    onAction(AddScheduleAction.SetMilestone(it))
                },
                colors = SwitchDefaults.colors(checkedThumbColor = AppColors.Theme, checkedTrackColor = AppColors.LightTheme)
            )
        }

        if (state.isMilestone) {
            VerticalDivider()

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.milestone_goal),
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier.alignByBaseline(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    BasicTextField(
                        value = milestoneGoalStr,
                        onValueChange = {
                            onSetMilestoneGoalStr(it)
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            textAlign = TextAlign.End,
                            color = if (milestoneGoalStr.toLongOrNull() != null) Color.Unspecified else AppColors.Red
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(AppColors.Theme)
                    )

                    if (milestoneGoalStr.isEmpty()) {
                        // Placeholder to make sure cursor is in right position
                        Text(
                            text = "0",
                            fontSize = 16.sp,
                            color = Color.Transparent,
                            modifier = Modifier.alpha(0f)
                        )
                    }
                }

                Text(
                    text = stringResource(Res.string.days),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .alignByBaseline()
                        .padding(start = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun ColumnScope.BottomSheetContent(
    timePickerState: TimePickerState,
    scaffoldState: BottomSheetScaffoldState,
    state: AddScheduleState,
    onAction: (AddScheduleAction) -> Unit
) {
    val scope = rememberCoroutineScope()

    if (scaffoldState.bottomSheetState.currentValue == BottomSheetValue.Expanded ||
        scaffoldState.bottomSheetState.targetValue == BottomSheetValue.Expanded) {
        TimePicker(
            state = timePickerState,
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally)
        )
    }

    Button(
        onClick = {
            scope.launch {
                if (state.timeEditType == TimeEditType.START_TIME) {
                    onAction(AddScheduleAction.SetStartTime(timePickerState.hour, timePickerState.minute))
                } else if (state.timeEditType == TimeEditType.END_TIME) {
                    onAction(AddScheduleAction.SetEndTime(timePickerState.hour, timePickerState.minute))
                }
                scaffoldState.bottomSheetState.collapse()
            }
        },
        modifier = Modifier
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        Text(
            text = stringResource(Res.string.save),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
    }
}
