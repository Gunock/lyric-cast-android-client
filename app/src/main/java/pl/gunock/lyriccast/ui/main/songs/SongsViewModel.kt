/*
 * Created by Tomasz Kiljanczyk on 03/10/2021, 12:04
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 03/10/2021, 12:02
 */

package pl.gunock.lyriccast.ui.main.songs

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
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class SongsViewModel @Inject constructor(
    categoriesRepository: CategoriesRepository,
    private val songsRepository: SongsRepository,
    val dataTransferRepository: DataTransferRepository
) : ViewModel() {
    private companion object {
        const val TAG: String = "SongsViewModel"
    }

    val songs: LiveData<List<SongItem>> get() = _songs

    private val _songs: MutableLiveData<List<SongItem>> = MutableLiveData(listOf())

    val pickedItem: LiveData<SongItem> get() = _pickedItem

    private val _pickedItem: MutableLiveData<SongItem> = MutableLiveData()

    val numberOfSelectedItems: LiveData<Pair<Int, Int>> get() = _numberOfSelectedItems

    private val _numberOfSelectedItems: MutableLiveData<Pair<Int, Int>> =
        MutableLiveData(Pair(0, 0))

    val selectedItemPosition: LiveData<Int> get() = _selectedItemPosition

    private val _selectedItemPosition: MutableLiveData<Int> = MutableLiveData(0)

    private var allSongs: List<SongItem> = listOf()


    val categories: LiveData<List<CategoryItem>> get() = _categories

    private val _categories: MutableLiveData<List<CategoryItem>> = MutableLiveData(listOf())


    val selectionTracker: SelectionTracker<BaseViewHolder> =
        SelectionTracker(this::onItemSelection)

    private var songsSubscription: Disposable? = null

    private var categoriesSubscription: Disposable? = null

    init {
        songsSubscription = songsRepository.getAllSongs()
            .subscribe {
                viewModelScope.launch(Dispatchers.Default) {
                    val songItems = it.map { song -> SongItem(song) }
                    allSongs = songItems.sorted()
                    _songs.postValue(allSongs)
                }
            }

        categoriesSubscription = categoriesRepository.getAllCategories()
            .subscribe {
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

    fun deleteSelectedItems() {
        val selectedSongs = allSongs.filter { item -> item.isSelected }
            .map { item -> item.song.id }

        songsRepository.deleteSongs(selectedSongs)
    }

    // TODO: Move filter to separate class (functional interface?)
    suspend fun filterItems(
        songTitle: String,
        categoryId: String? = null,
        isSelected: Boolean? = null
    ) {
        withContext(Dispatchers.Default) {
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

    fun resetSelection() {
        _songs.value!!.forEach { it.isSelected = false }
        selectionTracker.reset()
    }

    private fun onItemSelection(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = _songs.value!![position]

        if (!isLongClick && selectionTracker.count == 0) {
            _pickedItem.postValue(item)
        } else {
            item.isSelected = !item.isSelected

            if (selectionTracker.count == 0 && selectionTracker.countAfter == 1) {
                _songs.value!!.forEach { it.hasCheckbox = true }
            } else if (selectionTracker.count == 1 && selectionTracker.countAfter == 0) {
                _songs.value!!.forEach { it.hasCheckbox = false }
            }

            val countPair = Pair(selectionTracker.count, selectionTracker.countAfter)
            _numberOfSelectedItems.postValue(countPair)
            _selectedItemPosition.postValue(position)
        }

        return true
    }

}