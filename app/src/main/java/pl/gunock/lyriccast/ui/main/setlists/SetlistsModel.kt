/*
 * Created by Tomasz Kiljanczyk on 12/11/2022, 19:57
 * Copyright (c) 2022 . All rights reserved.
 * Last modified 12/11/2022, 19:10
 */

package pl.gunock.lyriccast.ui.main.setlists

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.common.helpers.FileHelper
import pl.gunock.lyriccast.datamodel.models.DatabaseTransferData
import pl.gunock.lyriccast.datamodel.repositiories.DataTransferRepository
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.domain.models.SetlistItem
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import java.io.File
import java.io.OutputStream
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class SetlistsModel @Inject constructor(
    private val setlistsRepository: SetlistsRepository,
    private val dataTransferRepository: DataTransferRepository
) : ViewModel() {
    private companion object {
        const val TAG = "SetlistsViewModel"
    }

    val setlists: StateFlow<List<SetlistItem>>
        get() = _setlists

    private val _setlists: MutableStateFlow<List<SetlistItem>> = MutableStateFlow(listOf())

    val pickedSetlist: StateFlow<SetlistItem?>
        get() = _pickedSetlist

    private val _pickedSetlist: MutableStateFlow<SetlistItem?> = MutableStateFlow(null)


    val numberOfSelectedSetlists: StateFlow<Pair<Int, Int>>
        get() = _numberOfSelectedSetlists

    private val _numberOfSelectedSetlists: MutableStateFlow<Pair<Int, Int>> =
        MutableStateFlow(Pair(0, 0))

    val selectedSetlistPosition: SharedFlow<Int> get() = _selectedSetlistPosition
    private val _selectedSetlistPosition: MutableSharedFlow<Int> = MutableSharedFlow(replay = 1)

    private var allSetlists: Iterable<SetlistItem> = listOf()

    val selectionTracker: SelectionTracker<BaseViewHolder> =
        SelectionTracker(this::onSetlistSelection)

    init {
        setlistsRepository.getAllSetlists()
            .onEach { setlists ->
                val setlistItems = setlists.map { SetlistItem(it) }.sorted()
                if (_setlists.value == setlistItems) return@onEach

                allSetlists = setlistItems
                _setlists.value = setlistItems
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    suspend fun deleteSelectedSetlists() {
        val selectedSetlists = allSetlists.filter { item -> item.isSelected }
            .map { item -> item.setlist.id }
        setlistsRepository.deleteSetlists(selectedSetlists)
        _numberOfSelectedSetlists.value = Pair(selectedSetlists.size, 0)
        selectionTracker.reset()
    }

    // TODO: Move filter to separate class (functional interface?)
    suspend fun filterSetlists(setlistName: String) =
        withContext(Dispatchers.Default) {
            val normalizedTitle = setlistName.trim().normalize()
            val duration = measureTimeMillis {
                val filteredItems = allSetlists.filter { setlistItem ->
                    setlistItem.normalizedName.contains(normalizedTitle, ignoreCase = true)
                }

                _setlists.value = filteredItems
            }
            Log.v(TAG, "Filtering took : ${duration}ms")
        }


    fun resetSetlistSelection() {
        _setlists.value.forEach {
            it.isSelected = false
            it.hasCheckbox = false
        }
        selectionTracker.reset()

        if (_numberOfSelectedSetlists.value != Pair(1, 0)) {
            _numberOfSelectedSetlists.value = Pair(1, 0)
        }
    }

    fun resetPickedSetlist() {
        _pickedSetlist.value = null
    }

    suspend fun exportSelectedSetlists(
        cacheDir: String,
        outputStream: OutputStream
    ): Flow<Int> = flow {
        val exportData: DatabaseTransferData = dataTransferRepository.getDatabaseTransferData()

        val exportDir = File(cacheDir, ".export")
        exportDir.deleteRecursively()
        exportDir.mkdirs()

        val selectedSetlists = allSetlists.filter { it.isSelected }

        val setlistNames: Set<String> = selectedSetlists.map { it.setlist.name }.toSet()

        val exportSetlists = exportData.setlistDtos!!
            .filter { it.name in setlistNames }

        val songTitles: Set<String> = exportSetlists.flatMap { it.songs }.toSet()

        val categoryNames: Set<String> = exportData.songDtos!!
            .filter { it.title in songTitles }
            .mapNotNull { it.category }
            .toSet()

        val songJsons: List<JSONObject> = exportData.songDtos!!
            .filter { it.title in songTitles }
            .map { it.toJson() }

        val categoryJsons = exportData.categoryDtos!!
            .filter { it.name in categoryNames }
            .map { it.toJson() }

        val setlistJsons = exportSetlists.filter { it.name in setlistNames }
            .map { it.toJson() }

        emit(R.string.main_activity_export_saving_json)

        val songsString = JSONArray(songJsons).toString()
        val categoriesString = JSONArray(categoryJsons).toString()
        val setlistsString = JSONArray(setlistJsons).toString()
        File(exportDir, "songs.json").writeText(songsString)
        File(exportDir, "categories.json").writeText(categoriesString)
        File(exportDir, "setlists.json").writeText(setlistsString)

        emit(R.string.main_activity_export_saving_zip)
        withContext(Dispatchers.IO) {
            FileHelper.zip(outputStream, exportDir.path)
        }

        emit(R.string.main_activity_export_deleting_temp)
        exportDir.deleteRecursively()
        resetSetlistSelection()
    }.flowOn(Dispatchers.Default)

    private fun onSetlistSelection(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = _setlists.value[position]

        if (!isLongClick && selectionTracker.count == 0) {
            _pickedSetlist.value = item
        } else {
            item.isSelected = !item.isSelected

            if (selectionTracker.count == 0 && selectionTracker.countAfter == 1) {
                _setlists.value.forEach { it.hasCheckbox = true }
            } else if (selectionTracker.count == 1 && selectionTracker.countAfter == 0) {
                _setlists.value.forEach { it.hasCheckbox = false }
            }

            val countPair = Pair(selectionTracker.count, selectionTracker.countAfter)
            _numberOfSelectedSetlists.value = countPair
            _selectedSetlistPosition.tryEmit(position)
        }

        return true
    }

}