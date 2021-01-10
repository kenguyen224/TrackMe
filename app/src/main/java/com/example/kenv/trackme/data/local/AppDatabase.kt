package com.example.kenv.trackme.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kenv.trackme.data.model.WorkoutModel

/**
 * Created by Kenv on 19/12/2020.
 */
@Database(entities = [WorkoutModel::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
}
