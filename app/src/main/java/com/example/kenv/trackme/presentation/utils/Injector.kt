package com.example.kenv.trackme.presentation.utils

import com.example.kenv.AndroidApplication
import com.example.kenv.trackme.presentation.activity.WorkoutActivity
import com.example.kenv.trackme.presentation.activity.WorkoutRecordingActivity
import com.example.kenv.trackme.presentation.di.DaggerWorkoutComponent
import com.example.kenv.trackme.presentation.di.DaggerWorkoutRecordingComponent

/**
 * Created by Kenv on 19/12/2020.
 */
fun WorkoutActivity.createComponent() {
    DaggerWorkoutComponent.factory()
        .create(this, AndroidApplication.getAppComponent()).also {
            component = it
        }
}

fun WorkoutRecordingActivity.createComponent() {
    DaggerWorkoutRecordingComponent.factory()
        .create(this, AndroidApplication.getAppComponent()).also {
            component = it
        }
}
