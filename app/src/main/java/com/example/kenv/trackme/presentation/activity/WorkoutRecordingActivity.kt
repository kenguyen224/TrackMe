package com.example.kenv.trackme.presentation.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.ActivityNavigator
import com.example.kenv.trackme.R
import com.example.kenv.trackme.databinding.ActivityWorkoutRecordingBinding
import com.example.kenv.trackme.presentation.arguments.WorkoutResult
import com.example.kenv.trackme.presentation.di.WorkoutRecordingComponent
import com.example.kenv.trackme.presentation.dialog.PermissionDeniedDialog
import com.example.kenv.trackme.presentation.dialog.RationaleDialog
import com.example.kenv.trackme.presentation.extensions.show
import com.example.kenv.trackme.presentation.service.LocationRecordingService
import com.example.kenv.trackme.presentation.utils.createComponent
import com.example.kenv.trackme.presentation.utils.formatMeter
import com.example.kenv.trackme.presentation.utils.formatSpeedText
import com.example.kenv.trackme.presentation.viewmodel.WorkoutRecordingViewModel
import com.example.kenv.trackme.presentation.viewmodel.WorkoutRecordingViewModel.Companion.REQUEST_CHECK_SETTINGS
import com.example.kenv.trackme.presentation.viewmodel.WorkoutRecordingViewModelFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject

class WorkoutRecordingActivity : AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback, LocationRecordingService.ClientCallback {

    private var service: LocationRecordingService? = null
    private var isBidingService = false

    // Monitors the state of the connection to the service.
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, serviceBinder: IBinder) {
            val binder: LocationRecordingService.LocalBinder =
                serviceBinder as LocationRecordingService.LocalBinder
            service = binder.getService()
            service?.initClientCallBack(this@WorkoutRecordingActivity)
            isBidingService = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service?.initClientCallBack(null)
            service = null
            isBidingService = false
        }
    }

    private lateinit var viewBinding: ActivityWorkoutRecordingBinding
    lateinit var component: WorkoutRecordingComponent
    private lateinit var activityNavigator: ActivityNavigator

    @Inject
    lateinit var workoutRecordingViewModelFactory: WorkoutRecordingViewModelFactory
    private val recordWorkoutViewModel by viewModels<WorkoutRecordingViewModel> { workoutRecordingViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        createComponent()
        component.inject(this)
        super.onCreate(savedInstanceState)
        viewBinding = ActivityWorkoutRecordingBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        activityNavigator = ActivityNavigator(this)
        val mapFragment: SupportMapFragment? =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as? SupportMapFragment
        mapFragment?.getMapAsync(recordWorkoutViewModel)
        with(viewBinding) {
            btnStartRecord.setOnClickListener { recordWorkoutViewModel.onStartRecord() }
            btnFinish.setOnClickListener { finish() }
            btnStopRecord.setOnClickListener {
                showStopRecordingView()
                service?.stopRecording()
            }
            btnPause.setOnClickListener {
                service?.onPause()
                showPauseView()
            }
            btnResume.setOnClickListener {
                service?.onResume()
                showRecordingView()
            }
        }
        bindViewModel()
    }

    private fun showRecordingView() = with(viewBinding) {
        btnStartRecord.show(false)
        btnPause.show(true)
        btnStopRecord.show(true)
        btnResume.show(false)
    }

    private fun showPauseView() = with(viewBinding) {
        btnPause.show(false)
        btnResume.show(true)
    }

    private fun showStopRecordingView() = with(viewBinding) {
        btnPause.show(false)
        btnResume.show(false)
        btnStopRecord.show(false)
    }

    private fun bindViewModel() = with(recordWorkoutViewModel) {
        showFinishButton.observe(this@WorkoutRecordingActivity, {
            viewBinding.btnFinish.show(true)
        })
        requestPermission.observe(this@WorkoutRecordingActivity, {
            requestPermission(it)
        })
        permissionDeniedDialog.observe(this@WorkoutRecordingActivity, {
            PermissionDeniedDialog.newInstance().show(supportFragmentManager, DIALOG_TAG)
        })
        startService.observe(this@WorkoutRecordingActivity, {
            service?.startRecording()
            showRecordingView()
        })
    }

    private fun requestPermission(permissions: List<String>) {
        if (shouldShowRationaleDialog(permissions)) {
            RationaleDialog.newInstance(LOCATION_PERMISSION_REQUEST_CODE, permissions)
                .show(supportFragmentManager, DIALOG_TAG)
        } else {
            ActivityCompat.requestPermissions(
                this@WorkoutRecordingActivity,
                permissions.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun shouldShowRationaleDialog(permissions: List<String>) = permissions.indexOfFirst {
        ActivityCompat.shouldShowRequestPermissionRationale(
            this@WorkoutRecordingActivity,
            it
        )
    } > -1

    private fun showCountingTime(timeSecond: Long) {
        val hours = timeSecond / 3600
        val minutesRemain = (timeSecond - hours * 3600) / 60
        val minutes = minutesRemain.takeIf { it > 0 } ?: 0
        val secondRemain = timeSecond - hours * 3600 - minutes * 60
        val seconds = secondRemain.takeIf { it > 0 } ?: 0
        viewBinding.tvCurrentTime.text =
            getString(R.string.counting_time_template, hours, minutes, seconds)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            recordWorkoutViewModel.onRequestLocationSettingResult(resultCode)
        }
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            recordWorkoutViewModel.onRequestPermissionsResult(permissions, grantResults)
        }
    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        bindService(
            Intent(this, LocationRecordingService::class.java), serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onLocationUpdate(
        latLngs: List<LatLng>,
        speed: Float,
        distance: Double
    ) {
        recordWorkoutViewModel.drawRoad(latLngs)
        viewBinding.tvCurrentSpeed.text = (speed * 3.6f).formatSpeedText()
        viewBinding.tvCurrentDistance.text = distance.formatMeter()
    }

    override fun onTimeChange(seconds: Long) = runOnUiThread { showCountingTime(seconds) }

    override fun showResult(workoutResult: WorkoutResult) {
        with(viewBinding.viewResult) {
            viewBinding.rlRecordingInfo.show(false)
            rootViewResult.show(true)
            tvStartTime.text = workoutResult.startTime
            tvFinishTime.text = workoutResult.finishTime
            tvDistance.text = workoutResult.distance.formatMeter()
            tvAvgSpeed.text = workoutResult.avgSpeed.formatSpeedText()
        }
        recordWorkoutViewModel.onFinishWorkout(workoutResult)
    }

    override fun markStartPosition(position: LatLng) {
        recordWorkoutViewModel.markStartPosition(position)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 15
        private const val DIALOG_TAG = "activity-recording-dialog"
    }
}
