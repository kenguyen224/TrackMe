package com.example.kenv.trackme.presentation.activity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.navArgs
import com.example.kenv.trackme.R
import com.example.kenv.trackme.databinding.ActivityWorkoutReviewBinding
import com.example.kenv.trackme.presentation.extensions.show
import com.example.kenv.trackme.presentation.utils.formatMeter
import com.example.kenv.trackme.presentation.utils.formatSpeedText
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

/**
 * Created by KeNV on 05,January,2021
 * VNG company,
 * HCM, Viet Nam
 */
class WorkoutReviewActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var viewBinding: ActivityWorkoutReviewBinding
    private val args: WorkoutReviewActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityWorkoutReviewBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        val mapFragment: SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        viewBinding.btnClose.setOnClickListener {
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        val middlePoint = LatLng(
            (args.workoutReviewArgument.locationTracking.first().latitude + args.workoutReviewArgument.locationTracking.last().latitude) / 2,
            (args.workoutReviewArgument.locationTracking.first().longitude + args.workoutReviewArgument.locationTracking.last().longitude) / 2
        )
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(middlePoint, 14f)
        with(map) {
            animateCamera(cameraUpdate)
            addPolyline(PolylineOptions().apply {
                addAll(args.workoutReviewArgument.locationTracking)
                color(Color.BLUE)
                width(5f)
                geodesic(true)
                addMarker(
                    MarkerOptions()
                        .position(args.workoutReviewArgument.locationTracking.first())
                        .title(getString(R.string.txt_start_point))
                        .icon(BitmapDescriptorFactory.defaultMarker())
                )
                addMarker(
                    MarkerOptions()
                        .position(args.workoutReviewArgument.locationTracking.last())
                        .title(getString(R.string.txt_end_point))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                )
            })
        }
        showWorkoutInfo()
    }

    private fun showWorkoutInfo() {
        with(viewBinding.viewResult) {
            root.show(true)
            tvStartTime.text = args.workoutReviewArgument.startTime
            tvFinishTime.text = args.workoutReviewArgument.finishTime
            tvDistance.text = args.workoutReviewArgument.distance.formatMeter()
            tvAvgSpeed.text = args.workoutReviewArgument.avgSpeed.formatSpeedText()
        }
    }
}
