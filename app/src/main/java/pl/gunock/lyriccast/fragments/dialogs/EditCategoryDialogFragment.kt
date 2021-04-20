/*
 * Created by Tomasz Kiljanczyk on 4/20/21 10:45 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 10:24 PM
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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputLayout
import org.bson.types.ObjectId
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.adapters.spinner.ColorSpinnerAdapter
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

    private lateinit var mNameInputLayout: TextInputLayout
    private lateinit var mNameInput: TextView
    private lateinit var mColorSpinner: Spinner

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
    ): View? {
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
        mColorSpinner.adapter = colorSpinnerAdapter

        if (mCategoryItem?.category?.color != null) {
            mNameInput.text = mCategoryItem.category.name.toUpperCase(Locale.ROOT)
            mColorSpinner.setSelection(colorValues.indexOf(mCategoryItem.category.color!!))
        }
    }

    private fun setupListeners(view: View) {
        mNameInput.addTextChangedListener(mCategoryNameTextWatcher)

        view.findViewById<Button>(R.id.btn_save_category).setOnClickListener {
            saveCategory()
        }

        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dismiss()
        }
    }

    private fun saveCategory() {
        val categoryName = mNameInput.text.toString().trim()
        if (validateCategoryName(categoryName) != NameValidationState.VALID) {
            mNameInput.text = categoryName
            mNameInput.requestFocus()
            return
        }

        val selectedColor = mColorSpinner.selectedItem as ColorItem

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