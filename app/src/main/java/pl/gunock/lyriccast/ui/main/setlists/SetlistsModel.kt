/*
 * Created by Tomasz Kiljanczyk on 30/12/2021, 14:14
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 30/12/2021, 13:56
 */

package pl.gunock.lyriccast.ui.main.setlists

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
    val dataTransferRepository: DataTransferRepository
) : ViewModel() {
    private companion object {
        const val TAG = "SetlistsViewModel"
    }

    val setlists: LiveData<List<SetlistItem>>
        get() = _setlists

    private val _setlists: MutableLiveData<List<SetlistItem>> = MutableLiveData(listOf())

    val pickedSetlist: LiveData<SetlistItem?>
        get() = _pickedSetlist

    private val _pickedSetlist: MutableLiveData<SetlistItem?> = MutableLiveData()


    val numberOfSelectedSetlists: LiveData<Pair<Int, Int>>
        get() = _numberOfSelectedSetlists

    private val _numberOfSelectedSetlists: MutableLiveData<Pair<Int, Int>> =
        MutableLiveData(Pair(0, 0))

    val selectedSetlistPosition: LiveData<Int> get() = _selectedSetlistPosition
    private val _selectedSetlistPosition: MutableLiveData<Int> = MutableLiveData(0)

    private var allSetlists: Iterable<SetlistItem> = listOf()

    val selectionTracker: SelectionTracker<BaseViewHolder> =
        SelectionTracker(this::onSetlistSelection)

    private var setlistsSubscription: Disposable? = null

    init {
        setlistsSubscription = setlistsRepository.getAllSetlists()
            .subscribe {
                viewModelScope.launch(Dispatchers.Default) {
                    val setlistItems = it.map { setlist -> SetlistItem(setlist) }.sorted()
                    allSetlists = setlistItems
                    _setlists.postValue(setlistItems)
                }
            }
    }

    override fun onCleared() {
        setlistsSubscription?.dispose()
        super.onCleared()
    }

    suspend fun deleteSelectedSetlists() {
        val selectedSetlists = allSetlists.filter { item -> item.isSelected }
            .map { item -> item.setlist.id }
        setlistsRepository.deleteSetlists(selectedSetlists)
        _numberOfSelectedSetlists.postValue(Pair(selectedSetlists.size, 0))
    }

    // TODO: Move filter to separate class (functional interface?)
    suspend fun filterSetlists(setlistName: String) {
        withContext(Dispatchers.Default) {
            val normalizedTitle = setlistName.trim().normalize()
            val duration = measureTimeMillis {
                val filteredItems = allSetlists.filter { setlistItem ->
                    setlistItem.normalizedName.contains(normalizedTitle, ignoreCase = true)
                }

                _setlists.postValue(filteredItems)
            }
            Log.v(TAG, "Filtering took : ${duration}ms")
        }
    }


    fun resetSetlistSelection() {
        _setlists.value?.forEach {
            it.isSelected = false
            it.hasCheckbox = false
        }
        selectionTracker.reset()

        if (_numberOfSelectedSetlists.value!! != Pair(1, 0)) {
            _numberOfSelectedSetlists.postValue(Pair(1, 0))
        }
    }

    fun resetPickedSetlist() {
        _pickedSetlist.postValue(null)
    }

    suspend fun exportSelectedSetlists(
        cacheDir: String,
        outputStream: OutputStream,
        messageResourceId: MutableLiveData<Int>
    ) {
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

        messageResourceId.postValue(R.string.main_activity_export_saving_json)

        val songsString = JSONArray(songJsons).toString()
        val categoriesString = JSONArray(categoryJsons).toString()
        val setlistsString = JSONArray(setlistJsons).toString()
        File(exportDir, "songs.json").writeText(songsString)
        File(exportDir, "categories.json").writeText(categoriesString)
        File(exportDir, "setlists.json").writeText(setlistsString)

        messageResourceId.postValue(R.string.main_activity_export_saving_zip)
        @Suppress("BlockingMethodInNonBlockingContext")
        FileHelper.zip(outputStream, exportDir.path)

        messageResourceId.postValue(R.string.main_activity_export_deleting_temp)
        exportDir.deleteRecursively()
        resetSetlistSelection()
    }

    private fun onSetlistSelection(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        val item = _setlists.value!![position]

        if (!isLongClick && selectionTracker.count == 0) {
            _pickedSetlist.postValue(item)
        } else {
            item.isSelected = !item.isSelected

            if (selectionTracker.count == 0 && selectionTracker.countAfter == 1) {
                _setlists.value!!.forEach { it.hasCheckbox = true }
            } else if (selectionTracker.count == 1 && selectionTracker.countAfter == 0) {
                _setlists.value!!.forEach { it.hasCheckbox = false }
            }

            val countPair = Pair(selectionTracker.count, selectionTracker.countAfter)
            _numberOfSelectedSetlists.postValue(countPair)
            _selectedSetlistPosition.postValue(position)
        }

        return true
    }

}