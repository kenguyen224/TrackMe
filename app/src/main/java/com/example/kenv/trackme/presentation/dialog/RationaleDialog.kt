package com.example.kenv.trackme.presentation.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.example.kenv.trackme.R

/**
 * Created by Kenv on 18/12/2020.
 */
/**
 * A dialog that explains the use of the location permission and requests the necessary
 * permission.
 *
 * The activity should implement
 * [androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback]
 * to handle permit or denial of this permission request.
 */
class RationaleDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val requestCode =
            arguments?.getInt(ARGUMENT_PERMISSION_REQUEST_CODE) ?: 0
        val permission = arguments?.getStringArray(ARG_PERMISSION_LIST)
            ?: throw IllegalArgumentException("[TrackMe] Invalid permission request rationale dialog")
        return AlertDialog.Builder(activity)
            .setMessage(R.string.permission_rationale_location)
            .setPositiveButton(android.R.string.ok) { _, _ -> // After click on Ok, request the permission.
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    permission,
                    requestCode
                )
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    companion object {
        private const val ARGUMENT_PERMISSION_REQUEST_CODE = "requestCode"
        const val ARG_PERMISSION_LIST = "list_permission"
        fun newInstance(requestCode: Int, permission: List<String>): RationaleDialog {
            val arguments = Bundle().apply {
                putInt(ARGUMENT_PERMISSION_REQUEST_CODE, requestCode)
                putStringArray(ARG_PERMISSION_LIST, permission.toTypedArray())
            }
            return RationaleDialog().apply {
                this.arguments = arguments
            }
        }
    }
}
