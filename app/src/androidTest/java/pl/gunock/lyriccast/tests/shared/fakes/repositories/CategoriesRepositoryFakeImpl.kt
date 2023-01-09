/*
 * Created by Tomasz Kiljanczyk on 08/01/2023, 23:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 08/01/2023, 23:51
 */

package pl.gunock.lyriccast.tests.shared.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import javax.inject.Inject

class CategoriesRepositoryFakeImpl @Inject constructor() : CategoriesRepository {
    private val categories = mutableListOf<Category>()
    private val categoryFlow = MutableStateFlow(categories.toList())


    override fun getAllCategories(): Flow<List<Category>> {
        return categoryFlow
    }

    override suspend fun upsertCategory(category: Category) {
        categories += category
        categoryFlow.emit(categories.toList())
    }

    override suspend fun deleteCategories(categoryIds: Collection<String>) {
        categories.removeIf { it.id in categoryIds }
        categoryFlow.emit(categories.toList())
    }
}