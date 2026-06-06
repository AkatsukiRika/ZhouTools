package ui.fragment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class AgentViewModelTest {

    @Test
    fun initialStateContainsWelcomeMessageAndEmptyInput() {
        val viewModel = AgentViewModel()

        val state = viewModel.uiState.value

        assertEquals("", state.inputText)
        assertFalse(state.isSending)
        assertNull(state.errorMessage)
        assertEquals(1, state.conversations.size)
        assertEquals(state.conversations.first().id, state.currentConversationId)
        assertEquals("Balanced", state.selectedModel.displayName)
        assertEquals(1, state.messages.size)
        assertEquals(AgentRole.Assistant, state.messages.first().role)
        assertEquals(AgentMessageStatus.Sent, state.messages.first().status)
    }

    @Test
    fun inputChangedUpdatesInputText() {
        val viewModel = AgentViewModel()

        viewModel.dispatch(AgentAction.InputChanged("Plan my day"))

        assertEquals("Plan my day", viewModel.uiState.value.inputText)
    }

    @Test
    fun sendMessageIgnoresBlankInput() {
        val viewModel = AgentViewModel()

        viewModel.dispatch(AgentAction.InputChanged("   "))
        viewModel.dispatch(AgentAction.SendMessage)

        val state = viewModel.uiState.value
        assertEquals("   ", state.inputText)
        assertEquals(1, state.messages.size)
    }

    @Test
    fun sendMessageAppendsUserAndAssistantMessages() {
        val viewModel = AgentViewModel()

        viewModel.dispatch(AgentAction.InputChanged("Plan my day"))
        viewModel.dispatch(AgentAction.SendMessage)

        val state = viewModel.uiState.value
        assertEquals("", state.inputText)
        assertFalse(state.isSending)
        assertNull(state.errorMessage)
        assertEquals(3, state.messages.size)
        assertEquals(AgentRole.User, state.messages[1].role)
        assertEquals("Plan my day", state.messages[1].content)
        assertEquals(AgentMessageStatus.Sent, state.messages[1].status)
        assertEquals(AgentRole.Assistant, state.messages[2].role)
        assertEquals(AgentMessageStatus.Sent, state.messages[2].status)
    }

    @Test
    fun createConversationAddsConversationAndSwitchesToIt() {
        val viewModel = AgentViewModel()
        val initialConversationId = viewModel.uiState.value.currentConversationId

        viewModel.dispatch(AgentAction.CreateConversation)

        val state = viewModel.uiState.value
        assertEquals(2, state.conversations.size)
        assertFalse(state.currentConversationId == initialConversationId)
        assertEquals(1, state.messages.size)
        assertEquals(AgentRole.Assistant, state.messages.first().role)
    }

    @Test
    fun openConversationSwitchesCurrentMessages() {
        val viewModel = AgentViewModel()
        val firstConversationId = viewModel.uiState.value.currentConversationId
        viewModel.dispatch(AgentAction.InputChanged("First chat"))
        viewModel.dispatch(AgentAction.SendMessage)
        viewModel.dispatch(AgentAction.CreateConversation)
        viewModel.dispatch(AgentAction.InputChanged("Second chat"))
        viewModel.dispatch(AgentAction.SendMessage)

        viewModel.dispatch(AgentAction.OpenConversation(firstConversationId))

        val state = viewModel.uiState.value
        assertEquals(firstConversationId, state.currentConversationId)
        assertEquals("First chat", state.messages[1].content)
    }

    @Test
    fun selectModelUpdatesSelectedModel() {
        val viewModel = AgentViewModel()

        viewModel.dispatch(AgentAction.SelectModel("deep"))

        val state = viewModel.uiState.value
        assertEquals("deep", state.selectedModel.id)
        assertEquals("Deep", state.selectedModel.displayName)
    }

    @Test
    fun firstUserMessageBecomesConversationTitle() {
        val viewModel = AgentViewModel()

        viewModel.dispatch(AgentAction.InputChanged("Summarize project status"))
        viewModel.dispatch(AgentAction.SendMessage)

        val state = viewModel.uiState.value
        assertEquals("Summarize project status", state.conversations.first().title)
    }
}
