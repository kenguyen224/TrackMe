package com.example.kenv.trackme.presentation.extensions

import android.location.Location
import com.google.android.gms.maps.model.LatLng

/**
 * Created by KeNV on 08,January,2021
 * VNG company,
 * HCM, Viet Nam
 */
fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)
