/*
 * Created by Tomasz Kiljanczyk on 4/20/21 11:03 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 10:40 AM
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
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.spinner.ColorSpinnerAdapter
import pl.gunock.lyriccast.datamodel.documents.CategoryDocument
import pl.gunock.lyriccast.enums.NameValidationState
import pl.gunock.lyriccast.fragments.viewholders.EditCategoryDialogViewModel
import pl.gunock.lyriccast.models.CategoryItem
import pl.gunock.lyriccast.models.ColorItem
import java.util.*


class EditCategoryDialogFragment(
    private val mCategoryItem: CategoryItem? = null
) : DialogFragment() {

    companion object {
        const val TAG = "EditCategoryDialogFragment"
    }

    private val mCategoryNameTextWatcher: CategoryNameTextWatcher = CategoryNameTextWatcher()

    private lateinit var mNameInputLayout: TextInputLayout
    private lateinit var mNameInput: TextView
    private lateinit var mColorSpinner: Spinner

    private lateinit var mDialogViewModel: EditCategoryDialogViewModel

    private lateinit var mCategoryNames: Set<String>

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mDialogViewModel =
            ViewModelProvider(requireActivity()).get(EditCategoryDialogViewModel::class.java)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mCategoryNames = mDialogViewModel.categoryNames.value ?: setOf()

        return inflater.inflate(R.layout.dialog_fragment_edit_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mNameInputLayout = view.findViewById(R.id.tv_category_name)
        mNameInput = view.findViewById(R.id.tin_category_name)
        mColorSpinner = view.findViewById(R.id.spn_category_color)

        view.findViewById<TextView>(R.id.tv_dialog_title).text = if (mCategoryItem == null) {
            getString(R.string.category_manager_add_category)
        } else {
            getString(R.string.category_manager_edit_category)
        }

        mNameInput.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(30))

        setupColorSpinner()
        setupListeners(view)
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
        mColorSpinner.adapter = colorSpinnerAdapter

        if (mCategoryItem?.category?.color != null) {
            mNameInput.text = mCategoryItem.category.name.toUpperCase(Locale.ROOT)
            mColorSpinner.setSelection(colorValues.indexOf(mCategoryItem.category.color!!))
        }
    }

    private fun setupListeners(view: View) {
        mNameInput.addTextChangedListener(mCategoryNameTextWatcher)

        view.findViewById<Button>(R.id.btn_save_category).setOnClickListener {
            val categoryName = mNameInput.text.toString().trim()
            if (validateCategoryName(categoryName) != NameValidationState.VALID) {
                mNameInput.text = categoryName
                mNameInput.requestFocus()
                return@setOnClickListener
            }

            val selectedColor = mColorSpinner.selectedItem as ColorItem

            val category: CategoryDocument = if (mDialogViewModel.category.value != null) {
                mDialogViewModel.category.value!!
            } else {
                CategoryDocument(name = mNameInput.text.toString(), color = selectedColor.value)
            }

            mDialogViewModel.category.value = category
            dismiss()
        }

        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dismiss()
        }
    }

    private fun validateCategoryName(name: String): NameValidationState {
        if (name.isBlank()) {
            return NameValidationState.EMPTY
        }

        if (mCategoryItem != null && mCategoryItem.category.name == name) {
            return NameValidationState.VALID
        }

        if (mCategoryNames.contains(name)) {
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
                    mNameInputLayout.error = getString(R.string.category_manager_enter_name)
                }
                NameValidationState.ALREADY_IN_USE -> {
                    mNameInputLayout.error = getString(R.string.category_manager_name_already_used)
                }
                NameValidationState.VALID -> {
                    mNameInputLayout.error = null
                }
            }
        }
    }

}