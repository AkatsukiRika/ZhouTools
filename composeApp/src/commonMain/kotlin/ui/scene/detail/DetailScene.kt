package ui.scene.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import global.AppColors
import moe.tlaster.precompose.molecule.rememberPresenter
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.details
import zhoutools.composeapp.generated.resources.history
import zhoutools.composeapp.generated.resources.ic_back
import zhoutools.composeapp.generated.resources.today

@OptIn(ExperimentalResourceApi::class, ExperimentalFoundationApi::class)
@Composable
fun DetailScene(navigator: Navigator) {
    val (state, channel) = rememberPresenter { DetailPresenter(it) }
    val tabs = mapOf(
        DETAIL_TAB_TODAY to stringResource(Res.string.today),
        DETAIL_TAB_HISTORY to stringResource(Res.string.history)
    )

    LazyColumn(modifier = Modifier
        .fillMaxSize()
        .background(AppColors.Background)
    ) {
        item {
            TitleBar(navigator)
        }

        stickyHeader {
            TabRow(
                selectedTabIndex = state.tab,
                backgroundColor = Color.White,
                contentColor = AppColors.Theme
            ) {
                tabs.forEach {
                    val index = it.key
                    val title = it.value
                    Tab(
                        selected = state.tab == index,
                        onClick = {
                            channel.trySend(DetailAction.ChangeTab(index))
                        },
                        text = {
                            Text(text = title)
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(2000.dp))
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun TitleBar(navigator: Navigator) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
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
            text = stringResource(Res.string.details),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 12.dp)
        )
    }
}