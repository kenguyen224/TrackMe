package com.example.kenv.trackme.presentation.utils

/**
 * Created by Kenv on 03/01/2021.
 */
private const val DOUBLE_TEMPLATE = "%.0fm"
private const val SPEED_TEMPLATE = "%.0f Km/h"

fun Double.formatMeter(): String = DOUBLE_TEMPLATE.format(this)

fun Float.formatSpeedText(): String = SPEED_TEMPLATE.format(this)
