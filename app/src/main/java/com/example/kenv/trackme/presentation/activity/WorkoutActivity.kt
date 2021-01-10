package com.example.kenv.trackme.presentation.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.example.kenv.trackme.R
import com.example.kenv.trackme.databinding.ActivityWorkoutBinding
import com.example.kenv.trackme.presentation.di.WorkoutComponent
import com.example.kenv.trackme.presentation.utils.createComponent

class WorkoutActivity : AppCompatActivity(), WorkoutComponentContract {

    lateinit var component: WorkoutComponent

    private lateinit var viewBinding: ActivityWorkoutBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        createComponent()
        component.inject(this)
        super.onCreate(savedInstanceState)
        title = getString(R.string.title_activity_workout)
        viewBinding = ActivityWorkoutBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        navController = findNavController(R.id.navHostFragment)
    }

    override fun getWorkoutComponent(): WorkoutComponent = component
}
