package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import helper.effect.WriteMemoEffect
import global.AppColors
import helper.effect.EffectHelper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import moe.tlaster.precompose.molecule.rememberPresenter
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.widget.BaseImmersiveScene
import ui.widget.TitleBar
import ui.widget.VerticalDivider
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.add_group
import zhoutools.composeapp.generated.resources.confirm
import zhoutools.composeapp.generated.resources.delete
import zhoutools.composeapp.generated.resources.edit_memo
import zhoutools.composeapp.generated.resources.group
import zhoutools.composeapp.generated.resources.ic_add
import zhoutools.composeapp.generated.resources.pin_to_top
import zhoutools.composeapp.generated.resources.set_as_todo
import zhoutools.composeapp.generated.resources.unsorted
import zhoutools.composeapp.generated.resources.write_memo

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WriteMemoScene(navigator: Navigator, isEdit: Boolean) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val (state, channel) = rememberPresenter { WriteMemoPresenter(it) }

    EffectHelper.observeWriteMemoEffect {
        when (it) {
            is WriteMemoEffect.BeginEdit -> {
                if (isEdit) {
                    channel.trySend(WriteMemoAction.BeginEdit(editMemo = it.memo))
                }
            }
        }
    }

    BaseImmersiveScene(
        modifier = Modifier
            .imePadding()
            .fillMaxSize()
            .background(AppColors.Background),
        navigationBarPadding = false
    ) {
        BottomSheetScaffold(
            sheetContent = {
                BottomSheetContent()
            },
            scaffoldState = scaffoldState,
            sheetGesturesEnabled = true,
            sheetPeekHeight = 0.dp,
            sheetBackgroundColor = Color.White,
            sheetElevation = 0.dp,
            backgroundColor = Color.Transparent
        ) {
            MainColumn(
                navigator = navigator,
                isEdit = isEdit,
                state = state,
                channel = channel,
                scaffoldState = scaffoldState
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MainColumn(
    navigator: Navigator,
    isEdit: Boolean,
    state: WriteMemoState,
    channel: Channel<WriteMemoAction>,
    scaffoldState: BottomSheetScaffoldState
) {
    val scope = rememberCoroutineScope()
    var text by remember(state.text) { mutableStateOf(state.text) }
    val density = LocalDensity.current
    val imeInsets = WindowInsets.ime
    val isImeVisible by remember {
        derivedStateOf {
            imeInsets.getBottom(density) > 0
        }
    }

    Column {
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

        if (isImeVisible) {
            Spacer(modifier = Modifier.height(16.dp))
            return
        }

        SettingsLayout(
            state.isTodo,
            { channel.trySend(WriteMemoAction.SetTodo(it)) },
            state.isPin,
            { channel.trySend(WriteMemoAction.SetPin(it)) },
            stringResource(Res.string.group),
            { scope.launch { scaffoldState.bottomSheetState.expand() } }
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
                .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
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
    }
}

@Composable
private fun SettingsLayout(
    todo: Boolean,
    setTodo: (Boolean) -> Unit,
    pin: Boolean,
    setPin: (Boolean) -> Unit,
    group: String,
    showBottomSheet: () -> Unit
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

        VerticalDivider()

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

        VerticalDivider()

        Row(
            modifier = Modifier
                .clickable {
                    showBottomSheet()
                }
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = group,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(Res.string.unsorted),
                fontSize = 16.sp
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BottomSheetContent() {
    Column(modifier = Modifier
        .fillMaxWidth()
        .navigationBarsPadding()
    ) {
        val chipColors = AppColors.getChipColors()

        FlowRow(modifier = Modifier.padding(16.dp)) {
            repeat(10) {
                FilterChip(
                    selected = it % 2 == 0,
                    onClick = {},
                    label = {
                        androidx.compose.material3.Text(text = stringResource(Res.string.unsorted))
                    },
                    colors = chipColors,
                    modifier = Modifier.padding(end = 8.dp),
                    border = null
                )
            }

            ElevatedFilterChip(
                selected = false,
                onClick = {},
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_add),
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(12.dp)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        androidx.compose.material3.Text(
                            text = stringResource(Res.string.add_group),
                            style = TextStyle(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                colors = FilterChipDefaults.elevatedFilterChipColors(
                    containerColor = AppColors.Yellow
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}