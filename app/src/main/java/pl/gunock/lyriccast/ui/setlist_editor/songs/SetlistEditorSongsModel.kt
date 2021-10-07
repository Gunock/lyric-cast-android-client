/*
 * Created by Tomasz Kiljanczyk on 07/10/2021, 19:59
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 07/10/2021, 19:59
 */

package pl.gunock.lyriccast.ui.setlist_editor.songs

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    val songs: LiveData<List<SongItem>> get() = _songs
    private val _songs: MutableLiveData<List<SongItem>> = MutableLiveData(listOf())
    private var allSongs: List<SongItem> = listOf()

    private var selectedSongs: MutableSet<SongItem> = mutableSetOf()

    val selectedSongPosition: LiveData<Int> get() = _selectedSongPosition
    private val _selectedSongPosition: MutableLiveData<Int> = MutableLiveData(0)

    var setlistSongIds: List<String> = listOf()

    val categories: LiveData<List<CategoryItem>> get() = _categories
    private val _categories: MutableLiveData<List<CategoryItem>> = MutableLiveData(listOf())

    val selectionTracker = SelectionTracker(this::onSongSelection)

    private var songsSubscription: Disposable? = null
    private var categoriesSubscription: Disposable? = null

    init {
        songsSubscription = songsRepository.getAllSongs().subscribe {
            viewModelScope.launch {
                val songItems = it.map { song ->
                    val isSelected = song.id in setlistSongIds
                    SongItem(song, hasCheckbox = true, isSelected = isSelected)
                }.sorted()
                allSongs = songItems
                _songs.postValue(allSongs)
            }
        }

        categoriesSubscription =
            categoriesRepository.getAllCategories().subscribe {
                viewModelScope.launch(Dispatchers.Default) {
                    val categoryItems = it.map { category -> CategoryItem(category) }.sorted()
                    _categories.postValue(categoryItems)
                }
            }
    }

    override fun onCleared() {
        songsSubscription?.dispose()
        categoriesSubscription?.dispose()
        super.onCleared()
    }

    suspend fun updateSongs() {
        withContext(Dispatchers.Default) {
            allSongs.forEach { it.isSelected = it.song.id in setlistSongIds }
            _songs.postValue(allSongs)

            selectedSongs = allSongs.filter { it.isSelected }.toMutableSet()
        }
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

    suspend fun filterSongs(
        songTitle: String,
        categoryItem: CategoryItem? = null,
        isSelected: Boolean? = null
    ) {
        filterSongs(songTitle, categoryItem?.category?.id, isSelected)
    }

    suspend fun filterSongs(
        songTitle: String,
        categoryId: String? = null,
        isSelected: Boolean? = null
    ) {
        withContext(Dispatchers.Default) {
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

                _songs.postValue(filteredItems)
            }
            Log.v(TAG, "Filtering took : ${duration}ms")
        }
    }

    private fun onSongSelection(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        @Suppress("UNUSED_PARAMETER")
        isLongClick: Boolean
    ): Boolean {
        val item: SongItem = _songs.value!![position]
        item.isSelected = !item.isSelected
        _selectedSongPosition.postValue(position)
        return true
    }

    private fun updateSelectedSongs() {
        for (item in _songs.value!!) {
            if (item.isSelected) {
                selectedSongs.add(item)
            } else {
                selectedSongs.remove(item)
            }
        }
    }

}