package ui.fragment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import constant.RouteConstants
import global.AppColors
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.scene.WriteMemoEvent
import ui.scene.WriteMemoObject
import util.MemoUtil
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.ic_pin
import zhoutools.composeapp.generated.resources.memo

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MemoFragment(navigator: Navigator) {
    val memoUtil = remember { MemoUtil() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.memo).uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
            )

            MemosLayout(memoUtil, navigator)
        }

        FloatingActionButton(
            backgroundColor = AppColors.Theme,
            onClick = {
                navigator.navigate(route = RouteConstants.ROUTE_WRITE_MEMO)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 36.dp, end = 18.dp)
        ) {
            Text(
                text = "+",
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun MemosLayout(memoUtil: MemoUtil, navigator: Navigator) {
    LazyColumn {
        items(memoUtil.getDisplayList()) { memo ->
            Card(modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .clickable {
                    WriteMemoObject.emitSync(WriteMemoEvent.BeginEdit(memo))
                    navigator.navigate(RouteConstants.ROUTE_WRITE_MEMO.replace(RouteConstants.PARAM_EDIT, "true"))
                }
            ) {
                Box(modifier = Modifier.padding(all = 8.dp)) {
                    Text(memo.text)

                    if (memo.isPin) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_pin),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
            }
        }
    }
}