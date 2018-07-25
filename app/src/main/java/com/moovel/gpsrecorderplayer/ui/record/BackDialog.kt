package com.moovel.gpsrecorderplayer.ui.record

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.moovel.gpsrecorderplayer.R

class BackDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setTitle(R.string.back_dialog_title)
                .setMessage(R.string.back_dialog_message)
                .setNegativeButton(R.string.universal_cancel) { _, _ -> }
                .setPositiveButton(R.string.back_dialog_stop) { _, _ -> (parentFragment as? Callback)?.onStopClicked() }
                .create()
    }

    interface Callback {
        fun onStopClicked()
    }
}
