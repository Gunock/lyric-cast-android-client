/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:19 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:19 PM
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
    private val mSongDao: SongDao,
    private val mLyricsSectionDao: LyricsSectionDao,
    private val mSetlistDao: SetlistDao,
    private val mCategoryDao: CategoryDao
) {
    private companion object {
        const val TAG = "LyricCastRepository"
    }

    val allSongs: Flow<List<SongAndCategory>> = mSongDao.getAllAsFlow()
    val allSetlists: Flow<List<Setlist>> = mSetlistDao.getAllAsFlow()
    val allCategories: Flow<List<Category>> = mCategoryDao.getAllAsFlow()

    @WorkerThread
    suspend fun clear() {
        mSongDao.deleteAll()
        mLyricsSectionDao.deleteAll()
        mSetlistDao.deleteAll()
        mCategoryDao.deleteAll()
    }

    @WorkerThread
    suspend fun getAllSongs(): List<Song> {
        return mSongDao.getAll()
    }

    @WorkerThread
    internal suspend fun getAllSongsWithLyricsSections(): List<SongWithLyricsSections> {
        return mSongDao.getAllWithLyricsSections()
    }

    @WorkerThread
    suspend fun getSongsAndCategories(songs: Collection<Song>): List<SongAndCategory> {
        return mSongDao.get(songs.map { song -> song.id })
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
            val songId = mSongDao.upsert(song)
            val sectionIds =
                mLyricsSectionDao.upsert(lyricsSections.map { it.copy(songId = songId) })

            val sectionIdMap = sectionIds.zip(lyricsSections)
                .map { it.second.name to it.first }
                .toMap()
            try {
                val crossRefs = order.map {
                    SongLyricsSectionCrossRef(null, songId, sectionIdMap[it.first]!!, it.second)
                }
                mLyricsSectionDao.upsertRelations(crossRefs)
            } catch (exception: NullPointerException) {
                Log.wtf(TAG, song.title)
                Log.wtf(TAG, sectionIdMap.toString())
                Log.wtf(TAG, order.toString())
                Log.wtf(TAG, exception)
            }
        } catch (e: Exception) {
            mSongDao.delete(listOf(song.id))
            throw e
        }
    }

    @WorkerThread
    internal suspend fun upsertSongs(
        songsWithLyricsSections: List<SongWithLyricsSections>,
        orderMap: Map<String, List<Pair<String, Int>>>
    ) {
        val songs: List<Song> = songsWithLyricsSections.map { it.song }
        mSongDao.upsert(songs)
        val songIdMap: Map<String, Long> = mSongDao.getAll()
            .map { it.title to it.songId!! }
            .toMap()

        val lyricsSections: List<LyricsSection> =
            songsWithLyricsSections.flatMap { songWithLyricsSections ->
                val songId = songIdMap[songWithLyricsSections.song.title]!!
                songWithLyricsSections.lyricsSections.map { it.copy(songId = songId) }
            }

        mLyricsSectionDao.upsert(lyricsSections)
        val sectionsMap: Map<Long, List<LyricsSection>> = mLyricsSectionDao.getAll()
            .groupBy { it.songId }
            .toMap()

        val sectionIdMap: Map<String, Map<String, Long>> = songs.map { song ->
            val songId = songIdMap[song.title]!!
            val sections = sectionsMap[songId]!!
            song.title to sections.map { it.name to it.lyricsSectionId!! }
                .toMap()
        }.toMap()

        val crossRefs = orderMap.flatMap { orderMapEntry ->
            val songId = songIdMap[orderMapEntry.key]!!
            val songSectionIdMap = sectionIdMap[orderMapEntry.key]!!
            orderMapEntry.value.map {
                SongLyricsSectionCrossRef(null, songId, songSectionIdMap[it.first]!!, it.second)
            }
        }
        mLyricsSectionDao.upsertRelations(crossRefs)
    }

    @WorkerThread
    internal suspend fun deleteSongs(songIds: List<Long>) {
        mSongDao.delete(songIds)
    }

    @WorkerThread
    suspend fun getSongWithLyrics(songId: Long): SongWithLyricsSections? {
        return mSongDao.getWithLyricsSections(songId)
    }

    @WorkerThread
    suspend fun getSongsWithLyrics(songs: Collection<Song>): List<SongWithLyricsSections> {
        return mSongDao.getAllWithLyrics(songs.map { song -> song.id })
    }

    @WorkerThread
    internal suspend fun getAllSetlists(): List<SetlistWithSongs> {
        return mSetlistDao.getAll()
    }

    @WorkerThread
    suspend fun getSetlistWithSongs(setlistId: Long): SetlistWithSongs? {
        return mSetlistDao.getWithSongs(setlistId)
    }

    internal suspend fun upsertSetlists(
        setlists: List<Setlist>,
        setlistCrossRefMap: Map<String, List<SetlistSongCrossRef>>
    ) {
        mSetlistDao.upsert(setlists)

        val setlistNames = setlists.map { it.name }.toSet()
        val setlistIdMap: Map<String, Long> = mSetlistDao.getAll()
            .filter { it.setlist.name in setlistNames }
            .map { it.setlist.name to it.setlist.id }
            .toMap()

        val setlistCrossRefs = setlistCrossRefMap.flatMap { entry ->
            val setlistId: Long = setlistIdMap[entry.key]!!
            return@flatMap entry.value.map { it.copy(setlistId = setlistId) }
        }

        mSetlistDao.upsertSongsCrossRefs(setlistCrossRefs)
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

    @WorkerThread
    suspend fun getAllCategories(): List<Category> {
        return mCategoryDao.getAll()
    }

    @WorkerThread
    suspend fun upsertCategory(category: Category) {
        if (category.name.isBlank()) {
            throw IllegalArgumentException("Blank category name $category")
        }

        mCategoryDao.upsert(category)
    }

    @WorkerThread
    internal suspend fun upsertCategories(categories: Collection<Category>) {
        return mCategoryDao.upsert(categories)
    }

    @WorkerThread
    internal suspend fun deleteCategories(categoryIds: Collection<Long>) {
        mCategoryDao.delete(categoryIds)
    }

}