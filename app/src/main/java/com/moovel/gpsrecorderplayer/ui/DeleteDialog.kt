package com.moovel.gpsrecorderplayer.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.moovel.gpsrecorderplayer.R

class DeleteDialog : DialogFragment() {

    companion object {
        fun instance(@StringRes titleRes: Int): DeleteDialog {
            val dialog = DeleteDialog()
            dialog.arguments = Bundle().apply { putInt("title_res", titleRes) }
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setTitle(arguments?.getInt("title_res") ?: -1)
                .setNegativeButton(R.string.universal_cancel) { _, _ -> }
                .setPositiveButton(R.string.universal_delete) { _, _ -> (parentFragment as? Callback)?.onDelete() }
                .create()
    }

    interface Callback {
        fun onDelete()
    }
}
