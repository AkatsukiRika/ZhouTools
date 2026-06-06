# Agent Chat Shell Design

## Goal

Turn the Agent fragment header into a chat-oriented shell with a left conversation drawer and a right model selector, while keeping conversations in memory only for this phase.

## Scope

This phase implements complete UI and state transitions for:

- Opening a left-side conversation drawer.
- Showing current in-memory conversation history.
- Creating a new conversation.
- Switching between conversations.
- Selecting a placeholder model from `Fast`, `Balanced`, and `Deep`.

Conversation persistence is intentionally out of scope. Restarting the app can reset the chat list.

## Architecture

`AgentViewModel` owns chat business state: conversations, active conversation, available models, and selected model. `AgentFragment` owns transient UI state such as whether the drawer or dropdown is open.

The Fragment renders `state.messages` for the active conversation. The sidebar renders `state.conversations`. The model menu renders `state.availableModels` and dispatches `AgentAction.SelectModel`.

## UI

The header keeps the project's compact Fragment style: 16 dp horizontal padding, bold uppercase title, light background, and theme-color emphasis.

The left header button opens a drawer. The drawer contains an `AGENT` title, a `New Chat` row with the add icon, and a list of conversations. The active conversation uses the theme color and bolder text.

The right header control is a compact model pill. It displays the current model name and an arrow-down icon. Tapping it opens a dropdown menu with all placeholder models.

## State

`AgentState` adds:

- `conversations`
- `currentConversationId`
- `availableModels`
- `selectedModelId`

`AgentConversation` contains an id, title, messages, and updated timestamp.

`AgentModel` contains an id and display name.

## Actions

`AgentAction` adds:

- `CreateConversation`
- `OpenConversation(conversationId)`
- `SelectModel(modelId)`

Sending a message updates the current conversation. The first user message becomes the conversation title.
