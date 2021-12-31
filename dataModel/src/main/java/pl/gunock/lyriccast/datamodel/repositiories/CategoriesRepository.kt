/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 13:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 12:40
 */

package pl.gunock.lyriccast.datamodel.repositiories

import kotlinx.coroutines.flow.Flow
import pl.gunock.lyriccast.datamodel.models.Category

interface CategoriesRepository {

    fun getAllCategories(): Flow<List<Category>>

    suspend fun upsertCategory(category: Category)

    suspend fun deleteCategories(categoryIds: Collection<String>)

}