package com.example.kenv.trackme.presentation.viewmodel

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.kenv.trackme.domain.coroutine.CoroutineDispatcherProvider
import com.example.kenv.trackme.domain.usecases.SaveWorkoutUseCase
import javax.inject.Inject

/**
 * Created by Kenv on 19/12/2020.
 */

@Suppress("UNCHECKED_CAST")
class WorkoutRecordingViewModelFactory @Inject constructor(
    private val activity: Activity,
    private val saveWorkoutUseCase: SaveWorkoutUseCase,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) :
    AbstractSavedStateViewModelFactory(
        activity as SavedStateRegistryOwner, Bundle.EMPTY
    ) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        require(modelClass == WorkoutRecordingViewModel::class.java) {
            "Invalid viewModel class: ${modelClass.simpleName}"
        }
        return WorkoutRecordingViewModel(
            saveWorkoutUseCase,
            activity,
            coroutineDispatcherProvider
        ) as T
    }
}
