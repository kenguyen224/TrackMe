package com.example.kenv.trackme.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.ActivityNavigator
import androidx.work.WorkManager
import com.example.kenv.trackme.R
import com.example.kenv.trackme.databinding.ActivityWorkoutRecordingBinding
import com.example.kenv.trackme.presentation.di.WorkoutRecordingComponent
import com.example.kenv.trackme.presentation.dialog.PermissionDeniedDialog
import com.example.kenv.trackme.presentation.dialog.RationaleDialog
import com.example.kenv.trackme.presentation.extensions.show
import com.example.kenv.trackme.presentation.utils.createComponent
import com.example.kenv.trackme.presentation.utils.formatMeter
import com.example.kenv.trackme.presentation.utils.formatSpeedText
import com.example.kenv.trackme.presentation.viewmodel.WorkoutRecordingViewModel
import com.example.kenv.trackme.presentation.viewmodel.WorkoutRecordingViewModel.Companion.REQUEST_CHECK_SETTINGS
import com.example.kenv.trackme.presentation.viewmodel.WorkoutRecordingViewModelFactory
import com.google.android.gms.maps.SupportMapFragment
import javax.inject.Inject

class WorkoutRecordingActivity : AppCompatActivity(),
    ActivityCompat.OnRequestPermissionsResultCallback {

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
            btnStartRecord.setOnClickListener {
                recordWorkoutViewModel.onStartRecord()
            }
            btnFinish.setOnClickListener {
                finish()
            }
            btnStopRecord.setOnClickListener {
                viewBinding.btnStopRecord.show(false)
                recordWorkoutViewModel.onStopRecording()
            }
        }
        bindViewModel()
    }

    private fun bindViewModel() = with(recordWorkoutViewModel) {
        showRecordButton.observe(this@WorkoutRecordingActivity, {
            viewBinding.btnStartRecord.show(it)
            viewBinding.btnStopRecord.show(!it)
        })
        showResult.observe(this@WorkoutRecordingActivity, {
            viewBinding.rlRecordingInfo.show(false)
            viewBinding.viewResult.rootViewResult.show(true)
            viewBinding.viewResult.tvStartTime.text = it.startTime
            viewBinding.viewResult.tvFinishTime.text = it.finishTime
            viewBinding.viewResult.tvDistance.text = it.distance
            viewBinding.viewResult.tvAvgSpeed.text = it.avgSpeed
        })
        showFinishButton.observe(this@WorkoutRecordingActivity, {
            viewBinding.btnFinish.show(true)
        })
        observeWorkout.observe(this@WorkoutRecordingActivity, {
            WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(it)
                .observe(this@WorkoutRecordingActivity, { workInfo ->
                    val time = workInfo.progress.getLong("countTime", 0)
                    Log.d("TrackMe", "count time: $time")
                    showCountingTime(time)
                    recordWorkoutViewModel.onRequestUpdateRoadEvent(time)
                })
        })
        showDistance.observe(this@WorkoutRecordingActivity, {
            viewBinding.tvCurrentDistance.text = it.formatMeter()
        })
        showSpeed.observe(this@WorkoutRecordingActivity, {
            viewBinding.tvCurrentSpeed.text = it.formatSpeedText()
        })
        requestPermission.observe(this@WorkoutRecordingActivity, {
            requestPermission(it)
        })
        permissionDeniedDialog.observe(this@WorkoutRecordingActivity, {
            PermissionDeniedDialog.newInstance().show(supportFragmentManager, DIALOG_TAG)
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 15
        private const val DIALOG_TAG = "activity-recording-dialog"
    }
}
