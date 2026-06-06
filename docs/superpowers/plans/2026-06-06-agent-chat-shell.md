# Agent Chat Shell Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a chatbot-style Agent header with an in-memory conversation drawer and model dropdown.

**Architecture:** Keep business state in `AgentViewModel` and transient UI state in `AgentFragment`. Extend existing tests before implementation, then wire the UI to the new state and actions.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform Material, AndroidX lifecycle ViewModel, Kotlin test.

---

## File Structure

- Modify `composeApp/src/commonTest/kotlin/ui/fragment/AgentViewModelTest.kt`: add tests for conversations and model selection.
- Modify `composeApp/src/commonMain/kotlin/ui/fragment/AgentViewModel.kt`: add `AgentConversation`, `AgentModel`, state fields, and actions.
- Modify `composeApp/src/commonMain/kotlin/ui/fragment/AgentFragment.kt`: replace `FragmentHeader` usage with Agent-specific header, drawer, and dropdown.

### Task 1: ViewModel Tests

- [ ] Add tests for initial conversation/model state.
- [ ] Add tests for creating and switching conversations.
- [ ] Add tests for selecting a model.
- [ ] Add tests that the first user message becomes conversation title.
- [ ] Run `./gradlew :composeApp:testDebugUnitTest` and confirm the new tests fail before implementation.

### Task 2: ViewModel State

- [ ] Add `AgentConversation` and `AgentModel` data classes.
- [ ] Extend `AgentState` with conversations and models.
- [ ] Add `CreateConversation`, `OpenConversation`, and `SelectModel` actions.
- [ ] Update send logic to mutate the active conversation and expose active messages through `state.messages`.
- [ ] Run `./gradlew :composeApp:testDebugUnitTest` and confirm tests pass.

### Task 3: Fragment UI

- [ ] Wrap the Agent screen in a left `ModalDrawer`.
- [ ] Replace the generic `FragmentHeader` with `AgentHeader`.
- [ ] Add drawer content for `New Chat` and conversation history.
- [ ] Add model dropdown menu in the right header slot.
- [ ] Run `./gradlew :composeApp:testDebugUnitTest` for Android verification.
