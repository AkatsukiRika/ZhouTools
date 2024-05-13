package ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import extension.isValidUrl
import global.AppColors
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import store.AppStore
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.cancel
import zhoutools.composeapp.generated.resources.confirm
import zhoutools.composeapp.generated.resources.server_settings
import zhoutools.composeapp.generated.resources.server_settings_hint

@OptIn(ExperimentalResourceApi::class)
@Composable
fun CloudServerDialog(onCancel: () -> Unit, onConfirm: (String) -> Unit) {
    val density = LocalDensity.current
    var inputUrl by remember { mutableStateOf(AppStore.customServerUrl) }
    val isValidUrl by remember(inputUrl) {
        derivedStateOf { inputUrl.isEmpty() || inputUrl.isValidUrl() }
    }
    val imeHeight = WindowInsets.ime.getBottom(density)
    val imeHeightDp = imeHeight / density.density

    Dialog(onDismissRequest = {}) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = imeHeightDp.dp)
            .background(Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            Row(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(Res.string.server_settings),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.weight(1f))
            }

            Text(
                text = stringResource(Res.string.server_settings_hint),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )

            TextField(
                value = inputUrl,
                onValueChange = {
                    inputUrl = it
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    textColor = if (isValidUrl) AppColors.DarkGreen else AppColors.Red
                )
            )

            Row(
                modifier = Modifier
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(Res.string.cancel).uppercase(),
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        onConfirm(inputUrl)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = stringResource(Res.string.confirm).uppercase(),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}