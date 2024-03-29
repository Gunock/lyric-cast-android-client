/*
 * Created by Tomasz Kiljanczyk on 10/01/2023, 21:34
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 10/01/2023, 21:31
 */

package pl.gunock.lyriccast.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import java.util.*
import javax.inject.Inject

class CategoriesRepositoryFakeImpl @Inject constructor() : CategoriesRepository {
    private val categories = mutableListOf<Category>()
    private val categoryFlow = MutableStateFlow(categories.toList())


    override fun getAllCategories(): Flow<List<Category>> {
        return categoryFlow
    }

    override suspend fun upsertCategory(category: Category) {
        val existingCategory = categories.find { it.id == category.id }
        if (existingCategory != null) {
            categories.remove(existingCategory)
        } else {
            category.id = UUID.randomUUID().toString()
        }

        categories += category
        categoryFlow.emit(categories.toList())
    }

    override suspend fun deleteCategories(categoryIds: Collection<String>) {
        categories.removeIf { it.id in categoryIds }
        categoryFlow.emit(categories.toList())
    }
}