package ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.painterResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.ic_back

@Composable
fun TitleBar(navigator: Navigator, title: String) {
    val rootModifier = Modifier
        .background(Color.White)
        .statusBarsPadding()
        .fillMaxWidth()

    Row(
        modifier = rootModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))

        Box(modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable {
                navigator.goBack()
            }
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_back),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}