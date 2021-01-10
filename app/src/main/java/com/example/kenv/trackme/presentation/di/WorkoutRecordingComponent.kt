package com.example.kenv.trackme.presentation.di

import android.app.Activity
import com.example.kenv.trackme.presentation.activity.WorkoutRecordingActivity
import com.example.kenv.trackme.presentation.di.module.WorkoutModule
import com.example.kenv.trackme.presentation.di.scope.WorkoutRecordingScope
import dagger.BindsInstance
import dagger.Component

/**
 * Created by Kenv on 22/12/2020.
 */
@Component(
    dependencies = [ApplicationComponent::class],
    modules = [WorkoutModule::class]
)
@WorkoutRecordingScope
interface WorkoutRecordingComponent {

    fun inject(activity: WorkoutRecordingActivity)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance activity: Activity,
            appComponent: ApplicationComponent
        ): WorkoutRecordingComponent
    }
}
