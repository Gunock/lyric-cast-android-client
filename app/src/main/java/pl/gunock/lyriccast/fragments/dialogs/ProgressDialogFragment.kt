/*
 * Created by Tomasz Kiljanczyk on 4/1/21 11:57 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 11:17 PM
 */

package pl.gunock.lyriccast.fragments.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import pl.gunock.lyriccast.R


class ProgressDialogFragment(private val message: String) : DialogFragment() {

    companion object {
        const val TAG = "ProgressDialogFragment"
    }

    lateinit var messageTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCanceledOnTouchOutside(false)
        return inflater.inflate(R.layout.dialog_fragment_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        messageTextView = view.findViewById(R.id.tv_progress_message)
        messageTextView.text = message
    }

    fun setMessage(newMessage: String) {
        messageTextView.text = newMessage
    }

}