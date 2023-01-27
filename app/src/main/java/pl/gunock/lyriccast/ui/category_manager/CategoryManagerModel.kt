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
import kotlinx.coroutines.flow.*
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import javax.inject.Inject

@HiltViewModel
class CategoryManagerModel @Inject constructor(
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryItem>> get() = _categories
    private val _categories: MutableStateFlow<List<CategoryItem>> = MutableStateFlow(listOf())

    val numberOfSelectedCategories: StateFlow<Pair<Int, Int>> get() = _numberOfSelectedCategories
    private val _numberOfSelectedCategories: MutableStateFlow<Pair<Int, Int>> =
        MutableStateFlow(Pair(0, 0))

    val selectedCategoryPosition: SharedFlow<Int> get() = _selectedCategoryPosition
    private val _selectedCategoryPosition: MutableSharedFlow<Int> = MutableSharedFlow(replay = 1)

    val selectionTracker: SelectionTracker<BaseViewHolder> =
        SelectionTracker(this::onCategorySelection)

    init {
        categoriesRepository.getAllCategories()
            .onEach {
                val categoryItems = it.map { category -> CategoryItem(category) }.sorted()
                _categories.value = categoryItems
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    fun getSelectedCategory(): CategoryItem {
        return categories.value.first { it.isSelected }
    }

    suspend fun deleteSelectedCategories() {
        val selectedCategories = categories.value.filter { item -> item.isSelected }
            .map { item -> item.category.id }

        categoriesRepository.deleteCategories(selectedCategories)
        _numberOfSelectedCategories.value = Pair(selectedCategories.size, 0)
    }

    fun resetCategorySelection() {
        _categories.value.forEach {
            it.hasCheckbox = false
            it.isSelected = false
        }
        selectionTracker.reset()
    }

    private fun onCategorySelection(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = _categories.value[position]

        if (!isLongClick && selectionTracker.count == 0) {
            return false
        }

        item.isSelected = !item.isSelected

        if (selectionTracker.count == 0 && selectionTracker.countAfter == 1) {
            _categories.value.forEach { it.hasCheckbox = true }
        } else if (selectionTracker.count == 1 && selectionTracker.countAfter == 0) {
            _categories.value.forEach {
                it.hasCheckbox = false
                it.isSelected = false
            }
        }

        val countPair = Pair(selectionTracker.count, selectionTracker.countAfter)
        _numberOfSelectedCategories.value = countPair
        _selectedCategoryPosition.tryEmit(position)

        return isLongClick || selectionTracker.count != 0
    }

}