package ui.scene

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import store.AppFlowStore

class DebugViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DebugState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            AppFlowStore.homeTabList.collectLatest { homeTabList ->
                _uiState.update { it.copy(homeTabList = homeTabList) }
            }
        }
    }
}

data class DebugState(
    val homeTabList: String = ""
)