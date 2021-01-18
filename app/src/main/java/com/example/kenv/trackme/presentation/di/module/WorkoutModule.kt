package com.example.kenv.trackme.presentation.di.module

import com.example.kenv.trackme.data.local.WorkoutStorage
import com.example.kenv.trackme.data.local.IWorkoutStorage
import com.example.kenv.trackme.data.repository.WorkoutRepository
import com.example.kenv.trackme.data.repository.IWorkoutRepository
import dagger.Binds
import dagger.Module

/**
 * Created by Kenv on 19/12/2020.
 */

@Module
abstract class WorkoutModule {
    @Binds
    abstract fun bindWorkoutStorage(workoutStorage: WorkoutStorage): IWorkoutStorage

    @Binds
    abstract fun bindWorkoutRepository(defaultWorkoutRepository: WorkoutRepository): IWorkoutRepository
}
