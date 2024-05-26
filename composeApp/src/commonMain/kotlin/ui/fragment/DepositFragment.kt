package ui.fragment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import extension.toMoneyDisplayStr
import global.AppColors
import moe.tlaster.precompose.molecule.rememberPresenter
import moe.tlaster.precompose.navigation.Navigator
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.add_monthly_record
import zhoutools.composeapp.generated.resources.balance
import zhoutools.composeapp.generated.resources.balance_diff
import zhoutools.composeapp.generated.resources.current_deposit
import zhoutools.composeapp.generated.resources.deposit
import zhoutools.composeapp.generated.resources.extra_deposit
import zhoutools.composeapp.generated.resources.ic_add
import zhoutools.composeapp.generated.resources.monthly_income
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
                .padding(start = 24.dp, top = 24.dp, bottom = 12.dp)
        )

        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
            items(state.displayDeque) {
                MonthCard(it)
            }
        }

        AddRecordButton(onClick = {})

        Spacer(modifier = Modifier.height(16.dp))
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
                val splitResult = state.currentAmount.toMoneyDisplayStr().split(".")

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

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun MonthCard(item: DepositDisplayRecord) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        .clip(RoundedCornerShape(8.dp))
        .background(Color.White)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.monthStr,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        MonthCardDataItem(
            type = Res.string.current_deposit,
            value = item.currentAmount.toMoneyDisplayStr()
        )

        MonthCardDataItem(
            type = Res.string.monthly_income,
            value = item.monthlyIncome.toMoneyDisplayStr()
        )

        MonthCardDataItem(
            type = Res.string.balance,
            value = item.balance.toMoneyDisplayStr()
        )

        MonthCardDataItem(
            type = Res.string.extra_deposit,
            value = item.extraDeposit.toMoneyDisplayStr()
        )

        val balanceDiff = item.balanceDiff
        val balanceDiffStr = if (balanceDiff == null) {
            "N/A"
        } else if (balanceDiff > 0) {
            "+${balanceDiff.toMoneyDisplayStr()}"
        } else {
            balanceDiff.toMoneyDisplayStr()
        }
        val balanceDiffColor = if (balanceDiff != null && balanceDiff < 0) {
            AppColors.Red
        } else AppColors.DarkGreen
        MonthCardDataItem(
            type = Res.string.balance_diff,
            value = balanceDiffStr,
            valueColor = balanceDiffColor
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun MonthCardDataItem(type: StringResource, value: String, valueColor: Color? = null) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
    ) {
        Text(
            text = stringResource(type),
            color = Color.DarkGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = value,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = valueColor ?: Color.Unspecified
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun AddRecordButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.LightTheme)
            .clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = stringResource(Res.string.add_monthly_record),
            color = Color.White
        )
    }
}