/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 13:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 13:13
 */

package pl.gunock.lyriccast.ui.category_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import javax.inject.Inject

@HiltViewModel
class CategoryManagerModel @Inject constructor(
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    val categories: LiveData<List<CategoryItem>> get() = _categories
    private val _categories: MutableLiveData<List<CategoryItem>> = MutableLiveData(listOf())

    val numberOfSelectedCategories: LiveData<Pair<Int, Int>> get() = _numberOfSelectedCategories
    private val _numberOfSelectedCategories: MutableLiveData<Pair<Int, Int>> =
        MutableLiveData(Pair(0, 0))

    val selectedCategoryPosition: LiveData<Int> get() = _selectedCategoryPosition
    private val _selectedCategoryPosition: MutableLiveData<Int> = MutableLiveData(0)

    val selectionTracker: SelectionTracker<BaseViewHolder> =
        SelectionTracker(this::onCategorySelection)

    init {
        categoriesRepository.getAllCategories()
            .onEach {
                val categoryItems = it.map { category -> CategoryItem(category) }.sorted()
                _categories.postValue(categoryItems)
            }.launchIn(viewModelScope)
    }

    fun getSelectedCategory(): CategoryItem {
        return categories.value!!.first { it.isSelected }
    }

    suspend fun deleteSelectedCategories() {
        val selectedCategories = categories.value!!.filter { item -> item.isSelected }
            .map { item -> item.category.id }

        categoriesRepository.deleteCategories(selectedCategories)
        _numberOfSelectedCategories.postValue(Pair(selectedCategories.size, 0))
    }

    fun resetCategorySelection() {
        _categories.value!!.forEach {
            it.isSelected = false
            it.hasCheckbox = false
        }
        selectionTracker.reset()
    }

    private fun onCategorySelection(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = _categories.value!![position]

        if (!isLongClick && selectionTracker.count == 0) {
            return false
        } else {
            item.isSelected = !item.isSelected

            if (selectionTracker.count == 0 && selectionTracker.countAfter == 1) {
                _categories.value!!.forEach { it.hasCheckbox = true }
            } else if (selectionTracker.count == 1 && selectionTracker.countAfter == 0) {
                _categories.value!!.forEach { it.hasCheckbox = false }
            }

            val countPair = Pair(selectionTracker.count, selectionTracker.countAfter)
            _numberOfSelectedCategories.postValue(countPair)
            _selectedCategoryPosition.postValue(position)
        }

        return true
    }

}