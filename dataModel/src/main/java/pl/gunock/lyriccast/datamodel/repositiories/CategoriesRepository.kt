/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 20:10
 */

package pl.gunock.lyriccast.datamodel.repositiories

import io.reactivex.Flowable
import pl.gunock.lyriccast.datamodel.models.Category
import java.io.Closeable

interface CategoriesRepository : Closeable {

    fun getAllCategories(): Flowable<List<Category>>

    fun upsertCategory(category: Category)

    fun deleteCategories(categoryIds: Collection<String>)

}