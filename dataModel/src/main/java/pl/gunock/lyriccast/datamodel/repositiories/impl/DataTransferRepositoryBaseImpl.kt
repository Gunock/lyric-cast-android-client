/*
 * Created by Tomasz Kiljanczyk on 19/07/2021, 00:22
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 19/07/2021, 00:22
 */

package pl.gunock.lyriccast.datamodel.repositiories.impl

import android.content.res.Resources
import android.util.Log
import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.datamodel.R
import pl.gunock.lyriccast.datamodel.extentions.toRealmList
import pl.gunock.lyriccast.datamodel.models.*
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datatransfer.models.CategoryDto
import pl.gunock.lyriccast.datatransfer.models.SetlistDto
import pl.gunock.lyriccast.datatransfer.models.SongDto

internal abstract class DataTransferRepositoryBaseImpl(
    private val mResources: Resources
) : DataTransferRepository {

    private companion object {
        const val TAG = "DataTransferRepository"
    }

    final override fun importSongs(
        data: DatabaseTransferData,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
        executeTransaction {
            executeDataImport(data, message, options)
        }
    }

    final override fun importSongs(
        songDtoSet: Set<SongDto>,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
        val categoryDtos: List<CategoryDto> = songDtoSet.map { songDto -> songDto.category }
            .distinct()
            .mapIndexedNotNull { index, categoryName ->
                CategoryDto(
                    name = categoryName?.take(30) ?: return@mapIndexedNotNull null,
                    color = options.colors[index % options.colors.size]
                )
            }

        val data = DatabaseTransferData(songDtoSet.toList(), categoryDtos, null)
        importSongs(data, message, options)
    }

    final override fun getDatabaseTransferData(): DatabaseTransferData {
        val songs: List<Song> = getAllSongs()
        val categories: List<Category> = getAllCategories()
        val setlists: List<Setlist> = getAllSetlists()

        val songDtos: List<SongDto> = songs.map { it.toDto() }
        val categoryDtos: List<CategoryDto> = categories.map { it.toDto() }
        val setlistDtos: List<SetlistDto> = setlists.map { it.toDto() }

        return DatabaseTransferData(
            songDtos = songDtos,
            categoryDtos = categoryDtos,
            setlistDtos = setlistDtos
        )
    }

    protected abstract fun executeTransaction(transaction: () -> Unit)

    protected abstract fun getAllSongs(): List<Song>

    protected abstract fun getAllSetlists(): List<Setlist>

    protected abstract fun getAllCategories(): List<Category>

    protected abstract fun upsertSongs(songs: Iterable<Song>)

    protected abstract fun upsertSetlists(setlists: Iterable<Setlist>)

    protected abstract fun upsertCategories(categories: Iterable<Category>)

    private fun executeDataImport(
        data: DatabaseTransferData,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
        if (options.deleteAll) {
            clearDatabase()
        }

        val removeConflicts: Boolean = !options.deleteAll && !options.replaceOnConflict

        if (data.categoryDtos != null) {
            message.postValue(mResources.getString(R.string.data_transfer_processor_importing_categories))
            executeCategoryImport(data.categoryDtos, options, removeConflicts)
        }

        if (data.songDtos != null) {
            message.postValue(mResources.getString(R.string.data_transfer_processor_importing_songs))
            executeSongImport(data.songDtos, options, removeConflicts)
        }

        if (data.setlistDtos != null) {
            message.postValue(mResources.getString(R.string.data_transfer_processor_importing_setlists))
            executeSetlistImport(data.setlistDtos, options, removeConflicts)
        }

        message.postValue(mResources.getString(R.string.data_transfer_processor_finishing_import))
        Log.d(TAG, "Finished import")
    }

    private fun executeCategoryImport(
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
            val categoryNameMap = allCategories.map { it.name to it.id }.toMap()
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

    private fun executeSongImport(
        songDtos: List<SongDto>,
        options: ImportOptions,
        removeConflicts: Boolean
    ) {
        val categoryMap: Map<String, Category> = getAllCategories().map { it.name to it }.toMap()

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
            val songTitleMap = allSongs.map { it.title to it.id }.toMap()
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

    private fun executeSetlistImport(
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
            val setlistNameMap = allSetlists.map { it.name to it.id }.toMap()

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

        val songTitleMap: Map<String, Song> = getAllSongs().map { it.title to it }.toMap()
        val setlistDtoNameMap = setlistDtos.map { it.name to it.songs }.toMap()
        setlists.forEach { setlist ->
            val presentation: List<Song> = setlistDtoNameMap[setlist.name]!!
                .map { songTitleMap[it]!! }

            setlist.presentation = presentation.toRealmList()
        }

        upsertSetlists(setlists)
    }
}