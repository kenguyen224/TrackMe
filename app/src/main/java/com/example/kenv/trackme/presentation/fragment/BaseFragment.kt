package com.example.kenv.trackme.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Created by Kenv on 12/12/2020.
 */

abstract class BaseFragment: Fragment() {
    abstract fun getBindingView(inflater: LayoutInflater,
                                container: ViewGroup?,): View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = getBindingView(inflater, container)
}
