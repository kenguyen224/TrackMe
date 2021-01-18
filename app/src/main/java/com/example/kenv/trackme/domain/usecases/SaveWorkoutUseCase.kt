package com.example.kenv.trackme.domain.usecases

import com.example.kenv.trackme.data.repository.IWorkoutRepository
import com.example.kenv.trackme.domain.entity.Result
import com.example.kenv.trackme.domain.entity.WorkoutEntity
import com.example.kenv.trackme.domain.transform.toModel
import javax.inject.Inject

/**
 * Created by Kenv on 18/12/2020.
 */

class SaveWorkoutUseCase @Inject constructor(
    private val repository: IWorkoutRepository
) {
    suspend operator fun invoke(workoutEntity: WorkoutEntity): Result<Unit> = try {
        Result.Success(repository.save(workoutEntity.toModel()))
    } catch (throwable: Throwable) {
        Result.Error(throwable)
    }
}
