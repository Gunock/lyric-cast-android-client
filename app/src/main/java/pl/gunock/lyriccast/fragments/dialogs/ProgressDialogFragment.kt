/*
 * Created by Tomasz Kiljanczyk on 14/05/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 14/05/2021, 00:06
 */

package pl.gunock.lyriccast.fragments.dialogs

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.DialogFragmentProgressBinding


class ProgressDialogFragment(messageText: String) : DialogFragment() {

    companion object {
        const val TAG = "ProgressDialogFragment"
    }

    val messageLiveData = MutableLiveData(messageText)
    var message: String
        get() = messageLiveData.value!!
        set(value) = messageLiveData.postValue(value)

    private lateinit var mBinding: DialogFragmentProgressBinding

    private var mDefaultTextColor: Int = Int.MIN_VALUE
    private var mDefaultProgressColor: Int = Int.MIN_VALUE
    private var mErrorProgressColor: Int = Int.MIN_VALUE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCanceledOnTouchOutside(false)

        mBinding = DialogFragmentProgressBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDefaultTextColor = requireContext().getColor(R.color.dialog_button)
        mDefaultProgressColor = requireContext().getColor(R.color.indeterminate_progress_bar)
        mErrorProgressColor = requireContext().getColor(R.color.error_Indeterminate_progress_bar)

        mBinding.tvProgressMessage.text = message
        mBinding.btnProgressOk.visibility = View.GONE
        mBinding.btnProgressOk.setOnClickListener { dismiss() }

        messageLiveData.observe(this) { mBinding.tvProgressMessage.text = it }
    }

    fun setErrorColor(errorColor: Boolean) {
        if (errorColor) {
            mBinding.pgbProgress.indeterminateTintList = ColorStateList.valueOf(mErrorProgressColor)
            mBinding.btnProgressOk.setTextColor(mErrorProgressColor)
        } else {
            mBinding.pgbProgress.indeterminateTintList =
                ColorStateList.valueOf(mDefaultProgressColor)
            mBinding.btnProgressOk.setTextColor(mDefaultTextColor)
        }
    }

    fun setShowOkButton(showOkButton: Boolean) {
        if (showOkButton) {
            mBinding.btnProgressOk.visibility = View.VISIBLE
        } else {
            mBinding.btnProgressOk.visibility = View.GONE
        }
    }

}