package xyz.deftu.ddc.utils

import kotlin.math.pow

fun Long.calcGoal(): Int {
    val y = (this / 100).toString().length - 1
    return (this / (100 * (10.0.pow(y)))).toInt()
}

fun Int.calcMilestone(original: Long): Long {
    val y = (original / 100).toString().length - 1
    return (this * 100 * (10.0.pow(y))).toLong()
}
