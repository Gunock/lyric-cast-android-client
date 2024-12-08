/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:06
 */

package dev.thomas_kiljanczyk.lyriccast.ui.shared.fragments

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.databinding.DialogFragmentProgressBinding


class ProgressDialogFragment(
    @StringRes private val initialMessageResId: Int? = null
) : DialogFragment() {

    companion object {
        const val TAG = "ProgressDialogFragment"
    }

    private lateinit var binding: DialogFragmentProgressBinding

    private lateinit var defaultTextColor: ColorStateList
    private var errorProgressColor: Int = Int.MIN_VALUE

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentProgressBinding.inflate(layoutInflater)

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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        defaultTextColor = binding.btnProgressOk.textColors
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
            binding.pgbProgress.indeterminateTintList = null
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