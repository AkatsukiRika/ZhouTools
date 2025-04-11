package ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import helper.MemoHelper
import helper.SyncHelper
import helper.effect.EffectHelper
import helper.effect.MemoEffect
import kotlinx.coroutines.flow.Flow
import model.records.Memo
import moe.tlaster.precompose.molecule.collectAction
import moe.tlaster.precompose.navigation.Navigator

@Composable
fun WriteMemoPresenter(actionFlow: Flow<WriteMemoAction>): WriteMemoState {
    var memo by remember { mutableStateOf<Memo?>(null) }
    var text by remember { mutableStateOf("") }
    var isTodo by remember { mutableStateOf(false) }
    var isPin by remember { mutableStateOf(false) }
    var allGroups by remember { mutableStateOf(MemoHelper.getGroupSet()) }
    var group by remember { mutableStateOf<String?>(null) }

    actionFlow.collectAction {
        when (this) {
            is WriteMemoAction.BeginEdit -> {
                memo = editMemo
                text = editMemo.text
                isTodo = editMemo.isTodo
                isPin = editMemo.isPin
                group = editMemo.group
            }

            is WriteMemoAction.SetTodo -> {
                isTodo = this.newTodo
            }

            is WriteMemoAction.SetPin -> {
                isPin = this.newPin
            }

            is WriteMemoAction.SetGroup -> {
                group = this.group
                group?.let {
                    val newAllGroups = allGroups.toMutableSet()
                    newAllGroups.add(it)
                    allGroups = newAllGroups
                }
            }

            is WriteMemoAction.Delete -> {
                memo?.let {
                    MemoHelper.deleteMemo(it)
                }
                SyncHelper.autoPushMemo()
                EffectHelper.emitMemoEffect(MemoEffect.RefreshData)
                navigator.goBack()
            }

            is WriteMemoAction.Confirm -> {
                text = this.text
                if (memo == null) {
                    MemoHelper.addMemo(text, isTodo, isPin, group)
                } else {
                    memo?.let {
                        MemoHelper.modifyMemo(it, text, isTodo, isPin, group)
                    }
                }
                SyncHelper.autoPushMemo()
                EffectHelper.emitMemoEffect(MemoEffect.RefreshData)
                navigator.goBack()
            }
        }
    }

    return WriteMemoState(memo, text, isTodo, isPin, allGroups, group)
}

data class WriteMemoState(
    val memo: Memo? = null,
    val text: String = "",
    val isTodo: Boolean = false,
    val isPin: Boolean = false,
    val allGroups: Set<String> = emptySet(),
    val group: String? = null
)

sealed interface WriteMemoAction {
    data class BeginEdit(val editMemo: Memo) : WriteMemoAction
    data class SetTodo(val newTodo: Boolean) : WriteMemoAction
    data class SetPin(val newPin: Boolean) : WriteMemoAction
    data class SetGroup(val group: String?) : WriteMemoAction
    data class Delete(val navigator: Navigator) : WriteMemoAction
    data class Confirm(val text: String, val navigator: Navigator) : WriteMemoAction
}