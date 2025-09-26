package ui.scene

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import helper.MemoHelper
import helper.SyncHelper
import helper.effect.EffectHelper
import helper.effect.MemoEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import model.records.Memo

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
    data object Delete : WriteMemoAction
    data class Confirm(val text: String) : WriteMemoAction
}

sealed interface WriteMemoEvent {
    data object GoBack : WriteMemoEvent
}

class WriteMemoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(WriteMemoState())
    val uiState: StateFlow<WriteMemoState> = _uiState.asStateFlow()

    private val _writeMemoEvent = MutableSharedFlow<WriteMemoEvent>()
    val writeMemoEvent: SharedFlow<WriteMemoEvent> = _writeMemoEvent.asSharedFlow()

    init {
        _uiState.update { it.copy(allGroups = MemoHelper.getGroupSet()) }
    }

    fun dispatch(action: WriteMemoAction) {
        when (action) {
            is WriteMemoAction.BeginEdit -> {
                _uiState.update {
                    it.copy(
                        memo = action.editMemo,
                        text = action.editMemo.text,
                        isTodo = action.editMemo.isTodo,
                        isPin = action.editMemo.isPin,
                        group = action.editMemo.group
                    )
                }
            }

            is WriteMemoAction.SetTodo -> {
                _uiState.update { it.copy(isTodo = action.newTodo) }
            }

            is WriteMemoAction.SetPin -> {
                _uiState.update { it.copy(isPin = action.newPin) }
            }

            is WriteMemoAction.SetGroup -> {
                val currentGroups = _uiState.value.allGroups.toMutableSet()
                action.group?.let { currentGroups.add(it) }
                _uiState.update {
                    it.copy(
                        group = action.group,
                        allGroups = currentGroups
                    )
                }
            }

            is WriteMemoAction.Delete -> {
                _uiState.value.memo?.let {
                    MemoHelper.deleteMemo(it)
                }
                SyncHelper.autoPushMemo()
                EffectHelper.emitMemoEffect(MemoEffect.RefreshData)
                viewModelScope.launch {
                    _writeMemoEvent.emit(WriteMemoEvent.GoBack)
                }
            }

            is WriteMemoAction.Confirm -> {
                val currentState = _uiState.value
                if (currentState.memo == null) {
                    MemoHelper.addMemo(action.text, currentState.isTodo, currentState.isPin, currentState.group)
                } else {
                    MemoHelper.modifyMemo(currentState.memo, action.text, currentState.isTodo, currentState.isPin, currentState.group)
                }
                SyncHelper.autoPushMemo()
                EffectHelper.emitMemoEffect(MemoEffect.RefreshData)
                viewModelScope.launch {
                    _writeMemoEvent.emit(WriteMemoEvent.GoBack)
                }
            }
        }
    }
}
