/*
 * Created by Tomasz Kiljanczyk on 4/8/21 1:47 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/8/21 1:41 PM
 */

package pl.gunock.lyriccast.fragments.dialogs

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ProgressBar
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
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mOkButton: Button

    private var mDefaultTextColor: Int = Int.MIN_VALUE
    private var mDefaultProgressColor: Int = Int.MIN_VALUE
    private var mErrorProgressColor: Int = Int.MIN_VALUE

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

        mDefaultTextColor = requireContext().getColor(R.color.dialog_button)
        mDefaultProgressColor = requireContext().getColor(R.color.indeterminate_progress_bar)
        mErrorProgressColor = requireContext().getColor(R.color.error_Indeterminate_progress_bar)

        mMessageTextView = view.findViewById(R.id.tv_progress_message)
        mProgressBar = view.findViewById(R.id.pgb_progress)
        mOkButton = view.findViewById(R.id.btn_progress_ok)

        mMessageTextView.text = message
        mOkButton.visibility = View.GONE
        mOkButton.setOnClickListener { dismiss() }

        messageLiveData.observe(this) { mMessageTextView.text = it }
    }

    fun setErrorColor(errorColor: Boolean) {
        if (errorColor) {
            mProgressBar.indeterminateTintList = ColorStateList.valueOf(mErrorProgressColor)
            mOkButton.setTextColor(mErrorProgressColor)
        } else {
            mProgressBar.indeterminateTintList = ColorStateList.valueOf(mDefaultProgressColor)
            mOkButton.setTextColor(mDefaultTextColor)
        }
    }

    fun setShowOkButton(showOkButton: Boolean) {
        if (showOkButton) {
            mOkButton.visibility = View.VISIBLE
        } else {
            mOkButton.visibility = View.GONE
        }
    }

}