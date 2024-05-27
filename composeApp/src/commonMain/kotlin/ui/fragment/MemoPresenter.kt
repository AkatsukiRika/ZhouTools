package ui.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import helper.MemoHelper
import helper.effect.EffectObserveHelper
import helper.effect.WriteMemoEffect
import kotlinx.coroutines.flow.Flow
import model.records.Memo
import moe.tlaster.precompose.molecule.collectAction

@Composable
fun MemoPresenter(actionFlow: Flow<MemoAction>, onGoEdit: () -> Unit): MemoState {
    var displayList by remember { mutableStateOf(MemoHelper.getDisplayList()) }
    var curMemo by remember { mutableStateOf<Memo?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    fun clickMemoItem(memo: Memo) {
        if (memo.isTodo) {
            curMemo = memo
            showBottomSheet = true
        } else {
            EffectObserveHelper.emitWriteMemoEffect(WriteMemoEffect.BeginEdit(memo))
            onGoEdit()
        }
    }

    fun clickEdit() {
        curMemo?.let {
            EffectObserveHelper.emitWriteMemoEffect(WriteMemoEffect.BeginEdit(it))
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
                    MemoHelper.markDone(it, !it.isTodoFinished)
                    curMemo = it.copy(isTodoFinished = !it.isTodoFinished)
                    displayList = MemoHelper.getDisplayList()
                }
                showBottomSheet = false
            }

            is MemoAction.HideBottomSheet -> {
                showBottomSheet = false
            }

            is MemoAction.RefreshDisplayList -> {
                displayList = MemoHelper.getDisplayList()
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