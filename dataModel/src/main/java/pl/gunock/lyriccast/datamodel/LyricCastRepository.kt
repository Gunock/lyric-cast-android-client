/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 3:18 AM
 */

package pl.gunock.lyriccast.datamodel

import androidx.annotation.WorkerThread
import pl.gunock.lyriccast.datamodel.dao.CategoryDao
import pl.gunock.lyriccast.datamodel.dao.LyricsSectionDao
import pl.gunock.lyriccast.datamodel.dao.SetlistDao
import pl.gunock.lyriccast.datamodel.dao.SongDao
import pl.gunock.lyriccast.datamodel.entities.*
import pl.gunock.lyriccast.datamodel.entities.relations.SetlistWithSongs
import pl.gunock.lyriccast.datamodel.entities.relations.SongAndCategory
import pl.gunock.lyriccast.datamodel.entities.relations.SongWithLyricsSections

class LyricCastRepository(
    private val songDao: SongDao,
    private val lyricsSectionDao: LyricsSectionDao,
    private val setlistDao: SetlistDao,
    private val categoryDao: CategoryDao
) {
    @WorkerThread
    suspend fun getSongs(): List<SongAndCategory> {
        return songDao.getAll()
    }

    @WorkerThread
    suspend fun getSongsAndCategories(songs: Collection<Song>): List<SongAndCategory> {
        return songDao.get(songs.map { song -> song.id })
    }

    @WorkerThread
    suspend fun upsertSong(
        songWithLyricsSections: SongWithLyricsSections,
        order: List<Pair<String, Int>>
    ) {
        val song = songWithLyricsSections.song
        val lyricsSections = songWithLyricsSections.lyricsSections
        try {
            val songId = songDao.upsert(song)
            val sectionIds =
                lyricsSectionDao.upsert(lyricsSections.map { LyricsSection(songId, it) })

            val sectionIdMap = sectionIds.zip(lyricsSections)
                .map { it.second.name to it.first }
                .toMap()

            val crossRefs = order.map {
                SongLyricsSectionCrossRef(null, songId, sectionIdMap[it.first]!!, it.second)
            }
            lyricsSectionDao.upsertRelations(crossRefs)
        } catch (e: Exception) {
            songDao.delete(listOf(song.id))
            throw e
        }
    }

    @WorkerThread
    suspend fun deleteSongs(songIds: List<Long>) {
        songDao.delete(songIds)
    }

    @WorkerThread
    suspend fun getSongWithLyrics(songId: Long): SongWithLyricsSections? {
        return songDao.getWithLyricsSections(songId)
    }

    @WorkerThread
    suspend fun getSongsWithLyrics(songs: Collection<Song>): List<SongWithLyricsSections> {
        return songDao.getAllWithLyrics(songs.map { song -> song.id })
    }

    @WorkerThread
    suspend fun getSetlistWithSongs(setlistId: Long): SetlistWithSongs? {
        return setlistDao.getWithSongs(setlistId)
    }

    @WorkerThread
    suspend fun getSetlists(): List<Setlist> {
        return setlistDao.getAll()
    }

    @WorkerThread
    suspend fun upsertSetlist(setlistWithSongs: SetlistWithSongs) {
        val setlist = setlistWithSongs.setlist
        try {
            val setlistId = setlistDao.upsert(setlist)

            val setlistSongCrossRefs = setlistWithSongs.setlistSongCrossRefs
                .map { SetlistSongCrossRef(setlistId, it) }

            setlistDao.upsertSongsCrossRefs(setlistSongCrossRefs)
        } catch (e: Exception) {
            setlistDao.delete(listOf(setlist.id))
            setlistDao.deleteSongsCrossRefs(setlist.id)
            throw e
        }
    }


    @WorkerThread
    suspend fun deleteSetlists(setlistIds: Collection<Long>) {
        return setlistDao.delete(setlistIds)
    }

    @WorkerThread
    suspend fun getCategories(): List<Category> {
        return categoryDao.getAll()
    }

    @WorkerThread
    suspend fun upsertCategory(category: Category) {
        categoryDao.upsertCategory(category)
    }

    @WorkerThread
    suspend fun deleteCategories(categoryIds: Collection<Long>) {
        categoryDao.deleteCategories(categoryIds)
    }
}