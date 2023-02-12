/*
 * Created by Tomasz Kiljanczyk on 12/11/2022, 19:57
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 12/11/2022, 19:34
 */

package pl.gunock.lyriccast.ui.setlist_editor.songs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.misc.SongItemFilter
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class SetlistEditorSongsModel @Inject constructor(
    songsRepository: SongsRepository,
    categoriesRepository: CategoriesRepository
) : ViewModel() {

    private companion object {
        const val TAG = "SetlistEditorSongsModel"
    }

    val songs: StateFlow<List<SongItem>> get() = _songs
    private val _songs: MutableStateFlow<List<SongItem>> = MutableStateFlow(listOf())
    private var allSongs: List<SongItem> = listOf()

    private var setlistSongIds: List<String> = listOf()

    val categories: StateFlow<List<CategoryItem>> get() = _categories
    private val _categories: MutableStateFlow<List<CategoryItem>> = MutableStateFlow(listOf())

    val searchValues get() = itemFilter.values

    private val itemFilter = SongItemFilter()

    init {
        songsRepository.getAllSongs()
            .onEach {
                val songItems = it.map { song ->
                    SongItem(song, hasCheckbox = true, isSelected = song.id in setlistSongIds)
                }.sorted()

                allSongs = songItems
                emitSongs()
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        categoriesRepository.getAllCategories()
            .onEach {
                val categoryItems = it.map { category -> CategoryItem(category) }.sorted()
                _categories.value = categoryItems
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        searchValues.songTitleFlow
            .debounce(500)
            .onEach { emitSongs() }
            .launchIn(viewModelScope)

        searchValues.categoryIdFlow
            .onEach { emitSongs() }
            .launchIn(viewModelScope)

        searchValues.isSelectedFlow
            .onEach { emitSongs() }
            .launchIn(viewModelScope)
    }


    fun init() {
    }

    suspend fun updateSetlistSongIds(newSetlistSongIds: List<String>) {
        setlistSongIds = newSetlistSongIds

        withContext(Dispatchers.Default) {
            allSongs.forEach { it.isSelected = it.song.id in setlistSongIds }

            emitSongs()
        }
    }

    fun updatePresentation(presentation: Array<String>): List<String> {
        val selectedSongIds = allSongs
            .filter { it.isSelected }
            .map { it.song.id }
            .toSet()

        val setlistSongIdsSet = setlistSongIds.toSet()

        val removedSongIds = setlistSongIdsSet.filter { it !in selectedSongIds }
        val addedSongIds = selectedSongIds.filter { it !in setlistSongIdsSet }

        val newSetlistSongIds = setlistSongIds.filter { it !in removedSongIds } + addedSongIds

        val newPresentation: List<String> = presentation.filter { it in newSetlistSongIds }

        return newPresentation + addedSongIds
    }

    fun selectSong(songItem: SongItem): Int {
        songItem.isSelected = !songItem.isSelected
        return _songs.value.indexOf(songItem)
    }

    private suspend fun emitSongs() = withContext(Dispatchers.Default) {
        Log.v(TAG, "Song filtering invoked")
        val duration = measureTimeMillis {
            _songs.value = itemFilter.apply(allSongs).toList()
        }
        Log.v(TAG, "Filtering took : ${duration}ms")
    }
}