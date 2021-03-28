/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 3:18 AM
 */

package pl.gunock.lyriccast.datamodel.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pl.gunock.lyriccast.datamodel.entities.Category

@Dao
interface CategoryDao {

    @Query("SELECT * FROM Category")
    suspend fun getAll(): List<Category>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: Category)

    @Query("DELETE FROM Category WHERE categoryId IN (:categoryIds)")
    suspend fun deleteCategories(categoryIds: Collection<Long>)

}