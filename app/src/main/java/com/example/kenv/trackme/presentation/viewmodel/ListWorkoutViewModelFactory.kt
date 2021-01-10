package com.example.kenv.trackme.presentation.viewmodel

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.example.kenv.trackme.data.local.WorkoutStorage
import com.example.kenv.trackme.domain.coroutine.CoroutineDispatcherProvider
import com.example.kenv.trackme.domain.usecases.GetWorkoutsUseCase
import javax.inject.Inject

/**
 * Created by Kenv on 19/12/2020.
 */

@Suppress("UNCHECKED_CAST")
class ListWorkoutViewModelFactory @Inject constructor(
    activity: Activity,
    private val getWorkoutsUseCase: GetWorkoutsUseCase,
    private val storage: WorkoutStorage,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : AbstractSavedStateViewModelFactory(
    activity as SavedStateRegistryOwner, Bundle.EMPTY
) {
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        require(modelClass == ListWorkoutViewModel::class.java) {
            "Invalid viewModel class: ${modelClass.simpleName}"
        }
        return ListWorkoutViewModel(getWorkoutsUseCase, storage, coroutineDispatcherProvider) as T
    }
}
