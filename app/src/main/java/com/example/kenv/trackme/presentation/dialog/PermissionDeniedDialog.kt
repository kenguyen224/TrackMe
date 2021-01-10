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
    private var finishActivity = false
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        finishActivity =
            arguments?.getBoolean(ARGUMENT_FINISH_ACTIVITY) ?: false
        return AlertDialog.Builder(activity)
            .setMessage(R.string.location_permission_denied)
            .setPositiveButton(android.R.string.ok, null)
            .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (finishActivity) {
            Toast.makeText(
                activity, R.string.permission_required_toast,
                Toast.LENGTH_SHORT
            ).show()
            activity?.finish()
        }
    }

    companion object {
        private const val ARGUMENT_FINISH_ACTIVITY = "finish"

        /**
         * Creates a new instance of this dialog and optionally finishes the calling Activity
         * when the 'Ok' button is clicked.
         */
        @JvmStatic
        fun newInstance(finishActivity: Boolean): PermissionDeniedDialog {
            val arguments = Bundle().apply {
                putBoolean(ARGUMENT_FINISH_ACTIVITY, finishActivity)
            }
            return PermissionDeniedDialog().apply {
                this.arguments = arguments
            }
        }
    }
}
