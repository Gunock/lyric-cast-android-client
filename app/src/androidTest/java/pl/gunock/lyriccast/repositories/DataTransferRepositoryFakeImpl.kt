/*
 * Created by Tomasz Kiljanczyk on 10/01/2023, 21:34
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 10/01/2023, 21:31
 */

package pl.gunock.lyriccast.repositories

import kotlinx.coroutines.flow.first
import pl.gunock.lyriccast.datamodel.models.Category
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.datamodel.repositiories.impl.DataTransferRepositoryBaseImpl
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