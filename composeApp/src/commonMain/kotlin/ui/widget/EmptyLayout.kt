package ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.ic_empty

@Composable
fun EmptyLayout(modifier: Modifier = Modifier, description: String) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 64.dp)
            .alpha(0.5f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_empty),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            fontSize = 16.sp
        )
    }
}