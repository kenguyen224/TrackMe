package com.example.kenv.trackme.presentation.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.kenv.trackme.R
import com.example.kenv.trackme.presentation.arguments.WorkoutResult
import com.example.kenv.trackme.presentation.extensions.toLatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/**
 * Created by Kenv on 11/01/2021.
 */

class LocationRecordingService : Service() {
    /**
     * Class used for the client Binder.  Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    class LocalBinder : Binder() {
        fun getService(): LocationRecordingService? = getInstance()
    }

    private val serviceBinder: IBinder = LocalBinder()

    private var mChangingConfiguration = false

    private lateinit var mNotificationManager: NotificationManager
    private lateinit var mServiceHandler: Handler
    private lateinit var mLocationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var clientCallBack: ClientCallback? = null

    private var currentLocation: Location? = null
    private val trackingLocation: MutableList<LatLng> = mutableListOf()
    private var distance: Double = 0.0
    private lateinit var startTime: Date
    private var timer: Timer = Timer()
    private var seconds: AtomicLong = AtomicLong(0)
    private var isPause: AtomicBoolean = AtomicBoolean(false)

    private val mLocationRequest: LocationRequest = LocationRequest().apply {
        interval = UPDATE_INTERVAL_IN_MILLISECONDS
        fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onCreate() {
        INSTANCE = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }
        getLastLocation()
        val handlerThread = HandlerThread(TAG)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)
            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("TrackMe", "Service started")
        trackingLocation.clear()
        currentLocation?.let {
            trackingLocation.add(it.toLatLng())
            clientCallBack?.markStartPosition(it.toLatLng())
        }
        startTime = Calendar.getInstance().time
        seconds.set(0)
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (isPause.get()) {
                    return
                }
                clientCallBack?.onTimeChange(seconds.incrementAndGet())
            }
        }, 1000, 1000)
        val startedFromNotification = intent.getBooleanExtra(
            EXTRA_STARTED_FROM_NOTIFICATION,
            false
        )

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            stopRecording()
            stopSelf()
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d("TrackMe", "in onBind()")
        stopForeground(true)
        mChangingConfiguration = false
        return serviceBinder
    }

    override fun onRebind(intent: Intent?) {
        Log.d("TrackMe", "in onRebind()")
        stopForeground(true)
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("TrackMe", "Last client unbound from service")
        if (!mChangingConfiguration) {
            Log.d("TrackMe", "Starting foreground service")
            startForeground(NOTIFICATION_ID, getNotification())
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }

    override fun onDestroy() {
        mServiceHandler.removeCallbacksAndMessages(null)
        INSTANCE = null
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    fun startRecording() {
        startService(
            Intent(
                applicationContext,
                LocationRecordingService::class.java
            )
        )
        try {
            fusedLocationClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            Log.e("TrackMe", "Lost location permission. Could not request updates. $unlikely")
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    fun stopRecording() {
        try {
            fusedLocationClient.removeLocationUpdates(mLocationCallback)
            timer.cancel()
            val finishTime = Calendar.getInstance().time
            val durationTime: Float = (finishTime.time - startTime.time) / 1000f //unit seconds
            val avgSpeed = (distance.toFloat() / durationTime) * 3.6f // m/s to km/h
            stopSelf()
            clientCallBack?.showResult(
                WorkoutResult(
                    startTime.toString(),
                    finishTime.toString(),
                    distance,
                    avgSpeed,
                    trackingLocation,
                    seconds.get()
                )
            )
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission. Could not remove updates. $unlikely")
        }
    }

    private fun getLocationText(): String =
        currentLocation?.let { "(" + it.latitude + ", " + it.longitude + ")" } ?: "Unknown location"

    /**
     * Returns the [NotificationCompat] used as part of the foreground service.
     */
    private fun getNotification(): Notification {
        val text: CharSequence = getLocationText()
        val priority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_HIGH
        } else {
            NotificationCompat.PRIORITY_HIGH
        }
        val builder: NotificationCompat.Builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText(text)
            .setContentTitle("TrackMe App Recording")
            .setOngoing(true)
            .setPriority(priority)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())
        return builder.build()
    }

    private fun getLastLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        currentLocation = task.result
                    } else {
                        Log.w("TrackMe", "Failed to get location.")
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e("TrackMe", "Lost location permission.$unlikely")
        }
    }

    private fun onNewLocation(location: Location) {
        if (isPause.get()) {
            return
        }
        currentLocation = location
        if (trackingLocation.isNotEmpty()) {
            distance += SphericalUtil.computeDistanceBetween(
                trackingLocation.last(),
                location.toLatLng()
            )
        }
        if (trackingLocation.isEmpty()) {
            clientCallBack?.markStartPosition(location.toLatLng())
        }
        trackingLocation.add(location.toLatLng())
        clientCallBack?.onLocationUpdate(trackingLocation, location.speed, distance)

        // Update notification content if running as a foreground service.
        if (serviceIsRunningInForeground(this)) {
            mNotificationManager.notify(NOTIFICATION_ID, getNotification())
        }
    }

    @Suppress("DEPRECATION")
    private fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(
            ACTIVITY_SERVICE
        ) as ActivityManager
        return manager.getRunningServices(Int.MAX_VALUE).indexOfFirst {
            javaClass.name == it.service.className && it.foreground
        } > -1
    }

    fun initClientCallBack(callBack: ClientCallback?) {
        this.clientCallBack = callBack
    }

    fun onPause() = isPause.set(true)

    fun onResume() = isPause.set(false)

    interface ClientCallback {
        fun onLocationUpdate(
            latLngs: List<LatLng>,
            speed: Float,
            distance: Double
        )

        fun onTimeChange(seconds: Long)
        fun showResult(workoutResult: WorkoutResult)
        fun markStartPosition(position: LatLng)
    }

    companion object {
        private val TAG = LocationRecordingService::class.java.name
        private const val CHANNEL_ID = "channel_01"
        private const val EXTRA_STARTED_FROM_NOTIFICATION = "started_from_notification"
        private const val NOTIFICATION_ID = 12345678
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2

        private var INSTANCE: LocationRecordingService? = null
        fun getInstance() = INSTANCE
    }
}
