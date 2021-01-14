// Copyright 2020 Google LLC
package com.example.kenv.trackme.presentation.utils

import android.content.pm.PackageManager

/**
 * Utility class for access to runtime permissions.
 */
object PermissionUtils {

    /**
     * Checks if the result contains a [PackageManager.PERMISSION_GRANTED] result for a
     * permission from a runtime permissions request.
     *
     * @see androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
     */
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
