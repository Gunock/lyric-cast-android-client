/*
 * Created by Tomasz Kiljanczyk on 4/4/21 2:00 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 1:50 AM
 */

package pl.gunock.lyriccast.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.dataimport.enums.ImportFormat
import pl.gunock.lyriccast.fragments.viewholders.ImportDialogViewModel


class ImportDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "ImportDialogFragment"
    }

    private lateinit var importFormatSpinner: Spinner
    private lateinit var deleteAllCheckBox: CheckBox
    private lateinit var replaceOnConflictCheckBox: CheckBox

    private lateinit var importDialogViewModel: ImportDialogViewModel


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        importDialogViewModel =
            ViewModelProvider(requireActivity()).get(ImportDialogViewModel::class.java)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setTitle(getString(R.string.import_dialog_title))

        return inflater.inflate(R.layout.dialog_fragment_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        importFormatSpinner = view.findViewById(R.id.spn_import_format)
        deleteAllCheckBox = view.findViewById(R.id.chk_delete_all)
        replaceOnConflictCheckBox = view.findViewById(R.id.chk_replace_on_conflict)

        setupColorSpinner()
        setupListeners(view)
        importDialogViewModel.importFormat =
            ImportFormat.getByName(importFormatSpinner.selectedItem as String)
    }

    private fun setupColorSpinner() {
        importFormatSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.import_formats,
            android.R.layout.simple_list_item_1
        )
    }

    private fun setupListeners(view: View) {
        deleteAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
            importDialogViewModel.deleteAll = isChecked
            if (isChecked) {
                replaceOnConflictCheckBox.isChecked = false
                replaceOnConflictCheckBox.isEnabled = false
            } else {
                replaceOnConflictCheckBox.isEnabled = true
            }
        }

        view.findViewById<Button>(R.id.btn_import).setOnClickListener {
            importDialogViewModel.deleteAll = deleteAllCheckBox.isChecked
            importDialogViewModel.replaceOnConflict = replaceOnConflictCheckBox.isChecked
            importDialogViewModel.importFormat =
                ImportFormat.getByName(importFormatSpinner.selectedItem as String)
            importDialogViewModel.accepted.value = true
            dismiss()
        }

        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dismiss()
        }
    }

}