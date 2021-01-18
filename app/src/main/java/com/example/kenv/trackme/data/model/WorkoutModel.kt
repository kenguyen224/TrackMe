package com.example.kenv.trackme.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * Created by Kenv on 18/12/2020.
 */
@Entity(
    primaryKeys = ["start_time", "finish_time"],
    tableName = "workout"
)
data class WorkoutModel(
    @ColumnInfo(name = "start_time") val startTime: String,
    @ColumnInfo(name = "finish_time") val finishTime: String,
    @ColumnInfo(name = "screen_shot") val screenShot: String,
    @ColumnInfo(name = "distance") val distance: Double,
    @ColumnInfo(name = "latlng") val trackingLocation: String,
    @ColumnInfo(name = "avgSpeed") val avgSpeed: Float,
    @ColumnInfo(name = "active_time") val activeTime: Long
)
