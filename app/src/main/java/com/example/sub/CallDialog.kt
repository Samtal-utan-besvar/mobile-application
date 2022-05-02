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
class CallDialog(private val displayName: String) : DialogFragment() {

    var onAnswerFunction: (() -> Unit)? = null
    var onDenyFunction: (() -> Unit)? = null

    fun setOnAnswer(answerFunction: () -> Unit) {
        onAnswerFunction = answerFunction
    }

    fun setOnDeny(denyFunction: () -> Unit) {
        onDenyFunction = denyFunction
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(displayName)
                .setPositiveButton("Answer"
                ) { _, _ ->
                    onAnswerFunction?.invoke()
                }
                .setNegativeButton("Deny"
                ) { _, _ ->
                    onDenyFunction?.invoke()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}

/**
class CallDialog(private val callSession: CallSession) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            builder.setView(inflater.inflate(R.layout.call_alert_layout, null))
            //builder.setMessage(callSession.remotePhoneNumber)
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

}**/