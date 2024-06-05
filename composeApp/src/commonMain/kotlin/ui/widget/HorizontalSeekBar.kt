package ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import global.AppColors
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.ic_arrow_down
import kotlin.math.roundToInt

@Composable
fun HorizontalSeekBar(
    modifier: Modifier = Modifier,
    itemList: List<String>,
    itemWidth: Dp,
    defaultSelectItem: String? = null,
    onSelectItem: ((String) -> Unit)? = null
) {
    fun getDefaultSelectIndex(): Int {
        val index = itemList.indexOf(defaultSelectItem)
        return if (index >= 0) index else 0
    }

    val density = LocalDensity.current
    val toPx = { dp: Dp ->
        density.run { dp.toPx() }
    }
    val toDp = { px: Float ->
        density.run { px.toDp() }
    }
    var thisWidth by remember { mutableIntStateOf(0) }
    val lazyListState = rememberLazyListState()
    val paddingHorizontal = thisWidth / 2 - toPx(itemWidth) / 2
    var selectIndex by remember { mutableIntStateOf(getDefaultSelectIndex()) }
    var isInitialScroll by remember { mutableStateOf(true) }

    fun selectItem(index: Int) {
        if (index in itemList.indices) {
            selectIndex = index
            onSelectItem?.invoke(itemList[index])
        }
    }

    LaunchedEffect(Unit) {
        lazyListState.animateScrollToItem(selectIndex)
        isInitialScroll = false
    }

    LaunchedEffect(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset) {
        if (!isInitialScroll) {
            lazyListState.apply {
                val totalOffset = firstVisibleItemIndex * toPx(itemWidth) + firstVisibleItemScrollOffset
                selectItem((totalOffset / toPx(itemWidth)).roundToInt())
            }
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.isScrollInProgress }.collect { isScrolling ->
            if (!isScrolling) {
                delay(100)
                if (!lazyListState.isScrollInProgress) {
                    lazyListState.animateScrollToItem(selectIndex)
                }
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged {
                thisWidth = it.width
            }
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_down),
            tint = AppColors.Theme,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            state = lazyListState,
            contentPadding = PaddingValues(horizontal = if (paddingHorizontal > 0) toDp(paddingHorizontal) else 0.dp)
        ) {
            itemsIndexed(itemList) { index, it ->
                Text(
                    text = it,
                    fontSize = if (index == selectIndex) 20.sp else 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = if (index == selectIndex) FontWeight.ExtraBold else FontWeight.Normal,
                    modifier = Modifier
                        .width(itemWidth)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}