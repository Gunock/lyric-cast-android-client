/*
 * Created by Tomasz Kiljanczyk on 16/05/2021, 17:06
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 15/05/2021, 21:47
 */

package pl.gunock.lyriccast.fragments.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import org.bson.types.ObjectId
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.spinner.ColorSpinnerAdapter
import pl.gunock.lyriccast.databinding.DialogFragmentEditCategoryBinding
import pl.gunock.lyriccast.datamodel.DatabaseViewModel
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument
import pl.gunock.lyriccast.enums.NameValidationState
import pl.gunock.lyriccast.fragments.viewmodels.EditCategoryDialogViewModel
import pl.gunock.lyriccast.models.CategoryItem
import pl.gunock.lyriccast.models.ColorItem
import java.util.*


class EditCategoryDialogFragment(
    private val mCategoryItem: CategoryItem? = null
) : DialogFragment() {

    companion object {
        const val TAG = "EditCategoryDialogFragment"
    }

    private val mDatabaseViewModel: DatabaseViewModel by viewModels {
        DatabaseViewModel.Factory(resources)
    }

    private val mCategoryNameTextWatcher: CategoryNameTextWatcher = CategoryNameTextWatcher()

    private lateinit var mBinding: DialogFragmentEditCategoryBinding

    private lateinit var mDialogViewModel: EditCategoryDialogViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mDialogViewModel =
            ViewModelProvider(requireActivity()).get(EditCategoryDialogViewModel::class.java)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogFragmentEditCategoryBinding.inflate(inflater)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.tvDialogTitle.text = if (mCategoryItem == null) {
            getString(R.string.category_manager_add_category)
        } else {
            getString(R.string.category_manager_edit_category)
        }

        mBinding.edCategoryName.filters = arrayOf(
            InputFilter.AllCaps(),
            InputFilter.LengthFilter(
                resources.getInteger(R.integer.ed_max_length_category_name)
            )
        )

        setupColorSpinner()
        setupListeners()
    }

    override fun onDestroy() {
        mDatabaseViewModel.close()
        super.onDestroy()
    }

    private fun setupColorSpinner() {
        val colorNames = resources.getStringArray(R.array.category_color_names)
        val colorValues = resources.getIntArray(R.array.category_color_values)
        val colors = Array(colorNames.size) { position ->
            ColorItem(colorNames[position], colorValues[position])
        }

        val colorSpinnerAdapter = ColorSpinnerAdapter(
            requireContext(),
            colors
        )
        mBinding.spnCategoryColor.adapter = colorSpinnerAdapter

        if (mCategoryItem?.category?.color != null) {
            val categoryNameUppercase = mCategoryItem.category
                .name
                .uppercase(Locale.ROOT)

            mBinding.edCategoryName.setText(categoryNameUppercase)
            mBinding.spnCategoryColor
                .setSelection(colorValues.indexOf(mCategoryItem.category.color!!))
        }
    }

    private fun setupListeners() {
        mBinding.edCategoryName.addTextChangedListener(mCategoryNameTextWatcher)

        mBinding.btnSaveCategory.setOnClickListener {
            saveCategory()
        }

        mBinding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun saveCategory() {
        val categoryName = mBinding.edCategoryName.text.toString().trim()
        if (validateCategoryName(categoryName) != NameValidationState.VALID) {
            mBinding.edCategoryName.setText(categoryName)
            mBinding.tinCategoryName.requestFocus()
            return
        }

        val selectedColor = mBinding.spnCategoryColor.selectedItem as ColorItem

        val categoryId: ObjectId = if (mDialogViewModel.category != null) {
            mDialogViewModel.category!!.id
        } else {
            ObjectId()
        }

        val categoryDocument =
            CategoryDocument(name = categoryName, color = selectedColor.value, id = categoryId)

        mDatabaseViewModel.upsertCategory(categoryDocument)
        mDialogViewModel.category = null
        dismiss()
    }

    private fun validateCategoryName(name: String): NameValidationState {
        if (name.isBlank()) {
            return NameValidationState.EMPTY
        }

        if (mCategoryItem != null && mCategoryItem.category.name == name) {
            return NameValidationState.VALID
        }

        if (mDialogViewModel.categoryNames.contains(name)) {
            return NameValidationState.ALREADY_IN_USE
        }

        return NameValidationState.VALID
    }

    inner class CategoryNameTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val newText = s.toString().trim()

            when (validateCategoryName(newText)) {
                NameValidationState.EMPTY -> {
                    mBinding.tinCategoryName.error = getString(R.string.category_manager_enter_name)
                }
                NameValidationState.ALREADY_IN_USE -> {
                    mBinding.tinCategoryName.error =
                        getString(R.string.category_manager_name_already_used)
                }
                NameValidationState.VALID -> {
                    mBinding.tinCategoryName.error = null
                }
            }
        }
    }

}