package ui.fragment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import moe.tlaster.precompose.molecule.rememberPresenter
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.current_deposit
import zhoutools.composeapp.generated.resources.deposit
import zhoutools.composeapp.generated.resources.records

@OptIn(ExperimentalResourceApi::class)
@Composable
fun DepositFragment(navigator: Navigator) {
    val scope = rememberCoroutineScope()
    val (state, channel) = rememberPresenter(keys = listOf(scope)) { DepositPresenter(it) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = stringResource(Res.string.deposit).uppercase(),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
        )

        BigCard(state)

        Text(
            text = stringResource(Res.string.records),
            fontWeight = FontWeight.Medium,
            fontSize = 20.sp,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 24.dp, top = 24.dp)
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun BigCard(state: DepositState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.White,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(Res.string.current_deposit)
            )

            val annotatedAmountString = buildAnnotatedString {
                val splitResult = state.getDisplayAmount().split(".")

                if (splitResult.size == 2) {
                    withStyle(SpanStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold)) {
                        append(splitResult[0])
                    }
                    withStyle(SpanStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)) {
                        append("." + splitResult[1])
                    }
                }
            }
            Text(annotatedAmountString)

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}