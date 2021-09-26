/*
 * Created by Tomasz Kiljanczyk on 26/09/2021, 17:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 26/09/2021, 17:19
 */

package pl.gunock.lyriccast.ui.main

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.DialogFragmentImportBinding
import pl.gunock.lyriccast.datatransfer.enums.ImportFormat


class ImportDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "ImportDialogFragment"
    }

    private lateinit var mImportDialogViewModel: ImportDialogViewModel

    private lateinit var mBinding: DialogFragmentImportBinding


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mImportDialogViewModel =
            ViewModelProvider(requireActivity()).get(ImportDialogViewModel::class.java)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogFragmentImportBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.tvDialogTitle.text = getString(R.string.main_activity_import_dialog_title)

        setupColorSpinner()
        setupListeners()
        mImportDialogViewModel.importFormat =
            ImportFormat.getByName(mBinding.spnImportFormat.selectedItem as String)
    }

    private fun setupColorSpinner() {
        mBinding.spnImportFormat.adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.import_formats,
            android.R.layout.simple_list_item_1
        )
    }

    private fun setupListeners() {
        mBinding.chkDeleteAll.setOnCheckedChangeListener { _, isChecked ->
            mImportDialogViewModel.deleteAll = isChecked
            if (isChecked) {
                mBinding.chkReplaceOnConflict.isChecked = false
                mBinding.chkReplaceOnConflict.isEnabled = false
            } else {
                mBinding.chkReplaceOnConflict.isEnabled = true
            }
        }

        mBinding.btnImport.setOnClickListener {
            mImportDialogViewModel.deleteAll = mBinding.chkDeleteAll.isChecked
            mImportDialogViewModel.replaceOnConflict = mBinding.chkReplaceOnConflict.isChecked
            mImportDialogViewModel.importFormat =
                ImportFormat.getByName(mBinding.spnImportFormat.selectedItem as String)
            mImportDialogViewModel.accepted.postValue(true)
            dismiss()
        }

        mBinding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

}