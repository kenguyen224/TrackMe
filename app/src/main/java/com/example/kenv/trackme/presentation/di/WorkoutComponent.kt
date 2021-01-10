package com.example.kenv.trackme.presentation.di

import android.app.Activity
import com.example.kenv.trackme.data.repository.WorkoutRepository
import com.example.kenv.trackme.presentation.activity.WorkoutActivity
import com.example.kenv.trackme.presentation.di.module.WorkoutModule
import com.example.kenv.trackme.presentation.di.scope.WorkoutScope
import com.example.kenv.trackme.presentation.fragment.HistoryWorkoutFragment
import dagger.BindsInstance
import dagger.Component

/**
 * Created by Kenv on 19/12/2020.
 */
@Component(
    dependencies = [ApplicationComponent::class],
    modules = [WorkoutModule::class]
)
@WorkoutScope
interface WorkoutComponent {

    val workoutRepository: WorkoutRepository

    fun inject(activity: WorkoutActivity)
    fun inject(fragment: HistoryWorkoutFragment)

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance activity: Activity,
            appComponent: ApplicationComponent
        ): WorkoutComponent
    }
}
