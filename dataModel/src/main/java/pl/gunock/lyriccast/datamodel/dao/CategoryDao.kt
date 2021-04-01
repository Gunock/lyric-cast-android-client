/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/31/21 3:14 PM
 */

package pl.gunock.lyriccast.datamodel.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.gunock.lyriccast.datamodel.entities.Category

@Dao
interface CategoryDao {

    @Query("SELECT * FROM Category")
    fun getAll(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: Category): Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(category: Collection<Category>): List<Long>

    @Query("DELETE FROM Category WHERE categoryId IN (:categoryIds)")
    suspend fun delete(categoryIds: Collection<Long>)

    @Query("DELETE FROM Category")
    suspend fun deleteAll()
}