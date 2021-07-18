/*
 * Created by Tomasz Kiljanczyk on 19/07/2021, 00:22
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 19/07/2021, 00:22
 */

package pl.gunock.lyriccast.datamodel.repositiories

import io.reactivex.Flowable
import pl.gunock.lyriccast.datamodel.models.Category

interface CategoriesRepository {

    fun getAllCategories(): Flowable<List<Category>>

    fun upsertCategory(category: Category)

    fun deleteCategories(categoryIds: Collection<String>)

}