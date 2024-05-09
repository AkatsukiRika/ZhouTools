package ui.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arch.EffectObservers
import arch.WriteMemoEffect
import kotlinx.coroutines.flow.Flow
import model.records.Memo
import moe.tlaster.precompose.molecule.collectAction
import util.MemoUtil

@Composable
fun MemoPresenter(actionFlow: Flow<MemoAction>, onGoEdit: () -> Unit): MemoState {
    val memoUtil = remember { MemoUtil() }
    var displayList by remember { mutableStateOf(memoUtil.getDisplayList()) }
    var curMemo by remember { mutableStateOf<Memo?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    fun clickMemoItem(memo: Memo) {
        if (memo.isTodo) {
            curMemo = memo
            showBottomSheet = true
        } else {
            EffectObservers.emitWriteMemoEffect(WriteMemoEffect.BeginEdit(memo))
            onGoEdit()
        }
    }

    fun clickEdit() {
        curMemo?.let {
            EffectObservers.emitWriteMemoEffect(WriteMemoEffect.BeginEdit(it))
            onGoEdit()
        }
    }

    actionFlow.collectAction {
        when (this) {
            is MemoAction.ClickMemoItem -> {
                clickMemoItem(memo)
            }

            is MemoAction.ClickEdit -> {
                clickEdit()
                showBottomSheet = false
            }

            is MemoAction.MarkDone -> {
                curMemo?.let {
                    memoUtil.markDone(it, !it.isTodoFinished)
                    curMemo = it.copy(isTodoFinished = !it.isTodoFinished)
                    displayList = memoUtil.getDisplayList()
                }
                showBottomSheet = false
            }

            is MemoAction.HideBottomSheet -> {
                showBottomSheet = false
            }

            is MemoAction.RefreshDisplayList -> {
                displayList = memoUtil.getDisplayList()
            }
        }
    }

    return MemoState(displayList, curMemo, showBottomSheet)
}

data class MemoState(
    val displayList: List<Memo> = emptyList(),
    val curMemo: Memo? = null,
    val showBottomSheet: Boolean = false
)

sealed interface MemoAction {
    data class ClickMemoItem(val memo: Memo) : MemoAction
    data object ClickEdit : MemoAction
    data object MarkDone : MemoAction
    data object HideBottomSheet : MemoAction
    data object RefreshDisplayList : MemoAction
}