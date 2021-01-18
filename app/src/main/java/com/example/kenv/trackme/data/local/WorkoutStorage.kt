package com.example.kenv.trackme.data.local

import com.example.kenv.trackme.data.model.WorkoutModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by Kenv on 18/12/2020.
 */

class WorkoutStorage @Inject constructor(private val database: AppDatabase) :
    IWorkoutStorage {
    override fun save(model: WorkoutModel) {
        database.workoutDao().insert(model)
    }

    override fun getAll(): Flow<List<WorkoutModel>> {
        return database.workoutDao().getAll()
    }
}
