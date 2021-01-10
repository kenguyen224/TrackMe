package com.example.kenv.trackme.presentation.extensions

import android.view.View

/**
 * Created by Kenv on 29/12/2020.
 */
fun View?.show(isShow: Boolean) = if (isShow) {
    this?.visibility = View.VISIBLE
} else {
    this?.visibility = View.GONE
}
