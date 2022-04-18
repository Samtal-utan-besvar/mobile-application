package com.example.sub

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.sub.session.CallSession
import kotlinx.coroutines.NonCancellable.start

class CallDialog(val callSession: CallSession) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(callSession.callerPhoneNumber)
                .setPositiveButton("Answer",
                    DialogInterface.OnClickListener { dialog, id ->
                        callSession.answer()
                    })
                .setNegativeButton("Deny",
                    DialogInterface.OnClickListener { dialog, id ->
                        callSession.deny()
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}