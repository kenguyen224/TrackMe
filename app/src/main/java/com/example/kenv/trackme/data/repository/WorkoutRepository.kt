package com.example.kenv.trackme.data.repository

import com.example.kenv.trackme.data.local.IWorkoutStorage
import com.example.kenv.trackme.data.model.WorkoutModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by Kenv on 18/12/2020.
 */

class WorkoutRepository @Inject constructor(private val storage: IWorkoutStorage) :
    IWorkoutRepository {
    override suspend fun save(workout: WorkoutModel) {
        storage.save(workout)
    }

    override suspend fun get(): Flow<List<WorkoutModel>> {
        return storage.getAll()
    }
}
