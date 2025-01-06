package util

import androidx.compose.runtime.Composable
import extension.getYear
import extension.toTwoDigits
import helper.NetworkHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.getString
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
    const val NOT_HOLIDAY = 0
    const val DAY_OFF = 1
    const val WORK_DAY = 2

    const val KEY_IS_OFF_DAY = "isOffDay"

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val holidayMap = MutableStateFlow(mapOf<Int, JsonObject>())

    init {
        coroutineScope.launch(Dispatchers.IO) {
            fetchHolidayMap()
        }
    }

    /**
     * @param year null for current year
     */
    suspend fun fetchHolidayMap(year: Int? = null) {
        val currentYear = TimeUtil.currentTimeMillis().getYear()
        val useYear = year ?: currentYear
        val holidays = NetworkHelper.getHolidays(useYear)
        if (holidays != null) {
            holidayMap.update {
                it + (useYear to holidays)
            }
        }
    }

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

    suspend fun getMonthNamesNonComposable(): List<String> = listOf(
        getString(Res.string.january),
        getString(Res.string.february),
        getString(Res.string.march),
        getString(Res.string.april),
        getString(Res.string.may),
        getString(Res.string.june),
        getString(Res.string.july),
        getString(Res.string.august),
        getString(Res.string.september),
        getString(Res.string.october),
        getString(Res.string.november),
        getString(Res.string.december),
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

    fun isHoliday(year: Int, month: Int, day: Int): Int {
        runCatching {
            val dateStr = "${year}-${month.toTwoDigits()}-${day.toTwoDigits()}"
            val holidays = holidayMap.value[year] ?: return NOT_HOLIDAY
            val holiday = holidays[dateStr]?.jsonObject ?: return NOT_HOLIDAY
            val isOffDay = holiday[KEY_IS_OFF_DAY]?.jsonPrimitive?.booleanOrNull
            return if (isOffDay == true) DAY_OFF else WORK_DAY
        }.onFailure {
            it.printStackTrace()
        }
        return NOT_HOLIDAY
    }

    fun getHolidayMap(): StateFlow<Map<Int, JsonObject>> = holidayMap
}