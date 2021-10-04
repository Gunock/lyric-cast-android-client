/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 18:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 16:06
 */

package pl.gunock.lyriccast.ui.category_manager

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.DialogFragmentEditCategoryBinding
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.domain.models.ColorItem
import pl.gunock.lyriccast.shared.enums.NameValidationState
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class EditCategoryDialogFragment(
    private val mCategoryItem: CategoryItem? = null
) : DialogFragment() {

    companion object {
        const val TAG = "EditCategoryDialogFragment"
    }

    @Inject
    lateinit var categoriesRepository: CategoriesRepository

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

        // TODO: Handle new category
        val categoryId: String = if (mDialogViewModel.category != null) {
            mDialogViewModel.category!!.id
        } else {
            ""
        }

        val category =
            Category(name = categoryName, color = selectedColor.value, id = categoryId)

        runBlocking { categoriesRepository.upsertCategory(category) }
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