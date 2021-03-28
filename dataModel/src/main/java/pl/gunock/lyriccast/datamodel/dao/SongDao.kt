/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 3:18 AM
 */

package pl.gunock.lyriccast.datamodel.dao

import androidx.room.*
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.relations.SongAndCategory
import pl.gunock.lyriccast.datamodel.entities.relations.SongWithLyricsSections

@Dao
interface SongDao {

    @Transaction
    @Query("SELECT * FROM Song")
    suspend fun getAll(): List<SongAndCategory>

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

}