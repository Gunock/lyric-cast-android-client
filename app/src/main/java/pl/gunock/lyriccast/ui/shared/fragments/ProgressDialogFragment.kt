/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 18:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 15:11
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
    val isError = MutableLiveData(false)

    private lateinit var binding: DialogFragmentProgressBinding

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

        binding = DialogFragmentProgressBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mDefaultTextColor = requireContext().getColor(R.color.dialog_button)
        mDefaultProgressColor = requireContext().getColor(R.color.indeterminate_progress_bar)
        mErrorProgressColor = requireContext().getColor(R.color.error_Indeterminate_progress_bar)

        binding.tvProgressMessage.text = message.value!!
        binding.btnProgressOk.visibility = View.GONE
        binding.btnProgressOk.setOnClickListener { dismiss() }

        message.observe(viewLifecycleOwner) { binding.tvProgressMessage.text = it }
        messageResourceId.observe(viewLifecycleOwner) { setMessage(it) }
        isError.observe(viewLifecycleOwner) {
            setErrorColor(it)
            setShowOkButton(it)
        }
    }

    fun setMessage(stringResourceId: Int) {
        if (stringResourceId == 0) {
            return
        }

        message.postValue(getString(stringResourceId))
    }

    private fun setErrorColor(errorColor: Boolean) {
        if (errorColor) {
            binding.pgbProgress.indeterminateTintList = ColorStateList.valueOf(mErrorProgressColor)
            binding.btnProgressOk.setTextColor(mErrorProgressColor)
        } else {
            binding.pgbProgress.indeterminateTintList =
                ColorStateList.valueOf(mDefaultProgressColor)
            binding.btnProgressOk.setTextColor(mDefaultTextColor)
        }
    }

    private fun setShowOkButton(showOkButton: Boolean) {
        if (showOkButton) {
            binding.btnProgressOk.visibility = View.VISIBLE
        } else {
            binding.btnProgressOk.visibility = View.GONE
        }
    }

}