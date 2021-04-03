/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/31/21 2:55 PM
 */

package pl.gunock.lyriccast.datamodel.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.SetlistSongCrossRef
import pl.gunock.lyriccast.datamodel.entities.relations.SetlistWithSongs

@Dao
interface SetlistDao {
    @Query("SELECT * FROM Setlist WHERE setlistId = :setlistId")
    suspend fun get(setlistId: Long): Setlist?

    @Query("SELECT * FROM Setlist WHERE name = :name")
    suspend fun getByName(name: String): Setlist?

    @Transaction
    @Query("SELECT * FROM Setlist WHERE setlistId = :setlistId")
    suspend fun getWithSongs(setlistId: Long): SetlistWithSongs?

    @Query("SELECT * FROM Setlist")
    fun getAll(): Flow<List<Setlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setlist: Setlist): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSongsCrossRefs(setlistSongCrossRefs: Collection<SetlistSongCrossRef>)

    @Query("DELETE FROM Setlist WHERE setlistId IN (:setlistIds)")
    suspend fun delete(setlistIds: Collection<Long>)

    @Query("DELETE FROM Setlist")
    suspend fun deleteAll()
}