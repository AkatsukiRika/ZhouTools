package constant

import kotlinx.serialization.json.Json

object TabConstants {
    const val TAB_TIME_CARD = 0
    const val TAB_SCHEDULE = 1
    const val TAB_SETTINGS = 2
    const val TAB_MEMO = 3
    const val TAB_DEPOSIT = 4

    private const val KEY_TIME_CARD = "tab_time_card"
    private const val KEY_SCHEDULE = "tab_schedule"
    private const val KEY_SETTINGS = "tab_settings"
    private const val KEY_MEMO = "tab_memo"
    private const val KEY_DEPOSIT = "tab_deposit"

    val DEFAULT_HOME_TABS = listOf(
        TAB_TIME_CARD,
        TAB_SCHEDULE,
        TAB_MEMO,
        TAB_DEPOSIT,
        TAB_SETTINGS
    )

    fun parseHomeTabList(rawValue: String): List<Int> {
        val tabKeys = runCatching {
            Json.decodeFromString<List<String>>(rawValue)
        }.getOrElse {
            emptyList()
        }

        return tabKeys
            .mapNotNull(::tabIdFromKey)
            .distinct()
            .ifEmpty { DEFAULT_HOME_TABS }
    }

    private fun tabIdFromKey(key: String): Int? {
        return when (key) {
            KEY_TIME_CARD -> TAB_TIME_CARD
            KEY_SCHEDULE -> TAB_SCHEDULE
            KEY_SETTINGS -> TAB_SETTINGS
            KEY_MEMO -> TAB_MEMO
            KEY_DEPOSIT -> TAB_DEPOSIT
            else -> null
        }
    }
}