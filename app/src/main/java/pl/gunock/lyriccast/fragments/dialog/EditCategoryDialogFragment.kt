/*
 * Created by Tomasz Kiljańczyk on 3/13/21 3:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/13/21 2:59 PM
 */

package pl.gunock.lyriccast.fragments.dialog

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
import pl.gunock.lyriccast.CategoriesContext
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.spinner.ColorSpinnerAdapter
import pl.gunock.lyriccast.enums.NameValidationState
import pl.gunock.lyriccast.models.CategoryItem
import pl.gunock.lyriccast.models.ColorItem
import pl.gunock.lyriccast.viewmodels.EditCategoryViewModel
import java.util.*


class EditCategoryDialogFragment(
    private val category: CategoryItem? = null
) : DialogFragment() {

    companion object {
        const val TAG = "EditCategoryDialogFragment"
    }

    private val categoryNameTextWatcher: CategoryNameTextWatcher = CategoryNameTextWatcher()

    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var nameInput: TextView
    private lateinit var colorSpinner: Spinner

    private lateinit var viewModel: EditCategoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (category == null) {
            dialog?.setTitle("Add category")
        } else {
            dialog?.setTitle("Edit category")
        }

        return inflater.inflate(R.layout.dialog_fragment_edit_category, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(EditCategoryViewModel::class.java)

        nameInputLayout = view.findViewById(R.id.tv_category_name)
        nameInput = view.findViewById(R.id.tin_category_name)
        colorSpinner = view.findViewById(R.id.spn_category_color2)

        nameInput.filters = arrayOf(InputFilter.AllCaps(), InputFilter.LengthFilter(20))

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
        colorSpinner.adapter = colorSpinnerAdapter

        if (category?.color != null) {
            nameInput.text = category.name.toUpperCase(Locale.ROOT)
            colorSpinner.setSelection(colorValues.indexOf(category.color))
        }
    }

    private fun setupListeners() {
        nameInput.addTextChangedListener(categoryNameTextWatcher)

        requireView().findViewById<Button>(R.id.btn_save_category).setOnClickListener {
            val categoryName = nameInput.text.toString()
            if (validateCategoryName(categoryName) != NameValidationState.VALID) {
                nameInput.text = categoryName
                nameInput.requestFocus()
                return@setOnClickListener
            }

            val selectedColor = colorSpinner.selectedItem as ColorItem
            val newCategory = CategoryItem(
                nameInput.text.toString(),
                selectedColor.value
            )

            viewModel.category.value = EditCategoryViewModel.CategoryDto(newCategory, category)
            dismiss()
        }

        requireView().findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dismiss()
        }
    }

    private fun validateCategoryName(name: String): NameValidationState {
        if (name.isBlank()) {
            return NameValidationState.EMPTY
        }

        if (category != null && category.name == name) {
            return NameValidationState.VALID
        }

        if (CategoriesContext.containsCategory(name)) {
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
            val newText = s.toString()

            when (validateCategoryName(newText)) {
                NameValidationState.EMPTY -> {
                    nameInputLayout.error = " "
                    nameInput.error = "Please enter category name"
                }
                NameValidationState.ALREADY_IN_USE -> {
                    nameInputLayout.error = " "
                    nameInput.error = "Category name already in use"
                }
                NameValidationState.VALID -> {
                    nameInputLayout.error = null
                    nameInput.error = null
                }
            }
        }
    }

}