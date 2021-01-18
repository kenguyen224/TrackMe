package com.example.kenv.trackme.presentation.viewmodel

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.example.kenv.trackme.domain.coroutine.CoroutineDispatcherProvider
import com.example.kenv.trackme.domain.entity.WorkoutEntity
import com.example.kenv.trackme.domain.transform.toLatLngModel
import com.example.kenv.trackme.domain.usecases.SaveWorkoutUseCase
import com.example.kenv.trackme.presentation.arguments.WorkoutResult
import com.example.kenv.trackme.presentation.extensions.toLatLng
import com.example.kenv.trackme.presentation.utils.PermissionUtils
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Kenv on 19/12/2020.
 */

class WorkoutRecordingViewModel(
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val activity: Activity,
    private val dispatcher: CoroutineDispatcherProvider
) : ViewModel(), GoogleMap.SnapshotReadyCallback,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener,
    OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var isLocationPermissionDenied: Boolean = true

    private var _showFinishButton = MutableLiveData<Unit>()
    val showFinishButton: LiveData<Unit> = _showFinishButton

    private var _requestPermission = MutableLiveData<List<String>>()
    val requestPermission: LiveData<List<String>> = _requestPermission

    private var _permissionDeniedDialog = MutableLiveData<Unit>()
    val permissionDeniedDialog: LiveData<Unit> = _permissionDeniedDialog

    private var _startService = MutableLiveData<Unit>()
    val startService: LiveData<Unit> = _startService

    private lateinit var workoutResult: WorkoutResult

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        with(mMap) {
            setOnMyLocationButtonClickListener(this@WorkoutRecordingViewModel)
            setOnMyLocationClickListener(this@WorkoutRecordingViewModel)
        }
        _requestPermission.value = NECESSARY_PERMISSION
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(activity, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        return false
    }

    fun onStartRecord() {
        if (!isLocationPermissionDenied) {
            startRecordingService()
        } else {
            requestCurrentLocation()
        }
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation() {
        if (!::mMap.isInitialized) return
        if (!isLocationPermissionDenied) {
            mMap.isMyLocationEnabled = true
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            _requestPermission.value = NECESSARY_PERMISSION
        }
    }

    private fun takeScreenShot() = viewModelScope.launch(dispatcher.io) {
        delay(2000)//wait camera for animating
        mMap.snapshot(this@WorkoutRecordingViewModel)
    }

    override fun onSnapshotReady(bitMap: Bitmap) {
        val fileName = IMAGE_FILE_TEMPLATE.format(System.currentTimeMillis().toString())
        val rotateMatrix = Matrix()
        rotateMatrix.postRotate(90F)
        val rotatedBitmap = if (bitMap.width >= bitMap.height) {
            Bitmap.createBitmap(
                bitMap,
                bitMap.width / 2 - bitMap.height / 2,
                0,
                bitMap.height,
                bitMap.height
            )
        } else {
            Bitmap.createBitmap(
                bitMap,
                0,
                bitMap.height / 2 - bitMap.width / 2,
                bitMap.width,
                bitMap.width
            )
        }
        try {
            val fOut = activity.openFileOutput(fileName, Context.MODE_PRIVATE)
            // Write the string to the file
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY_COMPRESS_IMAGE, fOut)
            fOut.flush()
            fOut.close()
            saveWorkout(activity.getFileStreamPath(fileName).toString())
        } catch (e: IOException) {
            Log.d("TrackMe", e.message ?: "")
        }
    }

    private fun saveWorkout(filePath: String) = viewModelScope.launch(dispatcher.io) {
        saveWorkoutUseCase(
            WorkoutEntity(
                workoutResult.startTime,
                workoutResult.finishTime,
                filePath,
                workoutResult.distance,
                workoutResult.trackingLocation.toLatLngModel(),
                workoutResult.avgSpeed,
                workoutResult.activeTime
            )
        )
        _showFinishButton.postValue(Unit)
    }

    fun onRequestPermissionsResult(
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (PermissionUtils.isPermissionGranted(permissions, grantResults, NECESSARY_PERMISSION)) {
            isLocationPermissionDenied = false
            requestCurrentLocation()
        } else {
            isLocationPermissionDenied = true
            _permissionDeniedDialog.value = Unit
        }
    }

    private fun startRecordingService() {
        if (!isGPSEnabled(activity)) {
            requestEnableLocationSetting()
            return
        }
        _startService.value = Unit
    }

    fun markStartPosition(position: LatLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
        mMap.addMarker(MarkerOptions().position(position))
    }

    fun onFinishWorkout(result: WorkoutResult) {
        workoutResult = result
        val locations = result.trackingLocation
        val middlePoint = LatLng(
            (locations.first().latitude + locations.last().latitude) / 2,
            (locations.first().longitude + locations.last().longitude) / 2
        )
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(middlePoint, 14f)
        mMap.animateCamera(cameraUpdate)
        takeScreenShot()
    }

    private fun requestEnableLocationSetting() {
        LocationRequest.create()?.apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }?.let {
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(it)
            val client: SettingsClient = LocationServices.getSettingsClient(activity)
            client.checkLocationSettings(builder.build()).apply {
                addOnSuccessListener {
                    startRecordingService()
                }
                addOnFailureListener { exception ->
                    if (exception is ResolvableApiException) {
                        try {
                            exception.startResolutionForResult(
                                activity,
                                REQUEST_CHECK_SETTINGS
                            )
                        } catch (sendEx: IntentSender.SendIntentException) {
                            Toast.makeText(
                                activity,
                                "Cannot enable location setting",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    fun drawRoad(positions: List<LatLng>) {
        if (positions.isNotEmpty()) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(positions.last(), 15f))
        }
        if (positions.size <= 2) {
            mMap.addPolyline(PolylineOptions().apply {
                addAll(positions)
                color(Color.BLUE)
                width(5f)
                geodesic(true)
            })
        } else {
            mMap.addPolyline(PolylineOptions().apply {
                add(positions[positions.lastIndex - 1], positions.last())
                color(Color.BLUE)
                width(5f)
                geodesic(true)
            })
        }
    }

    private fun isGPSEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onMyLocationClick(p0: Location) {
        val currentLng = p0.toLatLng()
        mMap.addMarker(MarkerOptions().position(currentLng).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLng))
        Toast.makeText(activity, "Current location:\n$p0", Toast.LENGTH_LONG).show()
    }

    fun onRequestLocationSettingResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            startRecordingService()
        }
    }

    companion object {
        private const val QUALITY_COMPRESS_IMAGE = 100
        private const val IMAGE_FILE_TEMPLATE = "%s.jpeg"
        const val REQUEST_CHECK_SETTINGS = 2
        private val NECESSARY_PERMISSION = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            listOf(ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION)
        } else {
            listOf(ACCESS_FINE_LOCATION)
        }
    }
}
