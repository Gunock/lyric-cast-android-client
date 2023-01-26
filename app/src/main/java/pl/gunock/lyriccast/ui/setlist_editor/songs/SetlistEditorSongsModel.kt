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
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
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

    private var selectedSongs: MutableSet<SongItem> = mutableSetOf()

    val selectedSongPosition: SharedFlow<Int> get() = _selectedSongPosition
    private val _selectedSongPosition: MutableSharedFlow<Int> = MutableSharedFlow(replay = 1)

    var setlistSongIds: List<String> = listOf()

    val categories: StateFlow<List<CategoryItem>> get() = _categories
    private val _categories: MutableStateFlow<List<CategoryItem>> = MutableStateFlow(listOf())

    val selectionTracker = SelectionTracker(this::onSongSelection)

    val searchValues get() = itemFilter.values

    private val itemFilter = SongItemFilter()

    init {
        songsRepository.getAllSongs()
            .onEach {
                val songItems = it.map { song ->
                    SongItem(song, hasCheckbox = true, isSelected = song.id in setlistSongIds)
                }.sorted()
                if (_songs.value == songItems) return@onEach

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

        searchValues.songTitle
            .debounce(500)
            .onEach { emitSongs() }
            .launchIn(viewModelScope)

        searchValues.categoryId
            .onEach { emitSongs() }
            .launchIn(viewModelScope)

        searchValues.isSelected
            .onEach { emitSongs() }
            .launchIn(viewModelScope)
    }

    suspend fun updateSongs() =
        withContext(Dispatchers.Default) {
            allSongs.forEach { it.isSelected = it.song.id in setlistSongIds }
            _songs.value = allSongs

            selectedSongs = allSongs.filter { it.isSelected }.toMutableSet()
        }

    fun updatePresentation(presentation: Array<String>): List<String> {
        updateSelectedSongs()

        val selectedSongIds = selectedSongs.map { it.song.id }
        val setlistSongIdsSet = setlistSongIds.toSet()

        val removedSongIds = setlistSongIdsSet.filter { it !in selectedSongIds }
        val addedSongIds = selectedSongIds.filter { it !in setlistSongIdsSet }

        val newSetlistSongIds = setlistSongIds.filter { it !in removedSongIds } + addedSongIds

        val newPresentation: List<String> = presentation.filter { it in newSetlistSongIds }

        return newPresentation + addedSongIds
    }

    private fun onSongSelection(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        @Suppress("UNUSED_PARAMETER")
        isLongClick: Boolean
    ): Boolean {
        val item: SongItem = _songs.value[position]
        item.isSelected = !item.isSelected
        _selectedSongPosition.tryEmit(position)
        return true
    }

    private fun updateSelectedSongs() {
        for (item in _songs.value) {
            if (item.isSelected) {
                selectedSongs.add(item)
            } else {
                selectedSongs.remove(item)
            }
        }
    }

    private suspend fun emitSongs() = withContext(Dispatchers.Default) {
        Log.v(TAG, "Song filtering invoked")
        val duration = measureTimeMillis {
            _songs.value = itemFilter.apply(allSongs).toList()
        }
        Log.v(TAG, "Filtering took : ${duration}ms")
    }
}