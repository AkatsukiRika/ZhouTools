package ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import global.AppColors
import org.jetbrains.compose.resources.stringResource
import store.AppStore
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.cancel
import zhoutools.composeapp.generated.resources.confirm
import zhoutools.composeapp.generated.resources.enter_your_x_below
import zhoutools.composeapp.generated.resources.total_deposit_goal

@Composable
fun DepositGoalDialog(onCancel: () -> Unit, onConfirm: (Long?) -> Unit) {
    var totalDepositGoal by remember { mutableStateOf(AppStore.totalDepositGoal.toString()) }
    val isValidGoal by remember(totalDepositGoal) {
        derivedStateOf { totalDepositGoal.isNotEmpty() && totalDepositGoal.toLongOrNull() != null }
    }

    Dialog(onDismissRequest = {}) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            val annotatedString = buildAnnotatedString {
                append(stringResource(Res.string.enter_your_x_below).substringBefore("%s"))

                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(stringResource(Res.string.total_deposit_goal))
                }

                append(stringResource(Res.string.enter_your_x_below).substringAfter("%s"))
            }

            Text(
                text = annotatedString,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp),
            )

            TextField(
                value = totalDepositGoal,
                onValueChange = {
                    totalDepositGoal = it
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    textColor = if (isValidGoal) AppColors.DarkGreen else AppColors.Red
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
                        if (totalDepositGoal.isEmpty()) {
                            onConfirm(0)
                        } else {
                            onConfirm(totalDepositGoal.toLongOrNull())
                        }
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