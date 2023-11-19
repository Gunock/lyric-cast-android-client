/*
 * Created by Tomasz Kiljanczyk on 07/01/2023, 21:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 07/01/2023, 21:22
 */

package pl.gunock.lyriccast.ui.category_manager.edit_category

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.databinding.DialogFragmentEditCategoryBinding
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.domain.models.ColorItem
import pl.gunock.lyriccast.shared.enums.NameValidationState
import pl.gunock.lyriccast.ui.shared.listeners.InputTextChangedListener
import java.util.Locale

@AndroidEntryPoint
class EditCategoryDialogFragment(
    private val categoryItem: CategoryItem? = null
) : DialogFragment() {

    companion object {
        const val TAG = "EditCategoryDialogFragment"
    }

    private val viewModel: EditCategoryDialogModel by viewModels()

    private val categoryNameTextWatcher: CategoryNameTextWatcher = CategoryNameTextWatcher()

    private lateinit var binding: DialogFragmentEditCategoryBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogFragmentEditCategoryBinding.inflate(layoutInflater)

        setupListeners()

        val title = if (categoryItem == null) {
            getString(R.string.category_manager_add_category)
        } else {
            getString(R.string.category_manager_edit_category)
        }

        return MaterialAlertDialogBuilder(
            requireContext(),
            R.style.ThemeOverlay_LyricCast_MaterialAlertDialog_NoTitle
        )
            .setTitle(title)
            .setPositiveButton(R.string.editor_button_save) { _, _ ->
                if (validateCategoryName()) {
                    lifecycleScope.launch(Dispatchers.Default) {
                        viewModel.saveCategory()
                        dismiss()
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.categoryId = categoryItem?.category?.id ?: ""

        binding.edCategoryName.filters = arrayOf(
            InputFilter.AllCaps(),
            InputFilter.LengthFilter(
                resources.getInteger(R.integer.ed_max_length_category_name)
            )
        )
        binding.edCategoryName.addTextChangedListener(InputTextChangedListener {
            viewModel.categoryName = it
        })

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
            binding.dropdownColor.context,
            colors
        )
        binding.dropdownColor.setAdapter(colorSpinnerAdapter)
        binding.dropdownColor.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val colorItem = colorSpinnerAdapter.getItem(position)
                setColor(colorItem)
            }

        if (categoryItem?.category != null) {
            val categoryNameUppercase = categoryItem.category
                .name
                .uppercase(Locale.ROOT)

            binding.edCategoryName.setText(categoryNameUppercase)
        }

        val existingColor = categoryItem?.category?.let {
            colors.find { it.value == categoryItem.category.color }
        }
        setColor(existingColor ?: colors.first())
    }

    private fun setupListeners() {
        binding.edCategoryName.addTextChangedListener(categoryNameTextWatcher)
    }

    private fun validateCategoryName(): Boolean {
        val categoryName = binding.edCategoryName.text.toString().trim()
        if (validateCategoryName(categoryName) != NameValidationState.VALID) {
            binding.edCategoryName.setText(categoryName)
            binding.tinCategoryName.requestFocus()
            return false
        }

        return true
    }

    private fun validateCategoryName(name: String): NameValidationState {
        if (name.isBlank()) {
            return NameValidationState.EMPTY
        }

        if (categoryItem != null && categoryItem.category.name == name) {
            return NameValidationState.VALID
        }

        if (name in viewModel.categoryNames) {
            return NameValidationState.ALREADY_IN_USE
        }

        return NameValidationState.VALID
    }

    private fun setColor(colorItem: ColorItem) {
        viewModel.categoryColor = colorItem
        binding.cardCategoryColor.setCardBackgroundColor(colorItem.value)
        binding.dropdownColor.setText(colorItem.name)
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
                    binding.tinCategoryName.error = getString(R.string.category_manager_enter_name)
                }

                NameValidationState.ALREADY_IN_USE -> {
                    binding.tinCategoryName.error =
                        getString(R.string.category_manager_name_already_used)
                }

                NameValidationState.VALID -> {
                    binding.tinCategoryName.error = null
                }
            }
        }
    }

}