package ui.fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import constant.TabConstants
import extension.clickableNoRipple
import global.AppColors
import hideSoftwareKeyboard
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ui.widget.BaseImmersiveScene
import ui.widget.FragmentHeader
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.agent
import zhoutools.composeapp.generated.resources.agent_message_hint
import zhoutools.composeapp.generated.resources.ic_send

@Composable
fun AgentFragment(navController: NavHostController) {
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
            FragmentHeader(
                navController = navController,
                homeTabId = TabConstants.TAB_AGENT,
                title = stringResource(Res.string.agent)
            )

            ChatContent(
                modifier = Modifier.weight(1f)
            )

            ChatInputBar()
        }
    }
}

@Composable
private fun ChatContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        MessageBubble(
            text = "Hi, I can help you organize ideas, answer questions, or draft something together.",
            isUser = false
        )

        MessageBubble(
            text = "Could you summarize today's priorities for me?",
            isUser = true
        )

        MessageBubble(
            text = "Sure. You can break today into three parts: tasks, time blocks, and items that need syncing.",
            isUser = false
        )
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
private fun ChatInputBar() {
    var text by remember { mutableStateOf("") }

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
                value = text,
                onValueChange = { text = it },
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
                    .background(AppColors.Theme),
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