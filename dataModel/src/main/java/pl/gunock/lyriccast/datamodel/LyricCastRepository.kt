/*
 * Created by Tomasz Kiljanczyk on 4/1/21 11:57 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 11:55 PM
 */

package pl.gunock.lyriccast.datamodel

import android.util.Log
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
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
    private companion object {
        const val TAG = "LyricCastRepository"
    }

    val allSongs: Flow<List<SongAndCategory>> = songDao.getAllAsFlow()
    val allSetlists: Flow<List<Setlist>> = setlistDao.getAll()
    val allCategories: Flow<List<Category>> = categoryDao.getAllAsFlow()

    @WorkerThread
    suspend fun clear() {
        songDao.deleteAll()
        lyricsSectionDao.deleteAll()
        setlistDao.deleteAll()
        categoryDao.deleteAll()
    }

    @WorkerThread
    suspend fun getAllSongs(): List<Song> {
        return songDao.getAll()
    }

    @WorkerThread
    suspend fun getSongsAndCategories(songs: Collection<Song>): List<SongAndCategory> {
        return songDao.get(songs.map { song -> song.id })
    }

    @WorkerThread
    internal suspend fun upsertSong(
        songWithLyricsSections: SongWithLyricsSections,
        order: List<Pair<String, Int>>
    ) {
        val song = songWithLyricsSections.song
        val lyricsSections = songWithLyricsSections.lyricsSections

        if (song.title.isBlank()) {
            throw IllegalArgumentException("Blank song title $song")
        }

        if (lyricsSections.isEmpty()) {
            throw IllegalArgumentException("Empty lyrics sections $lyricsSections")
        }


        try {
            val songId = songDao.upsert(song)
            val sectionIds =
                lyricsSectionDao.upsert(lyricsSections.map { LyricsSection(songId, it) })

            val sectionIdMap = sectionIds.zip(lyricsSections)
                .map { it.second.name to it.first }
                .toMap()
            try {
                val crossRefs = order.map {
                    SongLyricsSectionCrossRef(null, songId, sectionIdMap[it.first]!!, it.second)
                }
                lyricsSectionDao.upsertRelations(crossRefs)
            } catch (exception: NullPointerException) {
                Log.wtf(TAG, song.title)
                Log.wtf(TAG, sectionIdMap.toString())
                Log.wtf(TAG, order.toString())
                Log.wtf(TAG, exception)
            }
        } catch (e: Exception) {
            songDao.delete(listOf(song.id))
            throw e
        }
    }

    @WorkerThread
    internal suspend fun upsertSongs(
        songsWithLyricsSections: List<SongWithLyricsSections>,
        orderMap: Map<String, List<Pair<String, Int>>>
    ) {
        val songs = songsWithLyricsSections.map { it.song }
        val songIds = songDao.upsert(songs)
        val lyricsSections: List<LyricsSection> =
            songsWithLyricsSections.flatMapIndexed { index, songWithLyricsSections ->
                songWithLyricsSections.lyricsSections.map { LyricsSection(songIds[index], it) }
            }

        val sectionIds = lyricsSectionDao.upsert(lyricsSections)
        val sectionIdMap: Map<String, Map<String, Long>> = songs.map { song ->
            song.title to sectionIds.zip(lyricsSections).map { it.second.name to it.first }.toMap()
        }.toMap()

        val songIdMap: Map<String, Long> = songs.zip(songIds)
            .map { it.first.title to it.second }
            .toMap()

        val crossRefs = orderMap.flatMap { orderMapEntry ->
            val songId = songIdMap[orderMapEntry.key]!!
            val songSectionIdMap = sectionIdMap[orderMapEntry.key]!!
            orderMapEntry.value.map {
                SongLyricsSectionCrossRef(null, songId, songSectionIdMap[it.first]!!, it.second)
            }
        }
        lyricsSectionDao.upsertRelations(crossRefs)
    }

    @WorkerThread
    internal suspend fun deleteSongs(songIds: List<Long>) {
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
    internal suspend fun upsertSetlist(setlistWithSongs: SetlistWithSongs) {
        if (setlistWithSongs.setlist.name.isBlank()) {
            throw IllegalArgumentException("Blank setlist name $setlistWithSongs")
        }

        if (setlistWithSongs.songs.isEmpty()) {
            throw IllegalArgumentException("Empty setlist songs $setlistWithSongs")
        }

        val setlist = setlistWithSongs.setlist
        try {
            val setlistId = setlistDao.upsert(setlist)

            val setlistSongCrossRefs = setlistWithSongs.setlistSongCrossRefs
                .map { SetlistSongCrossRef(setlistId, it) }

            setlistDao.upsertSongsCrossRefs(setlistSongCrossRefs)
        } catch (e: Exception) {
            setlistDao.delete(listOf(setlist.id))
            throw e
        }
    }

    @WorkerThread
    internal suspend fun deleteSetlists(setlistIds: Collection<Long>) {
        return setlistDao.delete(setlistIds)
    }

    @WorkerThread
    suspend fun getAllCategories(): List<Category> {
        return categoryDao.getAll()
    }

    @WorkerThread
    suspend fun upsertCategory(category: Category) {
        if (category.name.isBlank()) {
            throw IllegalArgumentException("Blank category name $category")
        }

        categoryDao.upsert(category)
    }

    @WorkerThread
    internal suspend fun upsertCategories(categories: Collection<Category>) {
        return categoryDao.upsert(categories)
    }

    @WorkerThread
    internal suspend fun deleteCategories(categoryIds: Collection<Long>) {
        categoryDao.delete(categoryIds)
    }

}