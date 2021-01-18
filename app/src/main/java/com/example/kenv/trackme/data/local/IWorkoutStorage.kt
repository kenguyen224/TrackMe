package com.example.kenv.trackme.data.local

import com.example.kenv.trackme.data.model.WorkoutModel
import kotlinx.coroutines.flow.Flow

/**
 * Created by Kenv on 18/12/2020.
 */

interface IWorkoutStorage {
    fun save(model: WorkoutModel)
    fun getAll(): Flow<List<WorkoutModel>>
}
