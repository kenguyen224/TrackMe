package com.example.kenv.trackme.domain.coroutine

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Created by Kenv on 24/12/2020.
 */

data class CoroutineDispatcherProvider(
    val io: CoroutineDispatcher,
    val main: CoroutineDispatcher,
    val compute: CoroutineDispatcher
)
