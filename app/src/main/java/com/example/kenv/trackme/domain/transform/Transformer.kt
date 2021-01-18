package com.example.kenv.trackme.domain.transform

import com.example.kenv.trackme.data.model.LatLngModel
import com.example.kenv.trackme.data.model.WorkoutModel
import com.example.kenv.trackme.domain.entity.WorkoutEntity
import com.google.android.gms.maps.model.LatLng
import com.google.gson.reflect.TypeToken

/**
 * Created by Kenv on 19/12/2020.
 */
fun WorkoutModel.toEntity() = WorkoutEntity(
    startTime = this.startTime,
    finishTime = this.finishTime,
    screenShot = this.screenShot,
    distance = this.distance,
    trackingLocation = GSonUtils.fromJsonString(
        this.trackingLocation,
        object : TypeToken<List<LatLngModel>>() {}.type
    ) ?: throw IllegalArgumentException("parse location tracking error"),
    avgSpeed = this.avgSpeed,
    activeTime = this.activeTime
)

fun List<WorkoutModel>.toEntity() = this.map {
    it.toEntity()
}

fun WorkoutEntity.toModel() = WorkoutModel(
    startTime = this.startTime,
    finishTime = this.finishTime,
    screenShot = this.screenShot,
    distance = this.distance,
    trackingLocation = GSonUtils.toJsonString(this.trackingLocation),
    avgSpeed = this.avgSpeed,
    activeTime = this.activeTime
)

fun List<LatLng>.toLatLngModel() = this.map {
    LatLngModel(it.latitude, it.longitude)
}

fun List<LatLngModel>.toLatLng() = this.map {
    LatLng(it.latitude, it.longitude)
}
