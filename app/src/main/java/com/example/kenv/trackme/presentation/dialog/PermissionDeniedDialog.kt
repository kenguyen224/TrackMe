package com.example.kenv.trackme.presentation.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.kenv.trackme.R

/**
 * Created by Kenv on 18/12/2020.
 */
/**
 * A dialog that displays a permission denied message.
 */
class PermissionDeniedDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
            .setMessage(R.string.location_permission_denied)
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Toast.makeText(
            activity, R.string.permission_required_toast,
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        @JvmStatic
        fun newInstance(): PermissionDeniedDialog {
            val arguments = Bundle()
            return PermissionDeniedDialog().apply {
                this.arguments = arguments
            }
        }
    }
}
