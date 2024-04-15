package fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import api.NetworkApi
import constant.RouteConstants
import extension.firstCharToCapital
import global.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import store.AppStore
import util.TimeCardUtil
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.ic_logout
import zhoutools.composeapp.generated.resources.ic_sync
import zhoutools.composeapp.generated.resources.in_progress
import zhoutools.composeapp.generated.resources.logout
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
    val networkApi = remember { NetworkApi() }

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
        AppStore.loginToken = ""
        AppStore.loginUsername = ""
        navigator.navigate(
            route = RouteConstants.ROUTE_LOGIN,
            options = NavOptions(
                launchSingleTop = true,
                popUpTo = PopUpTo.First()
            )
        )
    }

    fun sync() {
        scope.launch(Dispatchers.IO) {
            inProgress = true
            val request = TimeCardUtil.buildSyncRequest()
            if (request == null) {
                showSnackbar(getString(Res.string.sync_failed))
                inProgress = false
                return@launch
            }
            val response = networkApi.sync(AppStore.loginToken, request)
            if (!response.first) {
                showSnackbar(response.second?.firstCharToCapital() ?: getString(Res.string.sync_failed))
                inProgress = false
                return@launch
            }
            AppStore.lastSync = Clock.System.now().toEpochMilliseconds()
            showSnackbar(getString(Res.string.sync_success))
            inProgress = false
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
                    logout()
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

        Divider()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    sync()
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

        Divider()
    }
}

@Composable
private fun Divider() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(AppColors.Divider)
    )
}