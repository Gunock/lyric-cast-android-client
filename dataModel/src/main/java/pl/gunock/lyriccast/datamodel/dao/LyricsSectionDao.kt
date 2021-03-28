/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 1:48 AM
 */

package pl.gunock.lyriccast.datamodel.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pl.gunock.lyriccast.datamodel.entities.LyricsSection
import pl.gunock.lyriccast.datamodel.entities.SongLyricsSectionCrossRef

@Dao
interface LyricsSectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lyricsSection: LyricsSection)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(lyricsSection: Collection<LyricsSection>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRelations(songLyricsSectionCrossRefs: Collection<SongLyricsSectionCrossRef>)

    @Query("DELETE FROM LyricsSection WHERE lyricsSectionId IN (:lyricsSectionIds)")
    suspend fun delete(lyricsSectionIds: Collection<Long>)
}