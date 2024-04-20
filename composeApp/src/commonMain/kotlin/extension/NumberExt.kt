package extension

import kotlin.math.pow
import kotlin.math.round

fun Float.roundToDecimalPlaces(n: Int): String {
    val multiplier = 10.0.pow(n.toDouble())
    val roundedNumber = round(this * multiplier) / multiplier
    val numberString = roundedNumber.toString()

    val decimalPointIndex = numberString.indexOf('.')
    return if (decimalPointIndex == -1) {
        "${numberString}.${"0".repeat(n)}"
    } else {
        val existingDecimalCount = numberString.length - decimalPointIndex - 1
        if (existingDecimalCount < n) {
            numberString + "0".repeat(n - existingDecimalCount)
        } else {
            numberString
        }
    }
}