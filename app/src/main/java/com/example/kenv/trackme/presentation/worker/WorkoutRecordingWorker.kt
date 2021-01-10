package com.example.kenv.trackme.presentation.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

/**
 * Created by KeNV on 25,December,2020
 * VNG company,
 * HCM, Viet Nam
 */
class WorkoutRecordingWorker(appContext: Context, params: WorkerParameters) :
    Worker(appContext, params) {
    private val startTime = System.currentTimeMillis()
    private var isStop: Boolean = false
    override fun doWork(): Result {
        Log.d("TrackMe", "Start recording")
        while (!isStop) {
            val pendingTime = System.currentTimeMillis() - startTime
            if (pendingTime % 1000 == 0L) {
                setProgressAsync(workDataOf("countTime" to pendingTime / 1000))
            }
        }
        return Result.success()
    }

    override fun onStopped() {
        Log.d("TrackMe", "Stop record")
        isStop = true
        super.onStopped()
    }
}
