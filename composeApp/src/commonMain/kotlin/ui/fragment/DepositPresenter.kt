package ui.fragment

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow

@Composable
fun DepositPresenter(actionFlow: Flow<DepositAction>): DepositState {
    var currentAmountCents by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        // For test purpose
        currentAmountCents = 1234567890L
    }

    return DepositState(currentAmountCents)
}

data class DepositState(
    val currentAmountCents: Long = 0L
) {
    fun getDisplayAmount(): String {
        return when {
            currentAmountCents < 10 -> {
                "0.0${currentAmountCents}"
            }

            currentAmountCents < 100 -> {
                "0.${currentAmountCents}"
            }

            else -> {
                val beforePoint = currentAmountCents / 100
                val afterPoint = currentAmountCents % 100
                val afterPointStr = if (afterPoint < 10) {
                    "0$afterPoint"
                } else afterPoint.toString()
                "${beforePoint}.${afterPointStr}"
            }
        }
    }
}

sealed interface DepositAction