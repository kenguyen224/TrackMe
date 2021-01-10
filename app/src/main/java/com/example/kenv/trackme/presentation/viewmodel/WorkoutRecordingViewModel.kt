package com.example.kenv.trackme.presentation.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.kenv.trackme.domain.coroutine.CoroutineDispatcherProvider
import com.example.kenv.trackme.domain.entity.WorkoutEntity
import com.example.kenv.trackme.domain.transform.toLatLngModel
import com.example.kenv.trackme.domain.usecases.SaveWorkoutUseCase
import com.example.kenv.trackme.presentation.arguments.WorkoutResult
import com.example.kenv.trackme.presentation.dialog.PermissionDeniedDialog
import com.example.kenv.trackme.presentation.extensions.toLatLng
import com.example.kenv.trackme.presentation.utils.PermissionUtils
import com.example.kenv.trackme.presentation.utils.formatMeter
import com.example.kenv.trackme.presentation.utils.formatSpeedText
import com.example.kenv.trackme.presentation.worker.WorkoutRecordingWorker
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
import com.google.maps.android.SphericalUtil
import java.io.IOException
import java.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by Kenv on 19/12/2020.
 */

class WorkoutRecordingViewModel(
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val activity: Activity,
    private val dispatcher: CoroutineDispatcherProvider
) : ViewModel(), GoogleMap.SnapshotReadyCallback, LocationListener,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener,
    OnMapReadyCallback {
    private var locationManager: LocationManager =
        activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private lateinit var mMap: GoogleMap
    private var permissionDenied: Boolean = false
    private val drawPosition: MutableList<LatLng> = mutableListOf()
    private lateinit var currentPosition: LatLng
    private lateinit var startTime: Date
    private lateinit var finishTime: Date
    private var isRecording: Boolean = false

    private var _showRecordButton = MutableLiveData<Boolean>()
    val showRecordButton: LiveData<Boolean> = _showRecordButton

    private var _showFinishButton = MutableLiveData<Unit>()
    val showFinishButton: LiveData<Unit> = _showFinishButton

    private var _showResult = MutableLiveData<WorkoutResult>()
    val showResult: LiveData<WorkoutResult> = _showResult

    private var _showDistance = MutableLiveData<Double>()
    val showDistance: LiveData<Double> = _showDistance

    private var _observeWorkout = MutableLiveData<UUID>()
    val observeWorkout: LiveData<UUID> = _observeWorkout

    private var _showSpeed = MutableLiveData<Float>()
    val showSpeed: LiveData<Float> = _showSpeed

    private var avgSpeed: Float = 0f

    fun onStopRecording() {
        isRecording = false
        //mockLocation()
        getWorkManagerInstance().cancelAllWorkByTag(RECORDING_WORKER_TAG)
        finishTime = Calendar.getInstance().time
        val durationTime: Float = (finishTime.time - startTime.time) / 1000f //unit seconds
        val distance = _showDistance.value ?: 0.0
        avgSpeed = (distance.toFloat() / durationTime) * 3.6f // m/s to km/h
        val middlePoint = LatLng(
            (currentPosition.latitude + drawPosition[0].latitude) / 2,
            (currentPosition.longitude + drawPosition[0].longitude) / 2
        )
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(middlePoint, 14f)
        mMap.animateCamera(cameraUpdate)
        takeScreenShot()
        _showResult.value = WorkoutResult(
            startTime.toString(),
            finishTime.toString(),
            _showDistance.value?.formatMeter() ?: "",
            avgSpeed.formatSpeedText()
        )
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        with(mMap) {
            setOnMyLocationButtonClickListener(this@WorkoutRecordingViewModel)
            setOnMyLocationClickListener(this@WorkoutRecordingViewModel)
        }
        requestCurrentLocation()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(activity, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        return false
    }

    fun onStartRecord() {
        if (isLocationPermissionAllow()) {
            startRecordWorkoutWorker()
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
        if (isLocationPermissionAllow()) {
            mMap.isMyLocationEnabled = true
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                INTERVAL_LOCATION_UPDATE,
                MIN_DISTANCE,
                this
            )
        } else {
            // Permission to access the location is missing. Show rationale and request permission
            PermissionUtils.requestPermission(
                activity as AppCompatActivity, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true
            )
        }
    }

    fun checkLocationPermission() {
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            PermissionDeniedDialog.newInstance(true)
                .show((activity as FragmentActivity).supportFragmentManager, "dialog")
            permissionDenied = false
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
                startTime.toString(),
                finishTime.toString(),
                filePath,
                _showDistance.value ?: 0.0,
                drawPosition.toLatLngModel(),
                avgSpeed
            )
        )
        _showFinishButton.postValue(Unit)
    }

    private fun getWorkManagerInstance() = WorkManager.getInstance(activity.applicationContext)

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }
        if (PermissionUtils.isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            requestCurrentLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    override fun onCleared() {
        WorkManager.getInstance(activity.applicationContext)
            .cancelAllWorkByTag(RECORDING_WORKER_TAG)
        super.onCleared()
    }

    private fun startRecordWorkoutWorker() {
        if (!isGPSEnabled(activity)) {
            requestEnableLocationSetting()
            return
        }
        if (!::currentPosition.isInitialized) {
            Toast.makeText(activity, "GPS is not available here", Toast.LENGTH_SHORT).show()
            return
        }
        getCurrentLocation()?.let {
            currentPosition = it.toLatLng()
        }
        isRecording = true
        startTime = Calendar.getInstance().time
        drawPosition.clear()
        drawPosition.add(currentPosition)
        mMap.addMarker(MarkerOptions().position(currentPosition))
        val recordingRequest = OneTimeWorkRequestBuilder<WorkoutRecordingWorker>()
            .addTag(RECORDING_WORKER_TAG)
            .build()
        getWorkManagerInstance().enqueue(recordingRequest)
        _observeWorkout.value = recordingRequest.id
        _showRecordButton.value = false
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
                    startRecordWorkoutWorker()
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

    fun onRequestUpdateRoadEvent(time: Long) {
        if (time % 5 == 0L) {
            requestCurrentLocation()
        }
    }

    private fun drawRoad() {
        if (drawPosition.size <= 2) {
            mMap.addPolyline(PolylineOptions().apply {
                addAll(drawPosition)
                color(Color.BLUE)
                width(5f)
                geodesic(true)
            })
        } else {
            mMap.addPolyline(PolylineOptions().apply {
                add(drawPosition[drawPosition.lastIndex - 1], drawPosition.last())
                color(Color.BLUE)
                width(5f)
                geodesic(true)
            })
        }
    }

    override fun onLocationChanged(location: Location) {
        getCurrentLocation()?.let {
            currentPosition = it.toLatLng()
            val currentDistance = _showDistance.value ?: 0.0
            val lastPosition =
                drawPosition.takeIf { item -> item.isNotEmpty() }?.last() ?: currentPosition
            _showDistance.value = currentDistance + SphericalUtil.computeDistanceBetween(
                lastPosition,
                currentPosition
            )
            _showSpeed.value = it.speed * 3.6f // m/s -> km/h
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 15f))
            locationManager.removeUpdates(this)
            drawPosition.add(currentPosition)
            drawRoad()
        }
    }

    private fun isGPSEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getCurrentLocation(): Location? {
        return if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        } else {
            null
        }
    }

    private fun isLocationPermissionAllow() = ContextCompat.checkSelfPermission(
        activity, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    override fun onMyLocationClick(p0: Location) {
        val currentLng = p0.toLatLng()
        mMap.addMarker(MarkerOptions().position(currentLng).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLng))
        Toast.makeText(activity, "Current location:\n$p0", Toast.LENGTH_LONG).show()
    }

    override fun onProviderDisabled(provider: String) {
        Toast.makeText(activity, "Disable location", Toast.LENGTH_SHORT).show()
    }

    override fun onProviderEnabled(provider: String) {
        Toast.makeText(activity, "Enable location", Toast.LENGTH_SHORT).show()
    }

    fun onRequestLocationSettingResult(resultCode: Int) {
        if (resultCode == Activity.RESULT_OK) {
            startRecordWorkoutWorker()
        }
    }

    companion object {
        private const val RECORDING_WORKER_TAG = "recording-workout"
        private const val INTERVAL_LOCATION_UPDATE: Long = 1000
        private const val MIN_DISTANCE = 1f
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val QUALITY_COMPRESS_IMAGE = 100
        private const val IMAGE_FILE_TEMPLATE = "%s.jpeg"
        const val REQUEST_CHECK_SETTINGS = 2
    }
}
