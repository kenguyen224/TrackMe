package com.example.kenv.trackme.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kenv.trackme.data.model.WorkoutModel
import kotlinx.coroutines.flow.Flow

/**
 * Created by Kenv on 19/12/2020.
 */
@Dao
interface WorkoutDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(workout: WorkoutModel)

    @Query("SELECT * FROM workout")
    fun getAll(): Flow<List<WorkoutModel>>
}
