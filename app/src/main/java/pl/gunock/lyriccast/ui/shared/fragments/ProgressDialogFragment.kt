/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 17:30
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 16:34
 */

package pl.gunock.lyriccast.ui.shared.fragments

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.DialogFragmentProgressBinding


class ProgressDialogFragment(
    @StringRes private val initialMessageResId: Int? = null
) : DialogFragment() {

    companion object {
        const val TAG = "ProgressDialogFragment"
    }

    private lateinit var binding: DialogFragmentProgressBinding

    private var defaultTextColor: Int = Int.MIN_VALUE
    private var defaultProgressColor: Int = Int.MIN_VALUE
    private var errorProgressColor: Int = Int.MIN_VALUE

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentProgressBinding.inflate(layoutInflater)

        defaultTextColor = requireContext().getColor(R.color.dialog_button)
        defaultProgressColor = requireContext().getColor(R.color.indeterminate_progress_bar)
        errorProgressColor = requireContext().getColor(R.color.error_Indeterminate_progress_bar)

        binding.btnProgressOk.visibility = View.GONE
        binding.btnProgressOk.setOnClickListener { dismiss() }

        if (initialMessageResId != null) {
            setMessage(initialMessageResId)
        }

        return MaterialAlertDialogBuilder(requireActivity()).setView(binding.root)
            .setCancelable(false).create()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    fun setMessage(@StringRes stringResourceId: Int) {
        if (stringResourceId == 0) {
            return
        }

        binding.tvProgressMessage.text = getString(stringResourceId)
    }

    fun setErrorState(isError: Boolean) {
        setErrorColor(isError)
        setShowOkButton(isError)
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