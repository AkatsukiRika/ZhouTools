package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import extension.isValidEmail
import global.AppColors
import helper.NetworkHelper
import hideSoftwareKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.request.RegisterRequest
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import ui.widget.BaseImmersiveScene
import ui.widget.TitleBar
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.email
import zhoutools.composeapp.generated.resources.invalid_email_toast
import zhoutools.composeapp.generated.resources.login_error_empty
import zhoutools.composeapp.generated.resources.password
import zhoutools.composeapp.generated.resources.registration
import zhoutools.composeapp.generated.resources.registration_failed
import zhoutools.composeapp.generated.resources.registration_success
import zhoutools.composeapp.generated.resources.sign_up
import zhoutools.composeapp.generated.resources.username

@Composable
fun SignUpScene(navigator: Navigator) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var inputUsername by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var requestInProgress by remember { mutableStateOf(false) }

    fun showInvalidEmailToast() {
        scope.launch {
            snackbarHostState.showSnackbar(getString(Res.string.invalid_email_toast))
        }
    }

    fun showEmptyToast() {
        scope.launch {
            snackbarHostState.showSnackbar(getString(Res.string.login_error_empty))
        }
    }

    fun showFailToast() {
        scope.launch {
            snackbarHostState.showSnackbar(getString(Res.string.registration_failed))
        }
    }

    fun showSuccessToast() {
        scope.launch {
            snackbarHostState.showSnackbar(getString(Res.string.registration_success))
        }
    }

    suspend fun delayGoBack() {
        delay(1500)
        navigator.goBack()
    }

    fun signUp() {
        hideSoftwareKeyboard()
        if (requestInProgress) {
            return
        }
        if (!inputEmail.isValidEmail()) {
            showInvalidEmailToast()
            return
        }
        if (inputUsername.isBlank() || inputPassword.isBlank()) {
            showEmptyToast()
            return
        }
        val request = RegisterRequest(
            username = inputUsername,
            email = inputEmail,
            password = inputPassword
        )
        scope.launch(Dispatchers.IO) {
            requestInProgress = true
            val errorMsg = NetworkHelper.register(request)
            if (errorMsg != null) {
                showFailToast()
                requestInProgress = false
            } else {
                showSuccessToast()
                delayGoBack()
            }
        }
    }

    BaseImmersiveScene(modifier = Modifier
        .imePadding()
        .fillMaxSize()
        .background(AppColors.Background)
    ) {
        Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBar(
                    navigator = navigator,
                    title = stringResource(Res.string.registration)
                )

                Spacer(modifier = Modifier.weight(1f))

                TextField(
                    value = inputUsername,
                    onValueChange = {
                        inputUsername = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
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
                    value = inputEmail,
                    onValueChange = {
                        inputEmail = it
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    isError = !inputEmail.isValidEmail(),
                    label = {
                        Text(
                            text = stringResource(Res.string.email),
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
                    onClick = ::signUp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 32.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.sign_up),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}