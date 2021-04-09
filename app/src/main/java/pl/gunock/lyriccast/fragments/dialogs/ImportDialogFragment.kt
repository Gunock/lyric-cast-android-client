/*
 * Created by Tomasz Kiljanczyk on 4/9/21 11:51 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/9/21 11:47 PM
 */

package pl.gunock.lyriccast.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat
import pl.gunock.lyriccast.fragments.viewholders.ImportDialogViewModel


class ImportDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "ImportDialogFragment"
    }

    private lateinit var mImportFormatSpinner: Spinner
    private lateinit var mDeleteAllCheckBox: CheckBox
    private lateinit var mReplaceOnConflictCheckBox: CheckBox

    private lateinit var mImportDialogViewModel: ImportDialogViewModel


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mImportDialogViewModel =
            ViewModelProvider(requireActivity()).get(ImportDialogViewModel::class.java)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_fragment_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mImportFormatSpinner = view.findViewById(R.id.spn_import_format)
        mDeleteAllCheckBox = view.findViewById(R.id.chk_delete_all)
        mReplaceOnConflictCheckBox = view.findViewById(R.id.chk_replace_on_conflict)

        view.findViewById<TextView>(R.id.tv_dialog_title).text =
            getString(R.string.main_activity_import_dialog_title)

        setupColorSpinner()
        setupListeners(view)
        mImportDialogViewModel.importFormat =
            ImportFormat.getByName(mImportFormatSpinner.selectedItem as String)
    }

    private fun setupColorSpinner() {
        mImportFormatSpinner.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.import_formats,
            android.R.layout.simple_list_item_1
        )
    }

    private fun setupListeners(view: View) {
        mDeleteAllCheckBox.setOnCheckedChangeListener { _, isChecked ->
            mImportDialogViewModel.deleteAll = isChecked
            if (isChecked) {
                mReplaceOnConflictCheckBox.isChecked = false
                mReplaceOnConflictCheckBox.isEnabled = false
            } else {
                mReplaceOnConflictCheckBox.isEnabled = true
            }
        }

        view.findViewById<Button>(R.id.btn_import).setOnClickListener {
            mImportDialogViewModel.deleteAll = mDeleteAllCheckBox.isChecked
            mImportDialogViewModel.replaceOnConflict = mReplaceOnConflictCheckBox.isChecked
            mImportDialogViewModel.importFormat =
                ImportFormat.getByName(mImportFormatSpinner.selectedItem as String)
            mImportDialogViewModel.accepted.value = true
            dismiss()
        }

        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dismiss()
        }
    }

}