# Agent ViewModel Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a production-ready `AgentViewModel` state model and connect `AgentFragment` to it.

**Architecture:** Follow the existing fragment pattern: a `ViewModel` exposes `StateFlow<AgentState>`, accepts `AgentAction` through `dispatch`, and the Fragment renders state with `collectAsState()`. Keep the LLM placeholder inside the ViewModel send path so the later API integration can replace one function without UI changes.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, AndroidX lifecycle ViewModel, Kotlin coroutines `StateFlow`, `kotlin-test`.

---

## File Structure

- Modify `composeApp/build.gradle.kts`: add `commonTest` dependency on `kotlin-test`.
- Create `composeApp/src/commonMain/kotlin/ui/fragment/AgentViewModel.kt`: state, action, message models, and dispatch logic.
- Create `composeApp/src/commonTest/kotlin/ui/fragment/AgentViewModelTest.kt`: ViewModel behavior tests.
- Modify `composeApp/src/commonMain/kotlin/ui/fragment/AgentFragment.kt`: collect ViewModel state and render real messages/input.

### Task 1: Test Source Set

**Files:**
- Modify: `composeApp/build.gradle.kts`

- [ ] **Step 1: Add common test dependency**

Add this source set block inside `kotlin { sourceSets { ... } }`:

```kotlin
commonTest.dependencies {
    implementation(libs.kotlin.test)
}
```

- [ ] **Step 2: Run test task to check discovery**

Run: `./gradlew :composeApp:allTests`

Expected: Gradle discovers test tasks. It may fail before `AgentViewModel` exists if the test file has already been added.

### Task 2: AgentViewModel Behavior

**Files:**
- Create: `composeApp/src/commonMain/kotlin/ui/fragment/AgentViewModel.kt`
- Create: `composeApp/src/commonTest/kotlin/ui/fragment/AgentViewModelTest.kt`

- [ ] **Step 1: Write failing tests**

Create tests that instantiate `AgentViewModel`, dispatch `AgentAction` values, and assert `uiState.value`.

```kotlin
@Test
fun sendMessageAppendsUserAndAssistantMessages() {
    val viewModel = AgentViewModel()

    viewModel.dispatch(AgentAction.InputChanged("Plan my day"))
    viewModel.dispatch(AgentAction.SendMessage)

    val state = viewModel.uiState.value
    assertEquals("", state.inputText)
    assertEquals(3, state.messages.size)
    assertEquals(AgentRole.User, state.messages[1].role)
    assertEquals("Plan my day", state.messages[1].content)
    assertEquals(AgentMessageStatus.Sent, state.messages[1].status)
    assertEquals(AgentRole.Assistant, state.messages[2].role)
    assertEquals(AgentMessageStatus.Sent, state.messages[2].status)
}
```

- [ ] **Step 2: Run tests and confirm red**

Run: `./gradlew :composeApp:allTests`

Expected: compilation or test failure because `AgentViewModel` and related types are not implemented yet.

- [ ] **Step 3: Implement ViewModel**

Implement `AgentState`, `AgentAction`, `AgentRole`, `AgentMessageStatus`, `AgentMessage`, and `AgentViewModel`.

The send logic trims input, ignores blank input, ignores duplicate sends, appends the user message, clears input, and appends a deterministic assistant placeholder.

- [ ] **Step 4: Run tests and confirm green**

Run: `./gradlew :composeApp:allTests`

Expected: all `AgentViewModelTest` tests pass.

### Task 3: Fragment Integration

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/ui/fragment/AgentFragment.kt`

- [ ] **Step 1: Replace local state with ViewModel state**

Add `val viewModel = viewModel { AgentViewModel() }` and `val state by viewModel.uiState.collectAsState()`.

- [ ] **Step 2: Render state messages**

Change `ChatContent` to accept `messages: List<AgentMessage>` and render each item with `MessageBubble(text = message.content, isUser = message.role == AgentRole.User)`.

- [ ] **Step 3: Wire input actions**

Change `ChatInputBar` to accept `value`, `enabled`, `onValueChange`, and `onSend`. Dispatch `AgentAction.InputChanged` and `AgentAction.SendMessage` from the Fragment.

- [ ] **Step 4: Verify compile**

Run: `./gradlew :composeApp:compileKotlinMetadata`

Expected: Kotlin metadata compilation succeeds.
