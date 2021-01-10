package com.example.kenv.trackme.domain.usecases

import com.example.kenv.trackme.data.repository.WorkoutRepository
import com.example.kenv.trackme.domain.entity.WorkoutEntity
import com.example.kenv.trackme.domain.transform.toEntity
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by Kenv on 18/12/2020.
 */

class GetWorkoutsUseCase @Inject constructor(private val workoutRepository: WorkoutRepository) {
    suspend operator fun invoke(): Flow<List<WorkoutEntity>> = workoutRepository.get()
        .map { it.toEntity() }
        .map { it.sortedByDescending { item -> item.startTime } }
}
