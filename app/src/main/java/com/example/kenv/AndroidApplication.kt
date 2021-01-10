package com.example.kenv

import android.app.Application
import android.util.Log
import com.example.kenv.trackme.presentation.di.ApplicationComponent

/**
 * Created by Kenv on 12/12/2020.
 */

class AndroidApplication : Application() {

    private lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("TrackMe", "Create android application")
        appComponent = ApplicationComponent.create(this)
        appComponent.inject(this)
    }

    companion object {
        private lateinit var instance: AndroidApplication
        fun getAppComponent() = instance.appComponent
    }
}
