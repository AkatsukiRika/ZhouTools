package ui.fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.schedule

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ScheduleFragment(navigator: Navigator) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(Res.string.schedule).uppercase(),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
        )

        CalendarGrid()
    }
}

@Composable
private fun CalendarGrid() {
    LazyVerticalGrid(
        modifier = Modifier
            .background(Color.White)
            .fillMaxWidth(),
        columns = GridCells.Fixed(7),
        content = {
            items(7) {
                Text(
                    text = "Sun",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }
            items(42) {
                Text(
                    text = "1",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    )
}