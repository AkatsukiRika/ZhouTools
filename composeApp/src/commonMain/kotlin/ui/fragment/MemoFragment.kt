package ui.fragment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import constant.RouteConstants
import global.AppColors
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import util.MemoUtil
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.memo

object MemoObject {
    private val _eventFlow = MutableSharedFlow<MemoExternalEvent?>(replay = 1)

    val eventFlow: SharedFlow<MemoExternalEvent?>
        get() = _eventFlow

    fun emitSync(event: MemoExternalEvent?) {
        runBlocking {
            _eventFlow.emit(event)
        }
    }
}

sealed interface MemoExternalEvent {
    data class WriteMemo(val text: String, val todo: Boolean, val pin: Boolean) : MemoExternalEvent
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MemoFragment(navigator: Navigator) {
    val event = MemoObject.eventFlow.collectAsStateWithLifecycle(initial = null).value
    val memoUtil = remember { MemoUtil() }

    LaunchedEffect(event) {
        if (event != null) {
            when (event) {
                is MemoExternalEvent.WriteMemo -> {
                    memoUtil.addMemo(event.text, event.todo, event.pin)
                }
            }
            MemoObject.emitSync(null)
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
    ) {
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