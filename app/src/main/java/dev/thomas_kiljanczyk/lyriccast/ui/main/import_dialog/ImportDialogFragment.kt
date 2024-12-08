/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.main.import_dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.databinding.DialogFragmentImportBinding
import dev.thomas_kiljanczyk.lyriccast.datatransfer.enums.ImportFormat
import kotlinx.coroutines.flow.MutableStateFlow


@AndroidEntryPoint
class ImportDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "ImportDialogFragment"
    }

    private val viewModel: ImportDialogModel by activityViewModels()

    private lateinit var binding: DialogFragmentImportBinding

    val isAccepted: MutableStateFlow<Boolean> = MutableStateFlow(false)


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentImportBinding.inflate(layoutInflater)

        setupImportTypeDropdown()
        setupListeners()

        return MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_LyricCast_MaterialAlertDialog)
            .setTitle(R.string.main_activity_import_dialog_title)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.main_activity_menu_import) { _, _ -> onImport() }
            .setView(binding.root)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    private fun setupImportTypeDropdown() {
        val firstImportType = binding.dropdownImportFormat.adapter.getItem(0).toString()
        val importFormat = ImportFormat.getByName(firstImportType)

        viewModel.importFormat = importFormat
        binding.dropdownImportFormat.setText(importFormat.displayName, false)
    }

    private fun setupListeners() {
        binding.chkDeleteAll.setOnCheckedChangeListener { _, isChecked ->
            viewModel.deleteAll = isChecked
            if (isChecked) {
                binding.chkReplaceOnConflict.isChecked = false
                binding.chkReplaceOnConflict.isEnabled = false
            } else {
                binding.chkReplaceOnConflict.isEnabled = true
            }
        }
    }

    private fun onImport() {
        viewModel.deleteAll = binding.chkDeleteAll.isChecked
        viewModel.replaceOnConflict = binding.chkReplaceOnConflict.isChecked
        viewModel.importFormat =
            ImportFormat.getByName(binding.dropdownImportFormat.text.toString())
        isAccepted.value = true
        dismiss()
    }

}