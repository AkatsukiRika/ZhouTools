package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helper.effect.WriteMemoEffect
import global.AppColors
import helper.effect.EffectHelper
import isIOS
import moe.tlaster.precompose.molecule.rememberPresenter
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.stringResource
import setNavigationBarColor
import setStatusBarColor
import ui.widget.TitleBar
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.confirm
import zhoutools.composeapp.generated.resources.delete
import zhoutools.composeapp.generated.resources.edit_memo
import zhoutools.composeapp.generated.resources.pin_to_top
import zhoutools.composeapp.generated.resources.set_as_todo
import zhoutools.composeapp.generated.resources.write_memo

@Composable
fun WriteMemoScene(navigator: Navigator, isEdit: Boolean) {
    val (state, channel) = rememberPresenter { WriteMemoPresenter(it) }
    var text by remember(state.text) { mutableStateOf(state.text) }

    LaunchedEffect(Unit) {
        setStatusBarColor("#FFFFFF", isLight = true)
        setNavigationBarColor("#F4F4F4", isLight = true)
    }

    EffectHelper.observeWriteMemoEffect {
        when (it) {
            is WriteMemoEffect.BeginEdit -> {
                if (isEdit) {
                    channel.trySend(WriteMemoAction.BeginEdit(editMemo = it.memo))
                }
            }
        }
    }

    var rootModifier = Modifier
        .imePadding()
        .fillMaxSize()
        .background(AppColors.Background)
    if (isIOS()) {
        rootModifier = rootModifier.navigationBarsPadding()
    }
    Column(modifier = rootModifier) {
        TitleBar(
            navigator = navigator,
            title = stringResource(
                if (isEdit) Res.string.edit_memo else Res.string.write_memo
            )
        )

        TextField(
            value = text,
            onValueChange = {
                text = it
            },
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(12.dp)),
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                backgroundColor = Color.White
            )
        )

        SettingsLayout(
            state.isTodo,
            { channel.trySend(WriteMemoAction.SetTodo(it)) },
            state.isPin,
            { channel.trySend(WriteMemoAction.SetPin(it)) }
        )

        if (isEdit) {
            OutlinedButton(
                onClick = {
                    channel.trySend(WriteMemoAction.Delete(navigator))
                },
                modifier = Modifier
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = text.isNotEmpty()
            ) {
                Text(
                    text = stringResource(Res.string.delete).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
        }

        Button(
            onClick = {
                channel.trySend(WriteMemoAction.Confirm(text, navigator))
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

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SettingsLayout(
    todo: Boolean,
    setTodo: (Boolean) -> Unit,
    pin: Boolean,
    setPin: (Boolean) -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(all = 16.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.set_as_todo),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Switch(
                checked = todo,
                onCheckedChange = setTodo,
                colors = SwitchDefaults.colors(checkedThumbColor = AppColors.Theme, checkedTrackColor = AppColors.LightTheme)
            )
        }

        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.pin_to_top),
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Switch(
                checked = pin,
                onCheckedChange = setPin,
                colors = SwitchDefaults.colors(checkedThumbColor = AppColors.Theme, checkedTrackColor = AppColors.LightTheme)
            )
        }
    }
}