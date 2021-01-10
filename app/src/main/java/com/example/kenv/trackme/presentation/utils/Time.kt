package com.example.kenv.trackme.presentation.utils

import java.util.concurrent.TimeUnit

/**
 * Created by Kenv on 03/01/2021.
 */

private const val TIME_TEMPLATE = "%02d:%02dm"

fun formatTime(milliseconds: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    val minute = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
    return TIME_TEMPLATE.format(hours, minute)
}
