/*
 * Created by Tomasz Kiljanczyk on 4/2/21 11:52 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/2/21 11:51 AM
 */

package pl.gunock.lyriccast.datamodel

import android.os.Build
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import pl.gunock.lyriccast.dataimport.models.ImportSong
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.datamodel.entities.LyricsSection
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.relations.SetlistWithSongs
import pl.gunock.lyriccast.datamodel.entities.relations.SongAndCategory
import pl.gunock.lyriccast.datamodel.entities.relations.SongWithLyricsSections
import pl.gunock.lyriccast.datamodel.models.ImportSongsOptions


class LyricCastViewModel(
    private val repository: LyricCastRepository
) : ViewModel() {
    private companion object {
        const val TAG = "LyricCastViewModel"
    }

    val allSongs: LiveData<List<SongAndCategory>> = repository.allSongs.asLiveData()
    val allSetlists: LiveData<List<Setlist>> = repository.allSetlists.asLiveData()
    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()

    suspend fun getAllSongs() =
        repository.getAllSongs()

    suspend fun upsertSong(song: SongWithLyricsSections, order: List<Pair<String, Int>>) =
        repository.upsertSong(song, order)

    suspend fun upsertSongs(
        songsWithLyricsSections: List<SongWithLyricsSections>,
        orderMap: Map<String, List<Pair<String, Int>>>
    ) = repository.upsertSongs(songsWithLyricsSections, orderMap)

    fun deleteSongs(songIds: List<Long>) =
        viewModelScope.launch { repository.deleteSongs(songIds) }

    fun upsertSetlist(setlist: SetlistWithSongs) =
        viewModelScope.launch { repository.upsertSetlist(setlist) }

    fun deleteSetlists(setlistIds: List<Long>) =
        viewModelScope.launch { repository.deleteSetlists(setlistIds) }

    suspend fun getAllCategories() =
        repository.getAllCategories()

    fun upsertCategories(categories: Collection<Category>) =
        runBlocking { repository.upsertCategories(categories) }

    fun upsertCategory(category: Category) =
        runBlocking { repository.upsertCategory(category) }

    fun deleteCategories(categoryIds: Collection<Long>) =
        viewModelScope.launch { repository.deleteCategories(categoryIds) }


    suspend fun importSongs(
        importSongs: Set<ImportSong>,
        message: MutableLiveData<String>,
        options: ImportSongsOptions
    ) {
        message.value = "Importing categories ..."
        var processedImportSongs = importSongs.toMutableSet()
        if (!options.deleteAll && !options.replaceOnConflict) {
            getAllSongs().forEach { song ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    processedImportSongs.removeIf { it.title == song.title }
                } else {
                    processedImportSongs = processedImportSongs
                        .filter { it.title != song.title }
                        .toMutableSet()
                }
            }
        }

        val categoryMap: MutableMap<String, Category> =
            processedImportSongs.map { importSong -> importSong.category }
                .distinct()
                .mapIndexed { index, categoryName ->
                    categoryName to Category(
                        name = categoryName.take(30),
                        color = options.colors[index % options.colors.size]
                    )
                }.toMap()
                .toMutableMap()
        categoryMap.remove("")

        if (options.deleteAll) {
            repository.clear()
        }

        val allCategories = getAllCategories()
        if (!options.deleteAll && !options.replaceOnConflict) {
            allCategories.forEach { categoryMap.remove(it.name) }
        }

        upsertCategories(categoryMap.values)

        message.value = "Importing songs ..."
        val categoryIdMap = getAllCategories()
            .map { it.name to it.categoryId }
            .toMap()

        val orderMap: MutableMap<String, List<Pair<String, Int>>> = mutableMapOf()
        val songsWithLyricsSections: List<SongWithLyricsSections> =
            processedImportSongs.map { importSong ->
                val song =
                    Song(title = importSong.title, categoryId = categoryIdMap[importSong.category])
                val lyricsSections: List<LyricsSection> =
                    importSong.lyrics.map {
                        LyricsSection(
                            songId = 0,
                            name = it.key,
                            text = it.value
                        )
                    }
                val order: List<Pair<String, Int>> = importSong.presentation
                    .mapIndexed { index, sectionName -> sectionName to index }

                orderMap[importSong.title] = order
                return@map SongWithLyricsSections(song, lyricsSections)
            }

        upsertSongs(songsWithLyricsSections, orderMap)

        message.value = "Finishing import ..."
        Log.d(TAG, "Finished import")
    }

}

class LyricCastViewModelFactory(
    private val repository: LyricCastRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LyricCastViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LyricCastViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}