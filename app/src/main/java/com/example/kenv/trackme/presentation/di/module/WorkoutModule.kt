package com.example.kenv.trackme.presentation.di.module

import com.example.kenv.trackme.data.local.DefaultWorkoutStorage
import com.example.kenv.trackme.data.local.WorkoutStorage
import com.example.kenv.trackme.data.repository.DefaultWorkoutRepository
import com.example.kenv.trackme.data.repository.WorkoutRepository
import dagger.Binds
import dagger.Module

/**
 * Created by Kenv on 19/12/2020.
 */

@Module
abstract class WorkoutModule {
    @Binds
    abstract fun bindWorkoutStorage(defaultWorkoutStorage: DefaultWorkoutStorage): WorkoutStorage

    @Binds
    abstract fun bindWorkoutRepository(defaultWorkoutRepository: DefaultWorkoutRepository): WorkoutRepository
}
