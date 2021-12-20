/*
 * Created by Tomasz Kiljanczyk on 21/12/2021, 00:28
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 21/12/2021, 00:14
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
import org.json.JSONArray
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.repositiories.CategoriesRepository
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.domain.models.CategoryItem
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import java.io.File
import java.io.OutputStream
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class SongsModel @Inject constructor(
    categoriesRepository: CategoriesRepository,
    private val songsRepository: SongsRepository,
    private val dataTransferRepository: DataTransferRepository
) : ViewModel() {
    private companion object {
        const val TAG = "SongsViewModel"
    }

    val songs: LiveData<List<SongItem>> get() = _songs
    private val _songs: MutableLiveData<List<SongItem>> = MutableLiveData(listOf())
    private var allSongs: List<SongItem> = listOf()


    val pickedSong: LiveData<SongItem?> get() = _pickedSong
    private val _pickedSong: MutableLiveData<SongItem?> = MutableLiveData()

    val numberOfSelectedSongs: LiveData<Pair<Int, Int>> get() = _numberOfSelectedSongs
    private val _numberOfSelectedSongs: MutableLiveData<Pair<Int, Int>> =
        MutableLiveData(Pair(0, 0))

    val selectedSongPosition: LiveData<Int> get() = _selectedSongPosition
    private val _selectedSongPosition: MutableLiveData<Int> = MutableLiveData(0)

    val categories: LiveData<List<CategoryItem>> get() = _categories
    private val _categories: MutableLiveData<List<CategoryItem>> = MutableLiveData(listOf())


    val selectionTracker: SelectionTracker<BaseViewHolder> =
        SelectionTracker(this::onSongSelection)

    private var songsSubscription: Disposable? = null
    private var categoriesSubscription: Disposable? = null

    init {
        songsSubscription =
            songsRepository.getAllSongs().subscribe {
                viewModelScope.launch(Dispatchers.Default) {
                    val songItems = it.map { song -> SongItem(song) }.sorted()
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

    fun getSelectedSong(): SongItem {
        return _songs.value!!.first { songItem -> songItem.isSelected }
    }

    fun getSelectedSongIds(): List<String> {
        return songs.value!!
            .filter { it.isSelected }
            .map { item -> item.song.id }
    }

    suspend fun deleteSelectedSongs() {
        val selectedSongs = allSongs.filter { item -> item.isSelected }
            .map { item -> item.song.id }

        songsRepository.deleteSongs(selectedSongs)
        _numberOfSelectedSongs.postValue(Pair(selectedSongs.size, 0))
    }

    // TODO: Move filter to separate class (functional interface?)
    suspend fun filterSongs(
        songTitle: String,
        categoryId: String? = null
    ) {
        withContext(Dispatchers.Default) {
            val predicates: MutableList<(SongItem) -> Boolean> = mutableListOf()

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

    fun resetSongSelection() {
        _songs.value!!.forEach {
            it.isSelected = false
            it.hasCheckbox = false
        }
        selectionTracker.reset()
    }

    fun resetPickedSong() {
        _pickedSong.postValue(null)
    }

    suspend fun exportSelectedSongs(
        cacheDir: String,
        outputStream: OutputStream,
        messageResourceId: MutableLiveData<Int>
    ) {
        val exportData: DatabaseTransferData = withContext(Dispatchers.Main) {
            dataTransferRepository.getDatabaseTransferData()
        }

        withContext(Dispatchers.IO) {
            val exportDir = File(cacheDir, ".export")
            exportDir.deleteRecursively()
            exportDir.mkdirs()

            val selectedSongs = allSongs.filter { it.isSelected }

            val songTitles: Set<String> = selectedSongs.map { it.song.title }.toSet()
            val categoryNames: Set<String> =
                selectedSongs.mapNotNull { it.song.category?.name }.toSet()


            val songJsons = exportData.songDtos!!
                .filter { it.title in songTitles }
                .map { it.toJson() }

            val categoryJsons = exportData.categoryDtos!!
                .filter { it.name in categoryNames }
                .map { it.toJson() }

            messageResourceId.postValue(R.string.main_activity_export_saving_json)

            val songsString = JSONArray(songJsons).toString()
            val categoriesString = JSONArray(categoryJsons).toString()
            File(exportDir, "songs.json").writeText(songsString)
            File(exportDir, "categories.json").writeText(categoriesString)

            messageResourceId.postValue(R.string.main_activity_export_saving_zip)
            FileHelper.zip(outputStream, exportDir.path)

            messageResourceId.postValue(R.string.main_activity_export_deleting_temp)
            exportDir.deleteRecursively()
            resetSongSelection()
        }
    }

    private fun onSongSelection(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = _songs.value!![position]

        if (!isLongClick && selectionTracker.count == 0) {
            _pickedSong.postValue(item)
        } else {
            item.isSelected = !item.isSelected

            if (selectionTracker.count == 0 && selectionTracker.countAfter == 1) {
                _songs.value!!.forEach { it.hasCheckbox = true }
            } else if (selectionTracker.count == 1 && selectionTracker.countAfter == 0) {
                _songs.value!!.forEach { it.hasCheckbox = false }
            }

            val countPair = Pair(selectionTracker.count, selectionTracker.countAfter)
            _numberOfSelectedSongs.postValue(countPair)
            _selectedSongPosition.postValue(position)
        }

        return true
    }

}