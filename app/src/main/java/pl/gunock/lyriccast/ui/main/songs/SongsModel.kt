/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 19:17
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 19:15
 */

package pl.gunock.lyriccast.ui.main.songs

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
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

    val songs: StateFlow<List<SongItem>> get() = _songs
    private val _songs: MutableStateFlow<List<SongItem>> = MutableStateFlow(listOf())
    private var allSongs: List<SongItem> = listOf()


    val pickedSong: StateFlow<SongItem?> get() = _pickedSong
    private val _pickedSong: MutableStateFlow<SongItem?> = MutableStateFlow(null)

    val numberOfSelectedSongs: StateFlow<Pair<Int, Int>> get() = _numberOfSelectedSongs
    private val _numberOfSelectedSongs: MutableStateFlow<Pair<Int, Int>> =
        MutableStateFlow(Pair(0, 0))

    val selectedSongPosition: SharedFlow<Int> get() = _selectedSongPosition
    private val _selectedSongPosition: MutableSharedFlow<Int> = MutableSharedFlow(replay = 1)

    val categories: StateFlow<List<CategoryItem>> get() = _categories
    private val _categories: MutableStateFlow<List<CategoryItem>> = MutableStateFlow(listOf())

    val selectionTracker: SelectionTracker<BaseViewHolder> =
        SelectionTracker(this::onSongSelection)

    init {
        songsRepository.getAllSongs()
            .onEach {
                val songItems = it.map { song -> SongItem(song) }.sorted()
                if (_songs.value == songItems) return@onEach

                allSongs = songItems
                _songs.value = allSongs
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        categoriesRepository.getAllCategories()
            .onEach {
                val categoryItems = it.map { category -> CategoryItem(category) }.sorted()
                _categories.value = categoryItems
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    fun getSelectedSong(): SongItem {
        return _songs.value.first { songItem -> songItem.isSelected }
    }

    fun getSelectedSongIds(): List<String> {
        return songs.value
            .filter { it.isSelected }
            .map { item -> item.song.id }
    }

    suspend fun deleteSelectedSongs() {
        val selectedSongs = allSongs.filter { item -> item.isSelected }
            .map { item -> item.song.id }

        songsRepository.deleteSongs(selectedSongs)
        _numberOfSelectedSongs.value = Pair(selectedSongs.size, 0)
        selectionTracker.reset()
    }

    // TODO: Move filter to separate class (functional interface?)
    fun filterSongs(
        songTitle: String,
        categoryId: String? = null
    ) {
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

            _songs.value = filteredItems
        }
        Log.v(TAG, "Filtering took : ${duration}ms")
    }

    fun resetSongSelection() {
        _songs.value.forEach {
            it.isSelected = false
            it.hasCheckbox = false
        }
        selectionTracker.reset()
        if (_numberOfSelectedSongs.value != Pair(1, 0)) {
            _numberOfSelectedSongs.value = Pair(1, 0)
        }
    }

    fun resetPickedSong() {
        _pickedSong.value = null
    }

    suspend fun exportSelectedSongs(
        cacheDir: String,
        outputStream: OutputStream
    ): Flow<Int> = flow {
        val exportData: DatabaseTransferData = dataTransferRepository.getDatabaseTransferData()

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

        emit(R.string.main_activity_export_saving_json)

        val songsString = JSONArray(songJsons).toString()
        val categoriesString = JSONArray(categoryJsons).toString()
        File(exportDir, "songs.json").writeText(songsString)
        File(exportDir, "categories.json").writeText(categoriesString)

        emit(R.string.main_activity_export_saving_zip)
        FileHelper.zip(outputStream, exportDir.path)

        emit(R.string.main_activity_export_deleting_temp)
        exportDir.deleteRecursively()
        resetSongSelection()
    }.flowOn(Dispatchers.Default)

    private fun onSongSelection(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = _songs.value[position]

        if (!isLongClick && selectionTracker.count == 0) {
            _pickedSong.value = item
        } else {
            item.isSelected = !item.isSelected

            if (selectionTracker.count == 0 && selectionTracker.countAfter == 1) {
                _songs.value.forEach { it.hasCheckbox = true }
            } else if (selectionTracker.count == 1 && selectionTracker.countAfter == 0) {
                _songs.value.forEach { it.hasCheckbox = false }
            }

            val countPair = Pair(selectionTracker.count, selectionTracker.countAfter)
            _numberOfSelectedSongs.value = countPair
            _selectedSongPosition.tryEmit(position)
        }

        return true
    }

}