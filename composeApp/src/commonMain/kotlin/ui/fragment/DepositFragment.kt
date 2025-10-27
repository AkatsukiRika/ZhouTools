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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import constant.RouteConstants
import extension.clickableNoRipple
import extension.monthStartTime
import extension.toMoneyDisplayStr
import extension.toMonthYearString
import global.AppColors
import helper.SyncHelper
import helper.effect.DepositEffect
import helper.effect.EffectHelper
import hideSoftwareKeyboard
import kotlinx.coroutines.launch
import model.records.DepositMonth
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.dialog.ConfirmDialog
import ui.widget.FragmentHeader
import ui.widget.ShimmerProgressBar
import ui.widget.VerticalDivider
import util.TimeUtil
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.add_monthly_record
import zhoutools.composeapp.generated.resources.balance
import zhoutools.composeapp.generated.resources.balance_diff
import zhoutools.composeapp.generated.resources.confirm
import zhoutools.composeapp.generated.resources.current_deposit
import zhoutools.composeapp.generated.resources.current_deposit_amount
import zhoutools.composeapp.generated.resources.date
import zhoutools.composeapp.generated.resources.delete_confirm_content
import zhoutools.composeapp.generated.resources.delete_confirm_title
import zhoutools.composeapp.generated.resources.deposit
import zhoutools.composeapp.generated.resources.extra_deposit
import zhoutools.composeapp.generated.resources.ic_add
import zhoutools.composeapp.generated.resources.ic_details
import zhoutools.composeapp.generated.resources.invalid_deposit_toast
import zhoutools.composeapp.generated.resources.monthly_income
import zhoutools.composeapp.generated.resources.records
import kotlin.math.roundToLong
import androidx.navigation.NavHostController
import store.AppStore
import ui.dialog.SetValueDialog
import ui.widget.InsetAwareSnackbarHost
import zhoutools.composeapp.generated.resources.current_balance
import zhoutools.composeapp.generated.resources.ic_calc
import zhoutools.composeapp.generated.resources.invalid_number
import zhoutools.composeapp.generated.resources.monthly_income_remain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepositFragment(navController: NavHostController) {
    val scope = rememberCoroutineScope()
    val viewModel = viewModel { DepositViewModel() }
    val state by viewModel.uiState.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(skipHiddenState = false)
    )
    val snackbarHostState = remember { SnackbarHostState() }
    var deleteDialogRecord by remember { mutableStateOf<DepositDisplayRecord?>(null) }
    var showCurrentBalanceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(scaffoldState.bottomSheetState.currentValue) {
        if (scaffoldState.bottomSheetState.currentValue != SheetValue.Expanded) {
            hideSoftwareKeyboard()
        }
    }

    LaunchedEffect(Unit) {
        SyncHelper.autoPullDeposit(onSuccess = {
            viewModel.dispatch(DepositAction.RefreshData)
        })
    }

    EffectHelper.observeDepositEffect {
        when (it) {
            is DepositEffect.RefreshData -> {
                viewModel.dispatch(DepositAction.RefreshData)
            }
        }
    }

    BottomSheetScaffold(
        sheetContent = {
            BottomSheetContent(
                onConfirm = {
                    viewModel.dispatch(DepositAction.AddMonth(it))
                    scope.launch {
                        scaffoldState.bottomSheetState.hide()
                    }
                },
                onShowSnackbar = {
                    scope.launch {
                        snackbarHostState.showSnackbar(it)
                    }
                }
            )
        },
        snackbarHost = {
            InsetAwareSnackbarHost(snackbarHostState)
        },
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        containerColor = AppColors.Background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FragmentHeader(title = stringResource(Res.string.deposit)) {
                Icon(
                    painter = painterResource(Res.drawable.ic_details),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable {
                            navController.navigate(RouteConstants.ROUTE_DEPOSIT_STATS)
                        }
                )
            }

            BigCard(state, onClick = {
                viewModel.dispatch(DepositAction.ToggleBigCardState)
            })

            Row(
                modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 24.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.records),
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    painter = painterResource(Res.drawable.ic_calc),
                    contentDescription = null,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable {
                            showCurrentBalanceDialog = true
                        }
                        .padding(4.dp),
                    tint = AppColors.Theme
                )
            }

            LazyColumn(modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ) {
                items(state.displayDeque) { item ->
                    MonthCard(item, onClick = {
                        deleteDialogRecord = item
                    })
                }
            }

            AddRecordButton(onClick = {
                scope.launch {
                    scaffoldState.bottomSheetState.expand()
                }
            })

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (deleteDialogRecord != null) {
        ConfirmDialog(
            title = stringResource(Res.string.delete_confirm_title),
            content = stringResource(Res.string.delete_confirm_content, deleteDialogRecord?.monthStr ?: ""),
            onCancel = {
                deleteDialogRecord = null
            },
            onConfirm = {
                deleteDialogRecord?.toDepositMonth()?.let {
                    viewModel.dispatch(DepositAction.RemoveMonth(it))
                }
                deleteDialogRecord = null
            }
        )
    }

    if (showCurrentBalanceDialog) {
        SetValueDialog(
            initialValue = AppStore.currentBalance,
            valueName = stringResource(Res.string.current_balance),
            onCancel = {
                showCurrentBalanceDialog = false
            },
            onConfirm = {
                showCurrentBalanceDialog = false
                if (it == null) {
                    scope.launch {
                        snackbarHostState.showSnackbar(getString(Res.string.invalid_number))
                    }
                } else {
                    AppStore.isCurrentBalanceSet = true
                    AppStore.currentBalance = it
                    viewModel.dispatch(DepositAction.ResetBigCardState)
                }
            }
        )
    }
}

@Composable
private fun BigCard(state: DepositState, onClick: () -> Unit) {
    var annotatedAmountString by remember { mutableStateOf(AnnotatedString("")) }

    LaunchedEffect(state.bigCardState, state.currentAmount, state.monthlyIncomeRemain) {
        annotatedAmountString = buildAnnotatedString {
            val splitResult = if (state.bigCardState == DepositBigCardState.REMAIN) {
                state.monthlyIncomeRemain.toMoneyDisplayStr().split(".")
            } else {
                state.currentAmount.toMoneyDisplayStr().split(".")
            }

            if (splitResult.size == 2) {
                withStyle(SpanStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold)) {
                    append(splitResult[0])
                }
                withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)) {
                    append("." + splitResult[1])
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            },
        backgroundColor = Color.White,
        elevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            val density = LocalDensity.current
            val toDp = { px: Float ->
                density.run { px.toDp() }
            }
            var height by remember { mutableIntStateOf(0) }

            ShimmerProgressBar(
                progress = if (state.bigCardState == DepositBigCardState.REMAIN) {
                    state.monthlyIncomeRemainProgress
                } else {
                    state.progress
                },
                color = if (state.bigCardState == DepositBigCardState.REMAIN) {
                    AppColors.LightTheme.copy(alpha = 0.5f)
                } else {
                    AppColors.LightGold.copy(alpha = 0.5f)
                },
                backgroundColor = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(toDp(height.toFloat()))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged {
                        height = it.height
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = stringResource(
                    if (state.bigCardState == DepositBigCardState.REMAIN) {
                        Res.string.monthly_income_remain
                    } else {
                        Res.string.current_deposit_amount
                    }
                ))

                Text(annotatedAmountString)

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun MonthCard(item: DepositDisplayRecord, onClick: (() -> Unit)? = null) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        .clip(RoundedCornerShape(8.dp))
        .clickable {
            onClick?.invoke()
        }
        .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.monthStr,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        MonthCardDataItem(
            type = Res.string.current_deposit,
            value = item.currentAmount.toMoneyDisplayStr()
        )

        MonthCardDataItem(
            type = Res.string.monthly_income,
            value = item.monthlyIncome.toMoneyDisplayStr()
        )

        MonthCardDataItem(
            type = Res.string.balance,
            value = item.balance.toMoneyDisplayStr()
        )

        MonthCardDataItem(
            type = Res.string.extra_deposit,
            value = item.extraDeposit.toMoneyDisplayStr()
        )

        val balanceDiff = item.balanceDiff
        val balanceDiffStr = if (balanceDiff == null) {
            "N/A"
        } else if (balanceDiff > 0) {
            "+${balanceDiff.toMoneyDisplayStr()}"
        } else {
            balanceDiff.toMoneyDisplayStr()
        }
        val balanceDiffColor = if (balanceDiff != null && balanceDiff < 0) {
            AppColors.Red
        } else AppColors.DarkGreen
        MonthCardDataItem(
            type = Res.string.balance_diff,
            value = balanceDiffStr,
            valueColor = balanceDiffColor
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MonthCardDataItem(type: StringResource, value: String, valueColor: Color? = null) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
    ) {
        Text(
            text = stringResource(type),
            color = Color.DarkGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = valueColor ?: Color.Unspecified
        )
    }
}

@Composable
private fun AddRecordButton(onClick: () -> Unit) {
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
            text = stringResource(Res.string.add_monthly_record),
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomSheetContent(onConfirm: (DepositMonth) -> Unit, onShowSnackbar: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var isPickingDate by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var dateStr by remember { mutableStateOf(TimeUtil.currentTimeMillis().toMonthYearString()) }
    var currentDepositStr by remember { mutableStateOf("0.00") }
    var monthlyIncomeStr by remember { mutableStateOf("0.00") }
    var extraDepositStr by remember { mutableStateOf("0.00") }

    Column(modifier = Modifier
        .fillMaxWidth()
        .clickableNoRipple {
            hideSoftwareKeyboard()
        }
    ) {
        if (isPickingDate) {
            BottomSheetDatePicker(datePickerState = datePickerState)
        } else {
            BottomSheetMainColumn(
                dateStr = dateStr,
                currentDepositStr = currentDepositStr,
                monthlyIncomeStr = monthlyIncomeStr,
                extraDepositStr = extraDepositStr,
                onCurrentDepositStrChange = {
                    currentDepositStr = it
                },
                onMonthlyIncomeStrChange = {
                    monthlyIncomeStr = it
                },
                onExtraDepositStrChange = {
                    extraDepositStr = it
                },
                onDateRowClick = {
                    isPickingDate = true
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isPickingDate) {
                    val selectedDateMillis = datePickerState.selectedDateMillis
                    if (selectedDateMillis != null) {
                        dateStr = selectedDateMillis.toMonthYearString()
                    }
                    isPickingDate = false
                } else {
                    val isValidInput = currentDepositStr.toDoubleOrNull() != null
                            && monthlyIncomeStr.toDoubleOrNull() != null
                            && extraDepositStr.toDoubleOrNull() != null
                    if (isValidInput) {
                        val selectedDateMillis = datePickerState.selectedDateMillis ?: TimeUtil.currentTimeMillis()
                        val month = DepositMonth(
                            monthStartTime = selectedDateMillis.monthStartTime(),
                            currentAmount = (currentDepositStr.toDouble() * 100).roundToLong(),
                            monthlyIncome = (monthlyIncomeStr.toDouble() * 100).roundToLong(),
                            extraDeposit = (extraDepositStr.toDouble() * 100).roundToLong()
                        )
                        onConfirm(month)
                    } else {
                        scope.launch {
                            onShowSnackbar(getString(Res.string.invalid_deposit_toast))
                        }
                    }
                }
            },
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            Text(
                text = stringResource(Res.string.confirm).uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun BottomSheetMainColumn(
    modifier: Modifier = Modifier,
    dateStr: String,
    currentDepositStr: String,
    monthlyIncomeStr: String,
    extraDepositStr: String,
    onCurrentDepositStrChange: ((String) -> Unit)? = null,
    onMonthlyIncomeStrChange: ((String) -> Unit)? = null,
    onExtraDepositStrChange: ((String) -> Unit)? = null,
    onDateRowClick: (() -> Unit)? = null
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .clickable {
                    onDateRowClick?.invoke()
                }
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
                text = dateStr,
                fontSize = 16.sp
            )
        }

        VerticalDivider()

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.current_deposit),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            BasicTextField(
                value = currentDepositStr,
                onValueChange = {
                    onCurrentDepositStrChange?.invoke(it)
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    textAlign = TextAlign.End,
                    color = if (currentDepositStr.toDoubleOrNull() != null) Color.Unspecified else AppColors.Red
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        VerticalDivider()

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.monthly_income),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            BasicTextField(
                value = monthlyIncomeStr,
                onValueChange = {
                    onMonthlyIncomeStrChange?.invoke(it)
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    textAlign = TextAlign.End,
                    color = if (monthlyIncomeStr.toDoubleOrNull() != null) Color.Unspecified else AppColors.Red
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        VerticalDivider()

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.extra_deposit),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            BasicTextField(
                value = extraDepositStr,
                onValueChange = {
                    onExtraDepositStrChange?.invoke(it)
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    textAlign = TextAlign.End,
                    color = if (extraDepositStr.toDoubleOrNull() != null) Color.Unspecified else AppColors.Red
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetDatePicker(modifier: Modifier = Modifier, datePickerState: DatePickerState) {
    DatePicker(
        state = datePickerState,
        modifier = modifier
    )
}