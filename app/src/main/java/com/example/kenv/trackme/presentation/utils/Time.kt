package com.example.kenv.trackme.presentation.utils

/**
 * Created by Kenv on 18/01/2021.
 */

private const val TIME_TEMPLATE = "%dh:%dm:%ds"

fun Long.formatTimeText(): String {
    val hours = this / 3600
    val minutesRemain = (this - hours * 3600) / 60
    val minutes = minutesRemain.takeIf { it > 0 } ?: 0
    val secondRemain = this - hours * 3600 - minutes * 60
    val seconds = secondRemain.takeIf { it > 0 } ?: 0
    return String.format(TIME_TEMPLATE, hours, minutes, seconds)
}
