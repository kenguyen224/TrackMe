package com.example.kenv.trackme.domain.entity

/**
 * Created by Kenv on 19/12/2020.
 */

sealed class Result<out T : Any> {

    class Success<out T : Any>(val data: T) : Result<T>()

    class Error(val exception: Throwable) : Result<Nothing>()
}
