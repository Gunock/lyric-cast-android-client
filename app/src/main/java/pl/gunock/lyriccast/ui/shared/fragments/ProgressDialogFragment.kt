/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 22:40
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 22:38
 */

package pl.gunock.lyriccast.ui.shared.fragments

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


class ProgressDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "ProgressDialogFragment"
    }

    val messageResourceId = MutableLiveData(0)
    val message = MutableLiveData("")

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

        mBinding.tvProgressMessage.text = message.value!!
        mBinding.btnProgressOk.visibility = View.GONE
        mBinding.btnProgressOk.setOnClickListener { dismiss() }

        message.observe(viewLifecycleOwner) { mBinding.tvProgressMessage.text = it }
        messageResourceId.observe(viewLifecycleOwner) { message.postValue(getString(it)) }
    }

    fun setMessage(stringResourceId: Int) {
        message.postValue(getString(stringResourceId))
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