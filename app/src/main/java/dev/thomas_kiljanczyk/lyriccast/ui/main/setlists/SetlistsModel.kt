/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.main.setlists

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.R
import dev.thomas_kiljanczyk.lyriccast.common.helpers.FileHelper
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.DatabaseTransferData
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.DataTransferRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SetlistsRepository
import dev.thomas_kiljanczyk.lyriccast.domain.models.SetlistItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
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

    private val _setlists: MutableSharedFlow<List<SetlistItem>> = MutableSharedFlow(replay = 1)

    val setlists: Flow<List<SetlistItem>>
        get() = _setlists


    private var _filteredSetlists: List<SetlistItem> = listOf()
    val filteredSetlists: List<SetlistItem>
        get() = _filteredSetlists

    private var allSetlists: List<SetlistItem> = listOf()

    val searchValues get() = itemFilter.values

    private val itemFilter = SetlistItemFilter()

    init {
        setlistsRepository.getAllSetlists()
            .onEach { setlists ->
                val setlistItems = setlists.map { SetlistItem(it) }.sorted()

                allSetlists = setlistItems
                emitSetlists()
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)

        searchValues.setlistNameFlow
            .debounce(500)
            .onEach { emitSetlists() }
            .launchIn(viewModelScope)
    }

    suspend fun deleteSelectedSetlists() {
        val selectedSetlists = allSetlists
            .filter { item -> item.isSelected }
            .map { item -> item.setlist.id }
        setlistsRepository.deleteSetlists(selectedSetlists)
    }

    fun hideSelectionCheckboxes() {
        allSetlists.forEach {
            it.hasCheckbox = false
            it.isSelected = false
        }
    }

    fun showSelectionCheckboxes() {
        allSetlists.forEach { it.hasCheckbox = true }
    }

    fun exportSelectedSetlists(
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
        hideSelectionCheckboxes()
    }.flowOn(Dispatchers.Default)

    fun selectSetlist(setlistId: Long, selected: Boolean): Boolean {
        val setlist = allSetlists
            .firstOrNull { it.setlist.idLong == setlistId } ?: return false

        setlist.isSelected = selected
        return true
    }

    private suspend fun emitSetlists() = withContext(Dispatchers.Default) {
        Log.v(TAG, "Setlist filtering invoked")
        val duration = measureTimeMillis {
            _filteredSetlists = itemFilter.apply(allSetlists).toList()
            _setlists.emit(_filteredSetlists)
        }
        Log.v(TAG, "Filtering took : ${duration}ms")
    }

}