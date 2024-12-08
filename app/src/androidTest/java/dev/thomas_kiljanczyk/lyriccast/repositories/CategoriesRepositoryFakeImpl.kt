/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.repositories

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID
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