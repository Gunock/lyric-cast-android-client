/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 5:11 PM
 */

package pl.gunock.lyriccast.datamodel

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import pl.gunock.lyriccast.datamodel.dao.SetlistDao
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.relations.SetlistWithSongs

class LyricCastRepository(
    private val mSetlistDao: SetlistDao
) {
    private companion object {
        const val TAG = "LyricCastRepository"
    }

    val allSetlists: Flow<List<Setlist>> = mSetlistDao.getAllAsFlow()

    @WorkerThread
    suspend fun clear() {
        mSetlistDao.deleteAll()
    }

    @WorkerThread
    suspend fun getSetlistWithSongs(setlistId: Long): SetlistWithSongs? {
        return mSetlistDao.getWithSongs(setlistId)
    }

    @WorkerThread
    internal suspend fun upsertSetlist(setlistWithSongs: SetlistWithSongs) {
        if (setlistWithSongs.setlist.name.isBlank()) {
            throw IllegalArgumentException("Blank setlist name $setlistWithSongs")
        }

        if (setlistWithSongs.songs.isEmpty()) {
            throw IllegalArgumentException("Empty setlist songs $setlistWithSongs")
        }

        val setlist = setlistWithSongs.setlist
        val setlistId = mSetlistDao.upsert(setlist)

        val setlistSongCrossRefs = setlistWithSongs.setlistSongCrossRefs
            .map { it.copy(setlistId = setlistId) }

        mSetlistDao.upsertSongsCrossRefs(setlistSongCrossRefs)
    }

    @WorkerThread
    internal suspend fun deleteSetlists(setlistIds: Collection<Long>) {
        return mSetlistDao.delete(setlistIds)
    }

}