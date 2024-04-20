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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import constant.RouteConstants
import extension.firstCharToCapital
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import logger
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo
import networkApi
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import store.AppStore
import ui.dialog.ConfirmDialog
import ui.widget.VerticalDivider
import util.TimeCardUtil
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.ic_logout
import zhoutools.composeapp.generated.resources.ic_sync
import zhoutools.composeapp.generated.resources.in_progress
import zhoutools.composeapp.generated.resources.logout
import zhoutools.composeapp.generated.resources.logout_confirm_content
import zhoutools.composeapp.generated.resources.logout_confirm_title
import zhoutools.composeapp.generated.resources.pull
import zhoutools.composeapp.generated.resources.pull_failed
import zhoutools.composeapp.generated.resources.pull_success
import zhoutools.composeapp.generated.resources.push
import zhoutools.composeapp.generated.resources.sync_confirm_content
import zhoutools.composeapp.generated.resources.sync_confirm_title
import zhoutools.composeapp.generated.resources.sync_data
import zhoutools.composeapp.generated.resources.sync_failed
import zhoutools.composeapp.generated.resources.sync_success

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SettingsFragment(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    showSnackbar: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var inProgress by remember { mutableStateOf(false) }
    var dots by remember { mutableStateOf("...") }
    var job by remember { mutableStateOf<Job?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSyncDialog by remember { mutableStateOf(false) }

    LaunchedEffect(inProgress) {
        if (inProgress) {
            job = launch {
                while (true) {
                    delay(200)
                    dots = when (dots) {
                        "." -> ".."
                        ".." -> "..."
                        else -> "."
                    }
                }
            }
        } else {
            job?.cancel()
        }
    }

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

    fun push() {
        scope.launch(Dispatchers.IO) {
            inProgress = true
            val request = TimeCardUtil.buildSyncRequest()
            if (request == null) {
                showSnackbar(getString(Res.string.sync_failed))
                inProgress = false
                showSyncDialog = false
                return@launch
            }
            val response = networkApi.sync(AppStore.loginToken, request)
            if (!response.first) {
                showSnackbar(response.second?.firstCharToCapital() ?: getString(Res.string.sync_failed))
                inProgress = false
                showSyncDialog = false
                return@launch
            }
            AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
            showSnackbar(getString(Res.string.sync_success))
            inProgress = false
            showSyncDialog = false
        }
    }

    fun pull() {
        scope.launch(Dispatchers.IO) {
            inProgress = true
            if (AppStore.loginToken.isNotBlank() && AppStore.loginUsername.isNotBlank()) {
                val serverData = networkApi.getServerTimeCards(AppStore.loginToken, AppStore.loginUsername)
                if (serverData == null) {
                    // failed
                    showSnackbar(getString(Res.string.pull_failed))
                    inProgress = false
                    showSyncDialog = false
                    return@launch
                }
                AppStore.timeCards = Json.encodeToString(serverData)
                logger.i { "pull success: ${AppStore.timeCards}" }
                // success
                showSnackbar(getString(Res.string.pull_success))
                inProgress = false
                showSyncDialog = false
                TimeCardEventFlow.emit(TimeCardEvent.RefreshTodayState)
            } else {
                // failed
                showSnackbar(getString(Res.string.pull_failed))
                inProgress = false
                showSyncDialog = false
            }
        }
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
                text = stringResource(Res.string.logout),
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

            if (inProgress) {
                Text(
                    text = "(${stringResource(Res.string.in_progress)}$dots)",
                    color = Color.Black.copy(alpha = 0.5f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        VerticalDivider()
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
                pull()
            },
            onConfirm = {
                push()
            },
            onDismiss = {
                showSyncDialog = false
            }
        )
    }
}