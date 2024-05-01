package util

import androidx.compose.runtime.Composable
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import zhoutools.composeapp.generated.resources.Res
import zhoutools.composeapp.generated.resources.april
import zhoutools.composeapp.generated.resources.august
import zhoutools.composeapp.generated.resources.december
import zhoutools.composeapp.generated.resources.february
import zhoutools.composeapp.generated.resources.sunday
import zhoutools.composeapp.generated.resources.monday
import zhoutools.composeapp.generated.resources.tuesday
import zhoutools.composeapp.generated.resources.wednesday
import zhoutools.composeapp.generated.resources.thursday
import zhoutools.composeapp.generated.resources.friday
import zhoutools.composeapp.generated.resources.january
import zhoutools.composeapp.generated.resources.july
import zhoutools.composeapp.generated.resources.june
import zhoutools.composeapp.generated.resources.march
import zhoutools.composeapp.generated.resources.may
import zhoutools.composeapp.generated.resources.november
import zhoutools.composeapp.generated.resources.october
import zhoutools.composeapp.generated.resources.saturday
import zhoutools.composeapp.generated.resources.september
import kotlin.math.abs

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

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    fun getMonthNames(): List<String> = listOf(
        stringResource(Res.string.january),
        stringResource(Res.string.february),
        stringResource(Res.string.march),
        stringResource(Res.string.april),
        stringResource(Res.string.may),
        stringResource(Res.string.june),
        stringResource(Res.string.july),
        stringResource(Res.string.august),
        stringResource(Res.string.september),
        stringResource(Res.string.october),
        stringResource(Res.string.november),
        stringResource(Res.string.december),
    )

    /**
     * @return pair.first: day of month (1..31); pair.second: day of week
     */
    fun getMonthDays(year: Int, month: Int): List<Pair<Int, DayOfWeek>> {
        val monthStart = LocalDate(year, month, 1)
        val nextMonthStart = monthStart.plus(1, DateTimeUnit.MonthBased(1))
        val daysInMonth = abs(monthStart.daysUntil(nextMonthStart))
        val resultList = mutableListOf<Pair<Int, DayOfWeek>>()
        for (day in 1..daysInMonth) {
            val date = LocalDate(year, month, day)
            resultList.add(Pair(date.dayOfMonth, date.dayOfWeek))
        }
        return resultList
    }
}