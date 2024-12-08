/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.category_manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.domain.models.CategoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class CategoryManagerModel @Inject constructor(
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    private val _categories: MutableStateFlow<List<CategoryItem>> = MutableStateFlow(listOf())
    val categories: StateFlow<List<CategoryItem>> get() = _categories

    init {
        categoriesRepository.getAllCategories()
            .onEach {
                val categoryItems = it.map { category -> CategoryItem(category) }.sorted()
                _categories.value = categoryItems
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    suspend fun deleteSelectedCategories() {
        val selectedCategories = _categories.value.filter { it.isSelected }
            .map { item -> item.category.id }

        categoriesRepository.deleteCategories(selectedCategories)
    }

    fun hideSelectionCheckboxes() {
        _categories.value.forEach {
            it.hasCheckbox = false
            it.isSelected = false
        }
    }

    fun showSelectionCheckboxes() {
        _categories.value.forEach { it.hasCheckbox = true }
    }

    fun selectCategory(categoryId: Long, selected: Boolean): Boolean {
        val category = _categories.value
            .firstOrNull { it.category.idLong == categoryId } ?: return false

        category.isSelected = selected
        return true
    }

}