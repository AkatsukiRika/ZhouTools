package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import extension.firstCharToCapital
import global.AppColors
import constant.RouteConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import model.LoginRequest
import moe.tlaster.precompose.navigation.Navigator
import networkApi
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import store.AppStore
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.login
import zhoutools.composeapp.generated.resources.login_error_empty
import zhoutools.composeapp.generated.resources.password
import zhoutools.composeapp.generated.resources.please_sign_in
import zhoutools.composeapp.generated.resources.unknown_error
import zhoutools.composeapp.generated.resources.username

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoginScene(navigator: Navigator) {
    var inputUsername by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    fun login() {
        scope.launch(Dispatchers.IO) {
            if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
                snackbarHostState.showSnackbar(message = getString(Res.string.login_error_empty))
                return@launch
            }

            val request = LoginRequest(username = inputUsername, password = inputPassword)
            val response = networkApi.login(request)
            val isSuccess = response.first
            if (!isSuccess) {
                val errorMsg = response.second
                if (errorMsg != null) {
                    snackbarHostState.showSnackbar(message = errorMsg.firstCharToCapital())
                } else {
                    snackbarHostState.showSnackbar(getString(Res.string.unknown_error))
                }
            } else {
                val token = response.second
                if (token != null) {
                    AppStore.loginToken = token
                    AppStore.loginUsername = inputUsername
                    scope.launch(Dispatchers.Main) {
                        keyboardController?.hide()
                        navigator.navigate(RouteConstants.ROUTE_HOME)
                    }
                } else {
                    snackbarHostState.showSnackbar(getString(Res.string.unknown_error))
                }
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.please_sign_in).uppercase(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                TextField(
                    value = inputUsername,
                    onValueChange = {
                        inputUsername = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 32.dp),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    label = {
                        Text(
                            text = stringResource(Res.string.username),
                            fontSize = 16.sp
                        )
                    }
                )

                TextField(
                    value = inputPassword,
                    onValueChange = {
                        inputPassword = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    label = {
                        Text(
                            text = stringResource(Res.string.password),
                            fontSize = 16.sp
                        )
                    },
                    visualTransformation = PasswordVisualTransformation()
                )

                Button(
                    onClick = {
                        login()
                    },
                    modifier = Modifier.padding(top = 32.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.login),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}