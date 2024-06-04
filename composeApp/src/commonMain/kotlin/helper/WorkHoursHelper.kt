package helper

object WorkHoursHelper {
    val workingHoursMap = mutableMapOf(
        "4h" to 4f,
        "4.5h" to 4.5f,
        "5h" to 5f,
        "5.5h" to 5.5f,
        "6h" to 6f,
        "6.5h" to 6.5f,
        "7h" to 7f,
        "7.5h" to 7.5f,
        "8h" to 8f,
        "8.5h" to 8.5f,
        "9h" to 9f,
        "9.5h" to 9.5f,
        "10h" to 10f,
        "10.5h" to 10.5f,
        "11h" to 11f,
        "11.5h" to 11.5f,
        "12h" to 12f,
        "12.5h" to 12.5f,
        "13h" to 13f,
        "13.5h" to 13.5f,
        "14h" to 14f,
        "14.5h" to 14.5f,
        "15h" to 15f,
        "15.5h" to 15.5f,
        "16h" to 16f
    )

    val overtimeHoursMap = mutableMapOf(
        "0.5h" to 0.5f,
        "1h" to 1f,
        "1.5h" to 1.5f,
        "2h" to 2f,
        "2.5h" to 2.5f,
        "3h" to 3f,
        "3.5h" to 3.5f,
        "4h" to 4f
    )

    fun getWorkingHourString(hours: Float): String? {
        return workingHoursMap.entries.firstOrNull { it.value == hours }?.key
    }

    fun getOvertimeHourString(hours: Float): String? {
        return overtimeHoursMap.entries.firstOrNull { it.value == hours }?.key
    }
}