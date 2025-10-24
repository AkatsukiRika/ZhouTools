package ui.fragment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import constant.RouteConstants
import extension.isValidUrl
import extension.toHourMinString
import extension.toMonthDayString
import getAppVersion
import global.AppColors
import helper.SyncHelper
import helper.WorkHoursHelper
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import store.AppFlowStore
import store.AppStore
import ui.dialog.CloudServerDialog
import ui.dialog.ConfirmDialog
import ui.dialog.SetValueDialog
import ui.widget.HorizontalSeekBar
import ui.widget.VerticalDivider
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.auto_sync
import zhoutools.composeapp.generated.resources.confirm
import zhoutools.composeapp.generated.resources.deposit_goal
import zhoutools.composeapp.generated.resources.export_data
import zhoutools.composeapp.generated.resources.ic_auto
import zhoutools.composeapp.generated.resources.ic_deposit_goal
import zhoutools.composeapp.generated.resources.ic_export
import zhoutools.composeapp.generated.resources.ic_logout
import zhoutools.composeapp.generated.resources.ic_server
import zhoutools.composeapp.generated.resources.ic_sync
import zhoutools.composeapp.generated.resources.ic_time_card
import zhoutools.composeapp.generated.resources.invalid_number
import zhoutools.composeapp.generated.resources.invalid_url_toast
import zhoutools.composeapp.generated.resources.last_sync_x
import zhoutools.composeapp.generated.resources.logout
import zhoutools.composeapp.generated.resources.logout_confirm_content
import zhoutools.composeapp.generated.resources.logout_confirm_title
import zhoutools.composeapp.generated.resources.min_overtime_hours
import zhoutools.composeapp.generated.resources.min_working_hours
import zhoutools.composeapp.generated.resources.pull
import zhoutools.composeapp.generated.resources.push
import zhoutools.composeapp.generated.resources.server_settings
import zhoutools.composeapp.generated.resources.sync_confirm_content
import zhoutools.composeapp.generated.resources.sync_confirm_title
import zhoutools.composeapp.generated.resources.sync_data
import zhoutools.composeapp.generated.resources.time_card_settings
import zhoutools.composeapp.generated.resources.total_deposit_goal
import zhoutools.composeapp.generated.resources.version_x

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsFragment(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    showSnackbar: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(skipHiddenState = false)
    )
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var showDepositGoalDialog by remember { mutableStateOf(false) }

    fun logout() {
        showLogoutDialog = false
        AppStore.loginToken = ""
        AppStore.loginUsername = ""
        AppStore.loginPassword = ""
        AppStore.clearCache()
        SyncHelper.clearPulledFlags()
        navController.navigate(RouteConstants.ROUTE_LOGIN) {
            launchSingleTop = true
            popUpTo(navController.graph.startDestinationRoute!!) {
                inclusive = true
            }
        }
    }

    BottomSheetScaffold(
        sheetContent = {
            TimeCardBottomSheet(onConfirm = {
                scope.launch {
                    scaffoldState.bottomSheetState.hide()
                }
            })
        },
        modifier = modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        containerColor = AppColors.Background,
        sheetShadowElevation = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogOutItem {
                showLogoutDialog = true
            }

            VerticalDivider()

            SettingItem(
                icon = Res.drawable.ic_time_card,
                name = Res.string.time_card_settings
            ) {
                scope.launch {
                    scaffoldState.bottomSheetState.expand()
                }
            }

            VerticalDivider()

            SyncItem {
                showSyncDialog = true
            }

            VerticalDivider()

            SettingItem(
                icon = Res.drawable.ic_export,
                name = Res.string.export_data
            ) {
                navController.navigate(RouteConstants.ROUTE_EXPORT)
            }

            VerticalDivider()

            SettingItem(
                icon = Res.drawable.ic_deposit_goal,
                name = Res.string.deposit_goal
            ) {
                showDepositGoalDialog = true
            }

            VerticalDivider()

            SettingItem(
                icon = Res.drawable.ic_server,
                name = Res.string.server_settings
            ) {
                showServerDialog = true
            }

            VerticalDivider()

            AutoSyncItem()

            VerticalDivider()

            Text(
                text = stringResource(Res.string.version_x, getAppVersion()),
                color = Color.Black.copy(alpha = 0.5f),
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 48.dp)
            )
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = stringResource(Res.string.logout_confirm_title),
            content = stringResource(Res.string.logout_confirm_content, AppStore.loginUsername),
            onCancel = {
                showLogoutDialog = false
            },
            onConfirm = {
                logout()
            }
        )
    }

    if (showSyncDialog) {
        ConfirmDialog(
            title = stringResource(Res.string.sync_confirm_title),
            content = stringResource(Res.string.sync_confirm_content),
            cancel = stringResource(Res.string.pull),
            confirm = stringResource(Res.string.push),
            onCancel = {
                showSyncDialog = false
                navController.navigate(RouteConstants.ROUTE_SYNC.replace(RouteConstants.PARAM_MODE, "pull"))
            },
            onConfirm = {
                showSyncDialog = false
                navController.navigate(RouteConstants.ROUTE_SYNC.replace(RouteConstants.PARAM_MODE, "push"))
            },
            onDismiss = {
                showSyncDialog = false
            }
        )
    }

    if (showServerDialog) {
        CloudServerDialog(
            onCancel = {
                showServerDialog = false
            },
            onConfirm = {
                showServerDialog = false
                if (it.isNotEmpty()) {
                    if (it.isValidUrl().not()) {
                        scope.launch {
                            showSnackbar(getString(Res.string.invalid_url_toast))
                        }
                    } else {
                        AppStore.customServerUrl = it
                    }
                } else {
                    AppStore.customServerUrl = ""
                }
            }
        )
    }

    if (showDepositGoalDialog) {
        SetValueDialog(
            initialValue = AppStore.totalDepositGoal,
            valueName = stringResource(Res.string.total_deposit_goal),
            onCancel = {
                showDepositGoalDialog = false
            },
            onConfirm = {
                showDepositGoalDialog = false
                if (it == null) {
                    scope.launch {
                        showSnackbar(getString(Res.string.invalid_number))
                    }
                } else {
                    AppStore.setTotalDepositGoalWithFlow(it)
                }
            }
        )
    }
}

@Composable
private fun LogOutItem(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_logout),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 13.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                .size(26.dp),
            tint = Color.Unspecified
        )

        Text(
            text = stringResource(Res.string.logout) + ": ${AppStore.loginUsername}",
            fontSize = 16.sp
        )
    }
}

@Composable
private fun SyncItem(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_sync),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 13.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                .size(26.dp),
            tint = Color.Unspecified
        )

        Text(
            text = stringResource(Res.string.sync_data),
            fontSize = 16.sp
        )

        if (AppStore.lastSync != 0L) {
            val text = "(${stringResource(Res.string.last_sync_x, "${AppStore.lastSync.toMonthDayString()} ${AppStore.lastSync.toHourMinString()}")})"

            Text(
                text = text,
                color = Color.Black.copy(alpha = 0.5f),
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun AutoSyncItem() {
    val isAutoSync = AppFlowStore.autoSyncFlow.collectAsStateWithLifecycle(initialValue = false).value
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_auto),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 13.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                .size(26.dp),
            tint = Color.Unspecified
        )

        Text(
            text = stringResource(Res.string.auto_sync),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Switch(
            checked = isAutoSync,
            onCheckedChange = {
                AppFlowStore.autoSyncFlow.emitIn(scope, it)
            },
            colors = SwitchDefaults.colors(checkedThumbColor = AppColors.Theme, checkedTrackColor = AppColors.LightTheme),
            modifier = Modifier.padding(end = 12.dp)
        )
    }
}

@Composable
private fun SettingItem(icon: DrawableResource, name: StringResource, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 13.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                .size(26.dp),
            tint = Color.Unspecified
        )

        Text(
            text = stringResource(name),
            fontSize = 16.sp
        )
    }
}

@Composable
private fun TimeCardBottomSheet(onConfirm: () -> Unit) {
    var minWorkingHours by remember { mutableFloatStateOf(AppStore.minWorkingHours) }
    var minOvertimeHours by remember { mutableFloatStateOf(AppStore.minOvertimeHours) }

    Column {
        Text(
            text = stringResource(Res.string.min_working_hours),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )

        HorizontalSeekBar(
            itemList = WorkHoursHelper.workingHoursMap.keys.toList(),
            itemWidth = 58.dp,
            modifier = Modifier.padding(bottom = 12.dp, top = 8.dp),
            defaultSelectItem = WorkHoursHelper.getWorkingHourString(AppStore.minWorkingHours)
        ) {
            WorkHoursHelper.workingHoursMap[it]?.let {
                minWorkingHours = it
            }
        }

        Text(
            text = stringResource(Res.string.min_overtime_hours),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )

        HorizontalSeekBar(
            itemList = WorkHoursHelper.overtimeHoursMap.keys.toList(),
            itemWidth = 58.dp,
            modifier = Modifier.padding(bottom = 12.dp, top = 8.dp),
            defaultSelectItem = WorkHoursHelper.getOvertimeHourString(AppStore.minOvertimeHours)
        ) {
            WorkHoursHelper.overtimeHoursMap[it]?.let {
                minOvertimeHours = it
            }
        }

        Button(
            onClick = {
                AppStore.setMinWorkingHoursWithFlow(minWorkingHours)
                AppStore.setMinOvertimeHoursWithFlow(minOvertimeHours)
                onConfirm()
            },
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp, bottom = 16.dp, top = 8.dp)
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            Text(
                text = stringResource(Res.string.confirm).uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        }
    }
}