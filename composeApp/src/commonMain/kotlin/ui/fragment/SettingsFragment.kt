package ui.fragment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import constant.RouteConstants
import extension.toHourMinString
import extension.toMonthDayString
import getAppVersion
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import store.AppStore
import ui.dialog.CloudServerDialog
import ui.dialog.ConfirmDialog
import ui.widget.VerticalDivider
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.export_data
import zhoutools.composeapp.generated.resources.ic_export
import zhoutools.composeapp.generated.resources.ic_logout
import zhoutools.composeapp.generated.resources.ic_server
import zhoutools.composeapp.generated.resources.ic_sync
import zhoutools.composeapp.generated.resources.last_sync_x
import zhoutools.composeapp.generated.resources.logout
import zhoutools.composeapp.generated.resources.logout_confirm_content
import zhoutools.composeapp.generated.resources.logout_confirm_title
import zhoutools.composeapp.generated.resources.pull
import zhoutools.composeapp.generated.resources.push
import zhoutools.composeapp.generated.resources.server_settings
import zhoutools.composeapp.generated.resources.sync_confirm_content
import zhoutools.composeapp.generated.resources.sync_confirm_title
import zhoutools.composeapp.generated.resources.sync_data
import zhoutools.composeapp.generated.resources.version_x

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SettingsFragment(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    showSnackbar: (String) -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }

    fun logout() {
        showLogoutDialog = false
        AppStore.loginToken = ""
        AppStore.loginUsername = ""
        AppStore.clearCache()
        navigator.navigate(
            route = RouteConstants.ROUTE_LOGIN,
            options = NavOptions(
                launchSingleTop = true,
                popUpTo = PopUpTo.First()
            )
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showLogoutDialog = true
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

        VerticalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showSyncDialog = true
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

        VerticalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    navigator.navigate(RouteConstants.ROUTE_EXPORT)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_export),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 13.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                    .size(26.dp),
                tint = Color.Unspecified
            )

            Text(
                text = stringResource(Res.string.export_data),
                fontSize = 16.sp
            )
        }

        VerticalDivider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showServerDialog = true
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_server),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 13.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
                    .size(26.dp),
                tint = Color.Unspecified
            )

            Text(
                text = stringResource(Res.string.server_settings),
                fontSize = 16.sp
            )
        }

        VerticalDivider()

        Text(
            text = stringResource(Res.string.version_x, getAppVersion()),
            color = Color.Black.copy(alpha = 0.5f),
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 48.dp)
        )
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
                navigator.navigate(RouteConstants.ROUTE_SYNC.replace(RouteConstants.PARAM_MODE, "pull"))
            },
            onConfirm = {
                showSyncDialog = false
                navigator.navigate(RouteConstants.ROUTE_SYNC.replace(RouteConstants.PARAM_MODE, "push"))
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
            }
        )
    }
}