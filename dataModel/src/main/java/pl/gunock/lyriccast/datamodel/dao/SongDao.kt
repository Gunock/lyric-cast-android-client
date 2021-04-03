/*
 * Created by Tomasz Kiljanczyk on 4/1/21 11:57 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 11:55 PM
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
    fun getAllAsFlow(): Flow<List<SongAndCategory>>

    @Transaction
    @Query("SELECT * FROM Song")
    suspend fun getAll(): List<Song>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(songs: Collection<Song>): List<Long>

    @Query("DELETE FROM Song WHERE songId IN (:songIds)")
    suspend fun delete(songIds: Collection<Long>)

    @Query("DELETE FROM Song")
    suspend fun deleteAll()
}