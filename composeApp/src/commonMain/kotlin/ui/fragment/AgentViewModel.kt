package ui.fragment

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import util.TimeUtil

data class AgentState(
    val conversations: List<AgentConversation> = listOf(AgentConversation.initial()),
    val currentConversationId: String = DEFAULT_AGENT_CONVERSATION_ID,
    val availableModels: List<AgentModel> = AgentModel.defaults(),
    val selectedModelId: String = DEFAULT_AGENT_MODEL_ID,
    val messages: List<AgentMessage> = AgentConversation.initial().messages,
    val inputText: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedModel: AgentModel
        get() = availableModels.firstOrNull { it.id == selectedModelId } ?: availableModels.first()
}

data class AgentConversation(
    val id: String,
    val title: String,
    val messages: List<AgentMessage>,
    val updatedAt: Long
) {
    companion object {
        fun initial(): AgentConversation {
            return AgentConversation(
                id = DEFAULT_AGENT_CONVERSATION_ID,
                title = DEFAULT_AGENT_CONVERSATION_TITLE,
                messages = listOf(AgentMessage.welcome()),
                updatedAt = 0L
            )
        }
    }
}

data class AgentModel(
    val id: String,
    val displayName: String
) {
    companion object {
        fun defaults(): List<AgentModel> {
            return listOf(
                AgentModel(id = "fast", displayName = "Fast"),
                AgentModel(id = DEFAULT_AGENT_MODEL_ID, displayName = "Balanced"),
                AgentModel(id = "deep", displayName = "Deep")
            )
        }
    }
}

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
    data object CreateConversation : AgentAction
    data class OpenConversation(val conversationId: String) : AgentAction
    data class SelectModel(val modelId: String) : AgentAction
}

class AgentViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AgentState())
    val uiState: StateFlow<AgentState> = _uiState.asStateFlow()

    private var nextMessageId = 1L
    private var nextConversationId = 1L

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

            is AgentAction.CreateConversation -> {
                createConversation()
            }

            is AgentAction.OpenConversation -> {
                openConversation(action.conversationId)
            }

            is AgentAction.SelectModel -> {
                selectModel(action.modelId)
            }
        }
    }

    private fun createConversation() {
        val conversation = AgentConversation(
            id = "agent-conversation-${nextConversationId++}",
            title = DEFAULT_AGENT_CONVERSATION_TITLE,
            messages = listOf(AgentMessage.welcome()),
            updatedAt = TimeUtil.currentTimeMillis()
        )
        _uiState.update {
            it.copy(
                conversations = listOf(conversation) + it.conversations,
                currentConversationId = conversation.id,
                messages = conversation.messages,
                inputText = "",
                errorMessage = null
            )
        }
    }

    private fun openConversation(conversationId: String) {
        val conversation = _uiState.value.conversations.firstOrNull { it.id == conversationId } ?: return
        _uiState.update {
            it.copy(
                currentConversationId = conversation.id,
                messages = conversation.messages,
                inputText = "",
                errorMessage = null
            )
        }
    }

    private fun selectModel(modelId: String) {
        if (_uiState.value.availableModels.none { it.id == modelId }) {
            return
        }
        _uiState.update { it.copy(selectedModelId = modelId) }
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
            val currentMessages = it.messages + userMessage
            it.copy(
                messages = currentMessages,
                conversations = it.conversations.updateCurrentConversation(
                    currentConversationId = it.currentConversationId,
                    messages = currentMessages,
                    updatedAt = userMessage.createdAt
                ),
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
            val currentMessages = it.messages + assistantMessage
            it.copy(
                messages = currentMessages,
                conversations = it.conversations.updateCurrentConversation(
                    currentConversationId = it.currentConversationId,
                    messages = currentMessages,
                    updatedAt = assistantMessage.createdAt
                ),
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

    private fun List<AgentConversation>.updateCurrentConversation(
        currentConversationId: String,
        messages: List<AgentMessage>,
        updatedAt: Long
    ): List<AgentConversation> {
        return map { conversation ->
            if (conversation.id == currentConversationId) {
                conversation.copy(
                    title = messages.firstUserMessageTitle() ?: conversation.title,
                    messages = messages,
                    updatedAt = updatedAt
                )
            } else {
                conversation
            }
        }.sortedByDescending { it.updatedAt }
    }

    private fun List<AgentMessage>.firstUserMessageTitle(): String? {
        return firstOrNull { it.role == AgentRole.User }
            ?.content
            ?.take(MAX_AGENT_CONVERSATION_TITLE_LENGTH)
    }
}

private const val DEFAULT_AGENT_CONVERSATION_ID = "agent-conversation-initial"
private const val DEFAULT_AGENT_CONVERSATION_TITLE = "New Conversation"
private const val DEFAULT_AGENT_MODEL_ID = "balanced"
private const val MAX_AGENT_CONVERSATION_TITLE_LENGTH = 48
