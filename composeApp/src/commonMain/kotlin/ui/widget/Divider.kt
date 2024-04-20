package ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import global.AppColors

@Composable
fun VerticalDivider() {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(AppColors.Divider)
    )
}