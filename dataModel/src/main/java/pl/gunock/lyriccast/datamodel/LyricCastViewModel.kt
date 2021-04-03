/*
 * Created by Tomasz Kiljanczyk on 4/4/21 12:28 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/3/21 11:57 PM
 */

package pl.gunock.lyriccast.datamodel

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import pl.gunock.lyriccast.dataimport.models.ImportSong
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.datamodel.entities.LyricsSection
import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.relations.SetlistWithSongs
import pl.gunock.lyriccast.datamodel.entities.relations.SongAndCategory
import pl.gunock.lyriccast.datamodel.entities.relations.SongWithLyricsSections
import pl.gunock.lyriccast.datamodel.models.ExportData
import pl.gunock.lyriccast.datamodel.models.ImportSongsOptions


class LyricCastViewModel(
    private val resources: Resources,
    private val repository: LyricCastRepository
) : ViewModel() {
    private companion object {
        const val TAG = "LyricCastViewModel"
    }

    val allSongs: LiveData<List<SongAndCategory>> = repository.allSongs.asLiveData()
    val allSetlists: LiveData<List<Setlist>> = repository.allSetlists.asLiveData()
    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()

    suspend fun upsertSong(song: SongWithLyricsSections, order: List<Pair<String, Int>>) =
        repository.upsertSong(song, order)

    fun deleteSongs(songIds: List<Long>) =
        viewModelScope.launch { repository.deleteSongs(songIds) }

    fun upsertSetlist(setlist: SetlistWithSongs) =
        viewModelScope.launch { repository.upsertSetlist(setlist) }

    fun deleteSetlists(setlistIds: List<Long>) =
        viewModelScope.launch { repository.deleteSetlists(setlistIds) }

    fun upsertCategory(category: Category) =
        runBlocking { repository.upsertCategory(category) }

    fun deleteCategories(categoryIds: Collection<Long>) =
        viewModelScope.launch { repository.deleteCategories(categoryIds) }


    suspend fun importSongs(
        importSongs: Set<ImportSong>,
        message: MutableLiveData<String>,
        options: ImportSongsOptions
    ) {
        message.postValue(resources.getString(R.string.importing_categories))
        var processedImportSongs = importSongs.toMutableSet()
        if (!options.deleteAll && !options.replaceOnConflict) {
            repository.getAllSongs().forEach { song ->
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

        val allCategories = repository.getAllCategories()
        if (!options.deleteAll && !options.replaceOnConflict) {
            allCategories.forEach { categoryMap.remove(it.name) }
        }

        repository.upsertCategories(categoryMap.values)

        message.postValue(resources.getString(R.string.importing_songs))
        val categoryIdMap = repository.getAllCategories()
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

        repository.upsertSongs(songsWithLyricsSections, orderMap)

        message.postValue("Finishing import ...")
        Log.d(TAG, "Finished import")
    }

    suspend fun databaseToJson(): ExportData {
        val categories: List<Category> = repository.getAllCategories()
        val categoryMap: Map<Long?, String> = categories.map { it.categoryId to it.name }.toMap()

        val songsJson: List<JSONObject> = repository.getAllSongsWithLyricsSections().map {
            it.toJson()
                .put("category", categoryMap[it.song.categoryId] ?: JSONObject.NULL)
        }

        val categoriesJson: List<JSONObject> = categories.map { it.toJson() }
        val setlistsJson: List<JSONObject> = repository.getAllSetlists().map { it.toJson() }
        return ExportData(
            songsJson = songsJson,
            categoriesJson = categoriesJson,
            setlistsJson = setlistsJson
        )
    }

}

class LyricCastViewModelFactory(
    private val context: Context,
    private val repository: LyricCastRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LyricCastViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LyricCastViewModel(context.resources, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}