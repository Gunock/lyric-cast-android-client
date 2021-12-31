/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 13:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 13:13
 */

package pl.gunock.lyriccast.ui.category_manager.edit_category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.domain.models.ColorItem
import javax.inject.Inject

@HiltViewModel
class EditCategoryDialogModel @Inject constructor(
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    val categoryNames: Set<String> get() = _categoryNames
    private var _categoryNames: Set<String> = setOf()

    var categoryId: String = ""

    var categoryName: String = ""

    var categoryColor: ColorItem? = null

    init {
        categoriesRepository.getAllCategories()
            .onEach { categories -> _categoryNames = categories.map { it.name }.toSet() }
            .launchIn(viewModelScope)
    }

    suspend fun saveCategory() {
        val category =
            Category(name = categoryName, color = categoryColor?.value, id = categoryId)

        categoriesRepository.upsertCategory(category)
    }

}