/*
 * Created by Tomasz Kiljanczyk on 06/10/2021, 12:51
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 06/10/2021, 12:48
 */

package pl.gunock.lyriccast.ui.category_manager.edit_category

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.domain.models.ColorItem
import javax.inject.Inject

@HiltViewModel
class EditCategoryDialogModel @Inject constructor(
    private val categoriesRepository: CategoriesRepository
) : ViewModel() {

    var categoryNames: Set<String> = setOf()

    var categoryId: String = ""

    var categoryName: String = ""

    var categoryColor: ColorItem? = null

    suspend fun saveCategory() {
        val category =
            Category(name = categoryName, color = categoryColor?.value, id = categoryId)

        categoriesRepository.upsertCategory(category)
    }

}