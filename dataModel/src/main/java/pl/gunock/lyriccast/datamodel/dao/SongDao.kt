/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/31/21 2:55 PM
 */

package pl.gunock.lyriccast.datamodel.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.relations.SongAndCategory
import pl.gunock.lyriccast.datamodel.entities.relations.SongWithLyricsSections

@Dao
interface SongDao {

    @Transaction
    @Query("SELECT * FROM Song")
    fun getAll(): Flow<List<SongAndCategory>>

    @Transaction
    @Query("SELECT * FROM Song WHERE songId IN (:songIds)")
    suspend fun getAllWithLyrics(songIds: Collection<Long>): List<SongWithLyricsSections>

    @Transaction
    @Query("SELECT * FROM Song WHERE songId IN (:songs)")
    suspend fun get(songs: Collection<Long>): List<SongAndCategory>

    @Query("SELECT * FROM Song WHERE title = :title")
    suspend fun getByTitle(title: String): Song?

    @Transaction
    @Query("SELECT * FROM Song WHERE songId = :songId")
    suspend fun getWithLyricsSections(songId: Long): SongWithLyricsSections?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(song: Song): Long

    @Query("DELETE FROM Song WHERE songId IN (:songIds)")
    suspend fun delete(songIds: Collection<Long>)

    @Query("DELETE FROM Song")
    suspend fun deleteAll()
}