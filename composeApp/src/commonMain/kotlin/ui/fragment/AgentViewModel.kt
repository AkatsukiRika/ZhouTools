package ui.fragment

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import util.TimeUtil

data class AgentState(
    val messages: List<AgentMessage> = listOf(AgentMessage.welcome()),
    val inputText: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null
)

data class AgentMessage(
    val id: String,
    val role: AgentRole,
    val content: String,
    val createdAt: Long,
    val status: AgentMessageStatus
) {
    companion object {
        fun welcome(): AgentMessage {
            return AgentMessage(
                id = "welcome",
                role = AgentRole.Assistant,
                content = "Hi, I can help you organize ideas, answer questions, or draft something together.",
                createdAt = 0L,
                status = AgentMessageStatus.Sent
            )
        }
    }
}

enum class AgentRole {
    User,
    Assistant,
    System
}

enum class AgentMessageStatus {
    Sending,
    Sent,
    Failed
}

sealed interface AgentAction {
    data class InputChanged(val text: String) : AgentAction
    data object SendMessage : AgentAction
    data class RetryMessage(val messageId: String) : AgentAction
    data object DismissError : AgentAction
}

class AgentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AgentState())
    val uiState: StateFlow<AgentState> = _uiState.asStateFlow()

    private var nextMessageId = 1L

    fun dispatch(action: AgentAction) {
        when (action) {
            is AgentAction.InputChanged -> {
                _uiState.update { it.copy(inputText = action.text) }
            }

            is AgentAction.SendMessage -> {
                sendMessage()
            }

            is AgentAction.RetryMessage -> {
                retryMessage(action.messageId)
            }

            is AgentAction.DismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun sendMessage() {
        val state = _uiState.value
        if (state.isSending) {
            return
        }

        val prompt = state.inputText.trim()
        if (prompt.isEmpty()) {
            return
        }

        val userMessage = createMessage(
            role = AgentRole.User,
            content = prompt,
            status = AgentMessageStatus.Sent
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isSending = true,
                errorMessage = null
            )
        }

        val assistantMessage = createMessage(
            role = AgentRole.Assistant,
            content = buildPlaceholderReply(prompt),
            status = AgentMessageStatus.Sent
        )

        _uiState.update {
            it.copy(
                messages = it.messages + assistantMessage,
                isSending = false
            )
        }
    }

    private fun retryMessage(messageId: String) {
        val failedMessage = _uiState.value.messages.firstOrNull {
            it.id == messageId && it.status == AgentMessageStatus.Failed
        } ?: return

        _uiState.update { it.copy(inputText = failedMessage.content, errorMessage = null) }
        sendMessage()
    }

    private fun createMessage(
        role: AgentRole,
        content: String,
        status: AgentMessageStatus
    ): AgentMessage {
        return AgentMessage(
            id = "agent-message-${nextMessageId++}",
            role = role,
            content = content,
            createdAt = TimeUtil.currentTimeMillis(),
            status = status
        )
    }

    private fun buildPlaceholderReply(prompt: String): String {
        return "I received: \"$prompt\". Once the LLM API is connected, I will answer this directly."
    }
}
