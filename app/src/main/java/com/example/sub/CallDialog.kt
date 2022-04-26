package com.example.sub

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.sub.session.CallSession

/**
 * A dialog that shows the phone number of a caller and lets the user accept or deny the call.
 */
class CallDialog(private val callSession: CallSession) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(callSession.remotePhoneNumber)
                .setPositiveButton("Answer",
                    { dialog, id ->
                        callSession.accept(requireContext())
                    })
                .setNegativeButton("Deny",
                    { dialog, id ->
                        callSession.deny()
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}