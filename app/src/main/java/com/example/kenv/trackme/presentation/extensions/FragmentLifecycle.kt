package com.example.kenv.trackme.presentation.extensions

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

/**
 * Created by Kenv on 25/12/2020.
 */

fun <T> LiveData<T>.safeObserve(fragment: Fragment, observer: Observer<in T>) {
    observe(fragment.viewLifecycleOwner, observer)
}
