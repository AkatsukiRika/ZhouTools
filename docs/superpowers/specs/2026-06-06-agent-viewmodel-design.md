# Agent ViewModel Design

## Goal

Add a dedicated ViewModel and state model for `AgentFragment` so the chat UI can run on structured state now and later swap in a real LLM API call without reshaping the screen.

## Architecture

`AgentFragment` will follow the existing fragment pattern used by `MemoFragment`, `ScheduleFragment`, and `TimeCardFragment`: create a ViewModel with `viewModel { AgentViewModel() }`, collect a `StateFlow`, and route user interaction through a `dispatch(AgentAction)` function.

The ViewModel will own the message list, current input text, send-in-progress flag, and user-visible error text. It will include enough message metadata for a real API integration: message id, role, content, timestamp, and delivery status.

## State Model

`AgentState` contains:

- `messages`: ordered list of `AgentMessage`
- `inputText`: current input field text
- `isSending`: true while an assistant response is being produced
- `errorMessage`: nullable text for send failures

`AgentMessage` contains:

- `id`: stable string id
- `role`: `User`, `Assistant`, or `System`
- `content`: rendered message text
- `createdAt`: millisecond timestamp
- `status`: `Sending`, `Sent`, or `Failed`

## Actions

`AgentAction` contains:

- `InputChanged(text)`: update the input field
- `SendMessage`: append a user message, clear input, and produce an assistant reply
- `RetryMessage(messageId)`: reserved for failed message retry behavior
- `DismissError`: clear the error message

## Current Behavior

Until a real LLM API is connected, `SendMessage` will append the user message and then append a deterministic assistant placeholder response. The placeholder lives inside the ViewModel send function so replacing it with a repository/API call does not require changing the Fragment or state model.

Empty or blank input does nothing. While sending, duplicate sends are ignored.

## UI Integration

`AgentFragment` will render `state.messages` instead of hard-coded sample bubbles. The input bar will receive `value`, `enabled`, `onValueChange`, and `onSend` from the Fragment. The send button will dispatch `AgentAction.SendMessage`.

## Testing

Add common tests for the ViewModel:

- Initial state includes assistant welcome message and empty input.
- `InputChanged` updates `inputText`.
- `SendMessage` with blank input does not append messages.
- `SendMessage` with text appends a user message, clears input, and appends an assistant message.
