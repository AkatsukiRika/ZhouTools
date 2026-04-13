package ui.scene

import androidx.lifecycle.ViewModel
import com.tangping.lib.firebase.getHomeTabList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DebugViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DebugState())
    val uiState = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(homeTabList = getHomeTabList())  }
    }
}

data class DebugState(
    val homeTabList: String = ""
)