/*
 * Created by Tomasz Kiljanczyk on 14/05/2021, 00:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 14/05/2021, 00:06
 */

package pl.gunock.lyriccast.fragments.dialogs

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
import pl.gunock.lyriccast.fragments.viewmodels.ImportDialogViewModel


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
        setupListeners(view)
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

    private fun setupListeners(view: View) {
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
            mImportDialogViewModel.accepted.value = true
            dismiss()
        }

        mBinding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

}