package com.example.kenv.trackme.presentation.arguments

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Kenv on 18/01/2021.
 */
@Parcelize
data class WorkoutPendingArgs(
    val distance: Double,
    val activeTime: Long,
    val isPause: Boolean
) : Parcelable
