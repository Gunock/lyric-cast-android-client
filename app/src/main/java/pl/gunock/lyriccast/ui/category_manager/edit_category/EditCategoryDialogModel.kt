/*
 * Created by Tomasz Kiljanczyk on 06/10/2021, 20:28
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 06/10/2021, 20:17
 */

package pl.gunock.lyriccast.ui.category_manager.edit_category

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.Disposable
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

    private var categoriesSubscription: Disposable? = null

    init {
        categoriesSubscription = categoriesRepository.getAllCategories().subscribe { categories ->
            _categoryNames = categories.map { it.name }.toSet()
        }
    }

    override fun onCleared() {
        categoriesSubscription?.dispose()
        super.onCleared()
    }

    suspend fun saveCategory() {
        val category =
            Category(name = categoryName, color = categoryColor?.value, id = categoryId)

        categoriesRepository.upsertCategory(category)
    }

}