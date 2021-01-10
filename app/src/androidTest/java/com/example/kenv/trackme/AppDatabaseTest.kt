package com.example.kenv.trackme

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.kenv.trackme.data.local.AppDatabase
import com.example.kenv.trackme.data.local.WorkoutDao
import com.example.kenv.trackme.data.model.WorkoutModel
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by Kenv on 19/12/2020.
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var workoutDao: WorkoutDao
    private lateinit var db: AppDatabase

    @Before
    fun init() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()
        workoutDao = db.workoutDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsert() {
        val model = WorkoutModel(1000, 10000, "test", "")
        workoutDao.insert(model)
        val expect = listOf(model)
        val actual = workoutDao.getAll()
        Assert.assertEquals(expect, actual)
    }
}
