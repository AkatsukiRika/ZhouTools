package model.records

const val GOAL_TYPE_TIME = 0

const val GOAL_TYPE_DEPOSIT = 1

data class Goal(
    val type: Int,
    val currentValue: Long = 0L,
    val goalValue: Long = 0L
) {
    fun getProgress(): Float {
        return currentValue.toFloat() / goalValue
    }
}