/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:33 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:32 PM
 */

package pl.gunock.lyriccast.fragments.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.R


class ProgressDialogFragment(messageText: String) : DialogFragment() {

    companion object {
        const val TAG = "ProgressDialogFragment"
    }

    val messageLiveData = MutableLiveData(messageText)
    var message: String
        get() = messageLiveData.value!!
        set(value) = messageLiveData.postValue(value)

    private lateinit var mMessageTextView: TextView

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

        mMessageTextView = view.findViewById(R.id.tv_progress_message)
        mMessageTextView.text = message

        messageLiveData.observe(this) { mMessageTextView.text = it }
    }

}