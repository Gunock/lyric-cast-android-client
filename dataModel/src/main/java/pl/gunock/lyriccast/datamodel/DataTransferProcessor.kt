/*
 * Created by Tomasz Kiljanczyk on 4/5/21 1:10 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 1:09 AM
 */

package pl.gunock.lyriccast.datamodel

import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.datamodel.entities.*
import pl.gunock.lyriccast.datamodel.entities.relations.SongWithLyricsSections
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.models.ImportOptions
import pl.gunock.lyriccast.datatransfer.models.CategoryDto
import pl.gunock.lyriccast.datatransfer.models.SetlistDto
import pl.gunock.lyriccast.datatransfer.models.SongDto

internal class DataTransferProcessor(
    private val resources: Resources,
    private val repository: LyricCastRepository
) {
    private companion object {
        const val TAG = "DataTransferProcessor"
    }

    suspend fun importSongs(
        data: DatabaseTransferData,
        message: MutableLiveData<String>,
        options: ImportOptions
    ) {
        if (options.deleteAll) {
            repository.clear()
        }

        val removeConflicts = !options.deleteAll && !options.replaceOnConflict

        if (data.categoryDtos != null) {
            message.postValue(resources.getString(R.string.importing_categories))
            var categories = data.categoryDtos.map { Category(it) }.toMutableList()

            if (removeConflicts) {
                for (category in repository.getAllCategories()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        categories.removeIf { it.name == category.name }
                    } else {
                        categories = categories.filter { it.name != category.name }
                            .toMutableList()
                    }
                }
            }

            repository.upsertCategories(categories)
        }

        if (data.songDtos != null) {
            message.postValue(resources.getString(R.string.importing_songs))

            val categoryMap: Map<String, Category> = repository.getAllCategories()
                .map { it.name to it }
                .toMap()

            val orderMap: MutableMap<String, List<Pair<String, Int>>> = mutableMapOf()
            val songsWithLyricsSections: List<SongWithLyricsSections> = data.songDtos
                .map { songDto ->
                    val song = Song(songDto, categoryMap[songDto.category]?.categoryId)
                    val lyricsSections: List<LyricsSection> = songDto.lyrics
                        .map { LyricsSection(songId = -1, name = it.key, text = it.value) }

                    val order: List<Pair<String, Int>> = songDto.presentation
                        .mapIndexed { index, sectionName -> sectionName to index }

                    orderMap[song.title] = order
                    return@map SongWithLyricsSections(song, lyricsSections)
                }

            repository.upsertSongs(songsWithLyricsSections, orderMap)
        }

        if (data.setlistDtos != null) {
            message.postValue(resources.getString(R.string.importing_setlists))

            val songTitleMap: Map<String, Song> = repository.getAllSongs()
                .map { it.title to it }
                .toMap()

            var setlists: MutableList<Setlist> = data.setlistDtos
                .map { Setlist(it) }
                .toMutableList()

            if (removeConflicts) {
                for (setlistWithSongs in repository.getAllSetlists()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        setlists.removeIf { it.name == setlistWithSongs.setlist.name }
                    } else {
                        setlists = setlists.filter { it.name != setlistWithSongs.setlist.name }
                            .toMutableList()
                    }
                }
            }

            val setlistNames = setlists.map { it.name }.toHashSet()
            val setlistCrossRefMap: Map<String, List<SetlistSongCrossRef>> =
                data.setlistDtos
                    .filter { it.name in setlistNames }
                    .map { setlistDto ->
                        val songList = setlistDto.songs

                        val setlistSongCrossRefs: List<SetlistSongCrossRef> =
                            songList.mapIndexed { index, songTitle ->
                                SetlistSongCrossRef(null, -1, songTitleMap[songTitle]!!.id, index)
                            }
                        return@map setlistDto.name to setlistSongCrossRefs
                    }.toMap()

            repository.upsertSetlists(setlists, setlistCrossRefMap)
        }

        message.postValue(resources.getString(R.string.finishing_import))
        Log.d(TAG, "Finished import")
    }

    suspend fun getDatabaseTransferData(): DatabaseTransferData {
        val categories: List<Category> = repository.getAllCategories()
        val categoryMap: Map<Long?, String> = categories.map { it.categoryId to it.name }.toMap()

        val songDtos: List<SongDto> = repository.getAllSongsWithLyricsSections().map {
            it.toDto(categoryMap[it.song.categoryId])
        }

        val categoryDtos: List<CategoryDto> = categories.map { it.toDto() }
        val setlistDtos: List<SetlistDto> = repository.getAllSetlists().map { it.toDto() }
        return DatabaseTransferData(
            songDtos = songDtos,
            categoryDtos = categoryDtos,
            setlistDtos = setlistDtos
        )
    }
}