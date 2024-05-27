package ui.scene

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import helper.MemoHelper
import kotlinx.coroutines.flow.Flow
import model.records.Memo
import moe.tlaster.precompose.molecule.collectAction

@Composable
fun WriteMemoPresenter(actionFlow: Flow<WriteMemoAction>): WriteMemoState {
    var memo by remember { mutableStateOf<Memo?>(null) }
    var text by remember { mutableStateOf("") }
    var isTodo by remember { mutableStateOf(false) }
    var isPin by remember { mutableStateOf(false) }

    actionFlow.collectAction {
        when (this) {
            is WriteMemoAction.BeginEdit -> {
                memo = editMemo
                text = editMemo.text
                isTodo = editMemo.isTodo
                isPin = editMemo.isPin
            }

            is WriteMemoAction.SetTodo -> {
                isTodo = this.newTodo
            }

            is WriteMemoAction.SetPin -> {
                isPin = this.newPin
            }

            is WriteMemoAction.Delete -> {
                memo?.let {
                    MemoHelper.deleteMemo(it)
                }
            }

            is WriteMemoAction.Confirm -> {
                text = this.text
                if (memo == null) {
                    MemoHelper.addMemo(text, isTodo, isPin)
                } else {
                    memo?.let {
                        MemoHelper.modifyMemo(it, text, isTodo, isPin)
                    }
                }
            }
        }
    }

    return WriteMemoState(memo, text, isTodo, isPin)
}

data class WriteMemoState(
    val memo: Memo? = null,
    val text: String = "",
    val isTodo: Boolean = false,
    val isPin: Boolean = false
)

sealed interface WriteMemoAction {
    data class BeginEdit(val editMemo: Memo) : WriteMemoAction
    data class SetTodo(val newTodo: Boolean) : WriteMemoAction
    data class SetPin(val newPin: Boolean) : WriteMemoAction
    data object Delete : WriteMemoAction
    data class Confirm(val text: String) : WriteMemoAction
}