package com.example.kenv.trackme.domain.entity

import com.example.kenv.trackme.data.model.LatLngModel

/**
 * Created by Kenv on 18/12/2020.
 */

data class WorkoutEntity(
    val startTime: String,
    val finishTime: String,
    val screenShot: String,
    val distance: Double,
    val trackingLocation: List<LatLngModel>,
    val avgSpeed: Float,
    val activeTime: Long
)
