package com.example.kenv.trackme.data.repository

import com.example.kenv.trackme.data.model.WorkoutModel
import kotlinx.coroutines.flow.Flow

/**
 * Created by Kenv on 18/12/2020.
 */

interface IWorkoutRepository {

    suspend fun save(workout: WorkoutModel)

    suspend fun get(): Flow<List<WorkoutModel>>
}
