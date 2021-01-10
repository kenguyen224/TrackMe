package com.example.kenv.trackme.presentation.arguments

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize

/**
 * Created by KeNV on 05,January,2021
 * VNG company,
 * HCM, Viet Nam
 */
@Keep
@Parcelize
data class WorkoutReviewArgument(
    val locationTracking: List<LatLng>,
    val startTime: String,
    val finishTime: String,
    val distance: Double,
    val avgSpeed: Float,
): Parcelable
