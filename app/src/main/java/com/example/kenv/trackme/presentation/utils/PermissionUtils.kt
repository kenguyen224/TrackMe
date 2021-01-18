// Copyright 2020 Google LLC
package com.example.kenv.trackme.presentation.utils

import android.content.pm.PackageManager

object PermissionUtils {

    fun isPermissionGranted(
        grantPermissions: Array<String>,
        grantResults: IntArray,
        permissions: List<String>
    ): Boolean =
        permissions.find {
            val index = grantPermissions.toList().indexOf(it)
            return@find if (index < 0 || index >= grantResults.size) {
                true
            } else {
                grantResults[index] != PackageManager.PERMISSION_GRANTED
            }
        } == null
}
