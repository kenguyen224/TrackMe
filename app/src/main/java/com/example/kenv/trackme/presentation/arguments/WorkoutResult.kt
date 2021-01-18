package com.example.kenv.trackme.presentation.arguments

import com.google.android.gms.maps.model.LatLng

/**
 * Created by Kenv on 03/01/2021.
 */

data class WorkoutResult(
    val startTime: String,
    val finishTime: String,
    val distance: Double,
    val avgSpeed: Float,
    val trackingLocation: List<LatLng>,
    val activeTime: Long
)
