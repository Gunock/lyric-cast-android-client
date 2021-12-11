/*
 * Created by Tomasz Kiljanczyk on 12/12/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/12/2021, 00:03
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
    val isError = MutableLiveData(false)

    private lateinit var binding: DialogFragmentProgressBinding

    private var defaultTextColor: Int = Int.MIN_VALUE
    private var defaultProgressColor: Int = Int.MIN_VALUE
    private var errorProgressColor: Int = Int.MIN_VALUE

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

        defaultTextColor = requireContext().getColor(R.color.dialog_button)
        defaultProgressColor = requireContext().getColor(R.color.indeterminate_progress_bar)
        errorProgressColor = requireContext().getColor(R.color.error_Indeterminate_progress_bar)

        binding.btnProgressOk.visibility = View.GONE
        binding.btnProgressOk.setOnClickListener { dismiss() }

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

        binding.tvProgressMessage.text = getString(stringResourceId)
    }

    fun hasBinding(): Boolean {
        return this::binding.isInitialized
    }

    private fun setErrorColor(errorColor: Boolean) {
        if (errorColor) {
            binding.pgbProgress.indeterminateTintList = ColorStateList.valueOf(errorProgressColor)
            binding.btnProgressOk.setTextColor(errorProgressColor)
        } else {
            binding.pgbProgress.indeterminateTintList =
                ColorStateList.valueOf(defaultProgressColor)
            binding.btnProgressOk.setTextColor(defaultTextColor)
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