package ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.ic_alarm
import zhoutools.composeapp.generated.resources.warning

@Composable
fun WarningDialog(
    title: String? = null,
    content: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val titleStr = title ?: stringResource(Res.string.warning)

    Dialog(onDismissRequest = {}) {
        Box(modifier = Modifier.background(Color.White, shape = RoundedCornerShape(32.dp))) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = painterResource(Res.drawable.ic_alarm),
                    contentDescription = null,
                    modifier = Modifier.size(54.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = titleStr,
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = content,
                    color = Color.Black,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = confirmText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    lineHeight = 58.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(1000.dp))
                        .background(Color.Black)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = { Color.White })
                        ) {
                            onConfirm()
                        }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }

            DismissButton(onDismiss, modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
            )
        }
    }
}