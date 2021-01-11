package com.example.kenv.trackme.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kenv.trackme.domain.coroutine.CoroutineDispatcherProvider
import com.example.kenv.trackme.domain.entity.WorkoutEntity
import com.example.kenv.trackme.domain.usecases.GetWorkoutsUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by Kenv on 19/12/2020.
 */

class ListWorkoutViewModel(
    private val getWorkoutsUseCase: GetWorkoutsUseCase,
    private val coroutineDispatcherProvider: CoroutineDispatcherProvider
) : ViewModel() {

    private val _listWorkout = MutableLiveData<List<WorkoutEntity>>()
    val listWorkout: LiveData<List<WorkoutEntity>> = _listWorkout

    init {
        getListWorkout()
    }

    fun getListWorkout() = viewModelScope.launch(coroutineDispatcherProvider.io) {
        getWorkoutsUseCase()
            .catch { throwable ->
                Log.d("TrackMe", throwable.message ?: "error get list workout")
            }
            .collect { _listWorkout.postValue(it) }
    }
}
