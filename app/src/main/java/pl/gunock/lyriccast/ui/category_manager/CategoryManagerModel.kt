/*
 * Created by Tomasz Kiljanczyk on 26/12/2022, 17:04
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 26/12/2022, 17:02
 */

package pl.gunock.lyriccast.ui.category_manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.domain.models.CategoryItem
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