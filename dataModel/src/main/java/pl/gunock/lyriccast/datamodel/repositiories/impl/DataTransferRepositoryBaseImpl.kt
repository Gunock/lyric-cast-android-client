/*
 * Created by Tomasz Kiljanczyk on 12/11/2022, 19:57
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 12/11/2022, 19:25
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import pl.gunock.lyriccast.datamodel.R
import pl.gunock.lyriccast.datamodel.extentions.toRealmList
import pl.gunock.lyriccast.datamodel.models.*
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datatransfer.models.CategoryDto
import pl.gunock.lyriccast.datatransfer.models.SetlistDto
import pl.gunock.lyriccast.datatransfer.models.SongDto

abstract class DataTransferRepositoryBaseImpl(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : DataTransferRepository {

    private companion object {
        const val TAG = "DataTransferRepository"
    }

    final override suspend fun importSongs(
        data: DatabaseTransferData,
        options: ImportOptions
    ): Flow<Int> {
        return executeDataImport(data, options)
    }

    final override suspend fun importSongs(
        songDtoSet: Set<SongDto>,
        options: ImportOptions
    ): Flow<Int> {
        val categoryDtos: List<CategoryDto> = songDtoSet.map { songDto -> songDto.category }
            .distinct()
            .mapIndexedNotNull { index, categoryName ->
                CategoryDto(
                    name = categoryName?.take(30) ?: return@mapIndexedNotNull null,
                    color = options.colors[index % options.colors.size]
                )
            }

        val data = DatabaseTransferData(songDtoSet.toList(), categoryDtos, null)
        return importSongs(data, options)
    }

    final override suspend fun getDatabaseTransferData(): DatabaseTransferData =
        coroutineScope {
            val songs: Deferred<List<Song>> = async { getAllSongs() }
            val categories: Deferred<List<Category>> = async { getAllCategories() }
            val setlists: Deferred<List<Setlist>> = async { getAllSetlists() }

            return@coroutineScope DatabaseTransferData(
                songDtos = songs.await().map { it.toDto() },
                categoryDtos = categories.await().map { it.toDto() },
                setlistDtos = setlists.await().map { it.toDto() }
            )
        }

    protected abstract suspend fun getAllSongs(): List<Song>

    protected abstract suspend fun getAllSetlists(): List<Setlist>

    protected abstract suspend fun getAllCategories(): List<Category>

    protected abstract suspend fun upsertSongs(songs: Iterable<Song>)

    protected abstract suspend fun upsertSetlists(setlists: Iterable<Setlist>)

    protected abstract suspend fun upsertCategories(categories: Iterable<Category>)

    private suspend fun executeDataImport(
        data: DatabaseTransferData,
        options: ImportOptions
    ): Flow<Int> = flow {
        if (options.deleteAll) {
            clearDatabase()
        }

        val removeConflicts: Boolean = !options.deleteAll && !options.replaceOnConflict

        if (data.categoryDtos != null) {
            emit(R.string.data_transfer_processor_importing_categories)
            executeCategoryImport(data.categoryDtos, options, removeConflicts)
        }

        if (data.songDtos != null) {
            emit(R.string.data_transfer_processor_importing_songs)
            executeSongImport(data.songDtos, options, removeConflicts)
        }

        if (data.setlistDtos != null) {
            emit(R.string.data_transfer_processor_importing_setlists)
            executeSetlistImport(data.setlistDtos, options, removeConflicts)
        }

        emit(R.string.data_transfer_processor_finishing_import)
        Log.d(TAG, "Finished import")
    }.flowOn(dispatcher)

    private suspend fun executeCategoryImport(
        categoryDtos: List<CategoryDto>,
        options: ImportOptions,
        removeConflicts: Boolean
    ) {
        val categories = categoryDtos.map { Category(it) }.toMutableList()

        val allCategories = getAllCategories()

        val categoryNames = allCategories.map { it.name }.toSet()
        if (removeConflicts) {
            categories.removeAll { it.name in categoryNames }
        } else if (options.replaceOnConflict) {
            val categoryNameMap = allCategories.associate { it.name to it.id }
            val categoriesToAdd: MutableList<Category> = mutableListOf()

            categories.removeAll {
                if (it.name in categoryNames) {
                    categoriesToAdd.add(it.copy(id = categoryNameMap[it.name]!!))
                    return@removeAll true
                }
                return@removeAll false
            }
            categories.addAll(categoriesToAdd)
        }

        upsertCategories(categories)
    }

    private suspend fun executeSongImport(
        songDtos: List<SongDto>,
        options: ImportOptions,
        removeConflicts: Boolean
    ) {
        val categoryMap: Map<String, Category> = getAllCategories().associateBy { it.name }

        val songs: MutableList<Song> = songDtos
            .map { dto ->
                val lyricsSections: List<Song.LyricsSection> = dto.lyrics
                    .map { Song.LyricsSection(name = it.key, text = it.value) }

                val song = Song(dto, categoryMap[dto.category])
                song.lyrics = lyricsSections.toRealmList()
                song.presentation = dto.presentation.toRealmList()

                return@map song
            }.toMutableList()


        val allSongs = getAllSongs()
        val songTitles = allSongs.map { it.title }.toSet()

        if (removeConflicts) {
            songs.removeAll { it.title in songTitles }
        } else if (options.replaceOnConflict) {
            val songTitleMap = allSongs.associate { it.title to it.id }
            val songsToAdd: MutableList<Song> = mutableListOf()
            songs.removeAll {
                if (it.title in songTitles) {
                    songsToAdd.add(it.copy(id = songTitleMap[it.title]!!))
                    return@removeAll true
                }
                return@removeAll false
            }
            songs.addAll(songsToAdd)
        }

        upsertSongs(songs)
    }

    private suspend fun executeSetlistImport(
        setlistDtos: List<SetlistDto>,
        options: ImportOptions,
        removeConflicts: Boolean
    ) {
        val setlists: MutableList<Setlist> = setlistDtos
            .map { Setlist(it) }
            .toMutableList()

        val allSetlists: List<Setlist> = getAllSetlists()
        val setlistNames = allSetlists.map { it.name }.toSet()

        if (removeConflicts) {
            setlists.removeAll { it.name in setlistNames }
        } else if (options.replaceOnConflict) {
            val setlistNameMap = allSetlists.associate { it.name to it.id }

            val setlistsToAdd: MutableList<Setlist> = mutableListOf()
            setlists.removeAll {
                if (it.name in setlistNames) {
                    setlistsToAdd.add(it.copy(id = setlistNameMap[it.name]!!))
                    return@removeAll true
                }
                return@removeAll false
            }
            setlists.addAll(setlistsToAdd)
        }

        val songTitleMap: Map<String, Song> = getAllSongs().associateBy { it.title }
        val setlistDtoNameMap = setlistDtos.associate { it.name to it.songs }
        setlists.forEach { setlist ->
            val presentation: List<Song> = setlistDtoNameMap[setlist.name]!!
                .map { songTitleMap[it]!! }

            setlist.presentation = presentation.toRealmList()
        }

        upsertSetlists(setlists)
    }
}