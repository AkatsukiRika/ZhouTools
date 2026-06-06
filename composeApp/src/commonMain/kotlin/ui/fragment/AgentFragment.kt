package ui.fragment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import constant.TabConstants
import extension.clickableNoRipple
import global.AppColors
import hideSoftwareKeyboard
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.widget.BaseImmersiveScene
import ui.widget.AutoSyncIndicator
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.agent
import zhoutools.composeapp.generated.resources.agent_model
import zhoutools.composeapp.generated.resources.agent_message_hint
import zhoutools.composeapp.generated.resources.agent_new_chat
import zhoutools.composeapp.generated.resources.ic_add
import zhoutools.composeapp.generated.resources.ic_arrow_down
import zhoutools.composeapp.generated.resources.ic_menu
import zhoutools.composeapp.generated.resources.ic_send

@Composable
fun AgentFragment(navController: NavHostController) {
    val viewModel = viewModel { AgentViewModel() }
    val state by viewModel.uiState.collectAsState()
    var showDrawerOverlay by remember { mutableStateOf(false) }
    val drawerVisibleState = remember { MutableTransitionState(false) }

    fun openDrawer() {
        showDrawerOverlay = true
        drawerVisibleState.targetState = true
    }

    fun closeDrawer() {
        drawerVisibleState.targetState = false
    }

    LaunchedEffect(drawerVisibleState.currentState, drawerVisibleState.targetState, drawerVisibleState.isIdle) {
        if (drawerVisibleState.isIdle && !drawerVisibleState.currentState && !drawerVisibleState.targetState) {
            showDrawerOverlay = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        BaseImmersiveScene(
            modifier = Modifier
                .imePadding()
                .fillMaxSize()
                .background(AppColors.Background),
            navigationBarPadding = false
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
                .clickableNoRipple {
                    hideSoftwareKeyboard()
                }
            ) {
                AgentHeader(
                    navController = navController,
                    state = state,
                    onOpenDrawer = {
                        openDrawer()
                    },
                    onSelectModel = {
                        viewModel.dispatch(AgentAction.SelectModel(it))
                    }
                )

                ChatContent(
                    messages = state.messages,
                    modifier = Modifier.weight(1f)
                )

                ChatInputBar(
                    value = state.inputText,
                    enabled = !state.isSending,
                    onValueChange = {
                        viewModel.dispatch(AgentAction.InputChanged(it))
                    },
                    onSend = {
                        viewModel.dispatch(AgentAction.SendMessage)
                    }
                )
            }
        }

        if (showDrawerOverlay) {
            AnimatedVisibility(
                visibleState = drawerVisibleState,
                enter = fadeIn(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(220))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.28f))
                        .clickableNoRipple {
                            closeDrawer()
                        }
                )
            }

            AnimatedVisibility(
                visibleState = drawerVisibleState,
                enter = slideInHorizontally(animationSpec = tween(260)) { -it },
                exit = slideOutHorizontally(animationSpec = tween(220)) { -it },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                AgentConversationDrawer(
                    conversations = state.conversations,
                    currentConversationId = state.currentConversationId,
                    onCreateConversation = {
                        viewModel.dispatch(AgentAction.CreateConversation)
                        closeDrawer()
                    },
                    onOpenConversation = {
                        viewModel.dispatch(AgentAction.OpenConversation(it))
                        closeDrawer()
                    }
                )
            }
        }
    }
}

@Composable
private fun AgentHeader(
    navController: NavHostController,
    state: AgentState,
    onOpenDrawer: () -> Unit,
    onSelectModel: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderIconButton(onClick = onOpenDrawer) {
            Icon(
                painter = painterResource(Res.drawable.ic_menu),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = stringResource(Res.string.agent).uppercase(),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 12.dp)
        )

        AutoSyncIndicator(navController, TabConstants.TAB_AGENT)

        Spacer(modifier = Modifier.weight(1f))

        AgentModelSelector(
            models = state.availableModels,
            selectedModel = state.selectedModel,
            onSelectModel = onSelectModel
        )
    }
}

@Composable
private fun HeaderIconButton(onClick: () -> Unit, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        content = content
    )
}

@Composable
private fun AgentModelSelector(
    models: List<AgentModel>,
    selectedModel: AgentModel,
    onSelectModel: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .clickable { expanded = true }
                .padding(start = 12.dp, top = 8.dp, end = 10.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedModel.displayName,
                color = AppColors.Theme,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Icon(
                painter = painterResource(Res.drawable.ic_arrow_down),
                contentDescription = stringResource(Res.string.agent_model),
                tint = Color.Gray,
                modifier = Modifier
                    .padding(start = 6.dp)
                    .size(10.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            models.forEach { model ->
                DropdownMenuItem(
                    onClick = {
                        onSelectModel(model.id)
                        expanded = false
                    }
                ) {
                    Text(
                        text = model.displayName,
                        color = if (model.id == selectedModel.id) AppColors.Theme else Color.Black,
                        fontWeight = if (model.id == selectedModel.id) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun AgentConversationDrawer(
    modifier: Modifier = Modifier,
    conversations: List<AgentConversation>,
    currentConversationId: String,
    onCreateConversation: () -> Unit,
    onOpenConversation: (String) -> Unit
) {
    Column(
        modifier = modifier
            .width(304.dp)
            .fillMaxHeight()
            .background(Color.White)
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AppColors.Theme)
                .clickable(onClick = onCreateConversation)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_add),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = stringResource(Res.string.agent_new_chat),
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            conversations.forEach { conversation ->
                ConversationHistoryItem(
                    conversation = conversation,
                    selected = conversation.id == currentConversationId,
                    onClick = {
                        onOpenConversation(conversation.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun ConversationHistoryItem(
    conversation: AgentConversation,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) AppColors.Theme.copy(alpha = 0.12f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = conversation.title,
            color = if (selected) AppColors.Theme else Color.Black,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ChatContent(messages: List<AgentMessage>, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    LaunchedEffect(messages.size) {
        withFrameNanos { }
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        messages.forEach { message ->
            MessageBubble(
                text = message.content,
                isUser = message.role == AgentRole.User
            )
        }
    }
}

@Composable
private fun MessageBubble(text: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(if (isUser) AppColors.Theme else Color.White)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = text,
                color = if (isUser) Color.White else Color.Black,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.Background)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(start = 14.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = {
                    Text(text = stringResource(Res.string.agent_message_hint))
                },
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    backgroundColor = Color.Transparent
                )
            )

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (enabled && value.isNotBlank()) AppColors.Theme else Color.LightGray)
                    .clickable(enabled = enabled && value.isNotBlank()) {
                        onSend()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_send),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
