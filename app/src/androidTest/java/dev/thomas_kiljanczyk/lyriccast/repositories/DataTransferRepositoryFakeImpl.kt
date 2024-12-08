/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.repositories

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Category
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Setlist
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.CategoriesRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SetlistsRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.impl.DataTransferRepositoryBaseImpl
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DataTransferRepositoryFakeImpl @Inject constructor(
    private val songsRepository: SongsRepository,
    private val setlistsRepository: SetlistsRepository,
    private val categoriesRepository: CategoriesRepository
) : DataTransferRepositoryBaseImpl() {
    override suspend fun getAllSongs(): List<Song> {
        return songsRepository.getAllSongs().first()
    }

    override suspend fun getAllSetlists(): List<Setlist> {
        return setlistsRepository.getAllSetlists().first()
    }

    override suspend fun getAllCategories(): List<Category> {
        return categoriesRepository.getAllCategories().first()
    }

    override suspend fun upsertSongs(songs: Iterable<Song>) {
        songs.forEach { songsRepository.upsertSong(it) }
    }

    override suspend fun upsertSetlists(setlists: Iterable<Setlist>) {
        setlists.forEach { setlistsRepository.upsertSetlist(it) }
    }

    override suspend fun upsertCategories(categories: Iterable<Category>) {
        categories.forEach { categoriesRepository.upsertCategory(it) }
    }

    override suspend fun clearDatabase() {
        val setlistIds = getAllSetlists().map { it.id }
        setlistsRepository.deleteSetlists(setlistIds)

        val songIds = getAllSongs().map { it.id }
        songsRepository.deleteSongs(songIds)

        val categoryIds = getAllCategories().map { it.id }
        categoriesRepository.deleteCategories(categoryIds)
    }

}