package scene

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import global.AppColors
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.password
import zhoutools.composeapp.generated.resources.please_sign_in
import zhoutools.composeapp.generated.resources.username

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoginScene() {
    var inputUsername by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(AppColors.Background)) {
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
                label = {
                    Text(
                        text = stringResource(Res.string.password),
                        fontSize = 16.sp
                    )
                },
                visualTransformation = PasswordVisualTransformation()
            )
        }
    }
}