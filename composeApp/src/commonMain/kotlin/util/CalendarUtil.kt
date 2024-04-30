package util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.sunday
import zhoutools.composeapp.generated.resources.monday
import zhoutools.composeapp.generated.resources.tuesday
import zhoutools.composeapp.generated.resources.wednesday
import zhoutools.composeapp.generated.resources.thursday
import zhoutools.composeapp.generated.resources.friday
import zhoutools.composeapp.generated.resources.saturday

object CalendarUtil {
    @OptIn(ExperimentalResourceApi::class)
    @Composable
    fun getWeekDays(): List<String> = listOf(
        stringResource(Res.string.sunday),
        stringResource(Res.string.monday),
        stringResource(Res.string.tuesday),
        stringResource(Res.string.wednesday),
        stringResource(Res.string.thursday),
        stringResource(Res.string.friday),
        stringResource(Res.string.saturday)
    )
}