package ui.scene

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import extension.clickableNoRipple
import ui.widget.BaseImmersiveScene

@Composable
fun DebugScene(navController: NavHostController) {
    val viewModel = viewModel { DebugViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    BaseImmersiveScene(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) {
        DebugColumn(navController, uiState)
    }
}

@Composable
private fun DebugColumn(navController: NavHostController, uiState: DebugState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        DebugButton(onClick = {
            navController.navigateUp()
        }) {
            Text(
                text = "Back",
                modifier = Modifier.padding(horizontal = 16.dp).padding(vertical = 8.dp),
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color.Green
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "---------- Remote Config ----------",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color.Green
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "home_tab_list = ${uiState.homeTabList}",
            modifier = Modifier.align(Alignment.CenterHorizontally),
            fontFamily = FontFamily.Monospace,
            color = Color.Green
        )
    }
}

@Composable
private fun DebugButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier
        .border(1.dp, Color.Green)
        .clickableNoRipple(onClick = onClick)
    ) {
        content()
    }
}