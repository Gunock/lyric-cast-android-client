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
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
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

    init {
        songsRepository.getAllSongs()
            .onEach {
                val songItems = it.map { song ->
                    SongItem(song, hasCheckbox = true, isSelected = song.id in setlistSongIds)
                }.sorted()
                if (_songs.value == songItems) return@onEach

                allSongs = songItems
                _songs.value = songItems
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        categoriesRepository.getAllCategories()
            .onEach {
                val categoryItems = it.map { category -> CategoryItem(category) }.sorted()
                _categories.value = categoryItems
            }.flowOn(Dispatchers.Default)
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

    fun filterSongs(
        songTitle: String,
        categoryItem: CategoryItem? = null,
        isSelected: Boolean? = null
    ) {
        filterSongs(songTitle, categoryItem?.category?.id, isSelected)
    }

    private fun filterSongs(
        songTitle: String,
        categoryId: String? = null,
        isSelected: Boolean? = null
    ) {
        updateSelectedSongs()

        val predicates: MutableList<(SongItem) -> Boolean> = mutableListOf()

        if (isSelected != null) {
            predicates.add { songItem -> songItem.isSelected }
        }

        if (!categoryId.isNullOrBlank()) {
            predicates.add { songItem -> songItem.song.category?.id == categoryId }
        }

        val normalizedTitle = songTitle.trim().normalize()
        predicates.add { item ->
            item.normalizedTitle.contains(normalizedTitle, ignoreCase = true)
        }

        val duration = measureTimeMillis {
            val filteredItems = allSongs.filter { songItem ->
                predicates.all { predicate -> predicate(songItem) }
            }.toSortedSet().toList()

            _songs.value = filteredItems
        }
        Log.v(TAG, "Filtering took : ${duration}ms")
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

}