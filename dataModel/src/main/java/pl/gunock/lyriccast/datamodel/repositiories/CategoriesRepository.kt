/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 18:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 16:37
 */

package pl.gunock.lyriccast.datamodel.repositiories

import io.reactivex.Flowable
import pl.gunock.lyriccast.datamodel.models.Category

interface CategoriesRepository {

    fun getAllCategories(): Flowable<List<Category>>

    suspend fun upsertCategory(category: Category)

    suspend fun deleteCategories(categoryIds: Collection<String>)

}