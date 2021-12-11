/*
 * Created by Tomasz Kiljanczyk on 12/11/2021, 18:07
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 12/11/2021, 18:03
 */

package pl.gunock.lyriccast.ui.setlist_editor.setlist

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.shared.enums.NameValidationState
import pl.gunock.lyriccast.ui.shared.adapters.BaseViewHolder
import pl.gunock.lyriccast.ui.shared.misc.SelectionTracker
import javax.inject.Inject

@HiltViewModel
class SetlistEditorModel @Inject constructor(
    private val songsRepository: SongsRepository,
    private val setlistsRepository: SetlistsRepository
) : ViewModel() {

    private companion object {
        const val TAG = "SetlistEditorModel"
    }

    val songs: List<SetlistSongItem> get() = _songs
    private var _songs: MutableList<SetlistSongItem> = mutableListOf()

    private var setlistNames: Set<String> = setOf()

    var setlistName: String = ""

    var setlistId: String = ""

    private var editedSetlist: Setlist? = null

    val numberOfSelectedSongs: LiveData<Pair<Int, Int>> get() = _numberOfSelectedSongs
    private val _numberOfSelectedSongs: MutableLiveData<Pair<Int, Int>> =
        MutableLiveData(Pair(0, 0))

    val selectedSongPosition: LiveData<Int> get() = _selectedSongPosition
    private val _selectedSongPosition: MutableLiveData<Int> = MutableLiveData(0)

    val removedSongPosition: LiveData<Int> get() = _removedSongPosition
    private val _removedSongPosition: MutableLiveData<Int> = MutableLiveData(0)

    val selectionTracker: SelectionTracker<BaseViewHolder> = SelectionTracker(this::onSongSelection)

    private var availableId: Long = 0L

    private var setlistsSubscription: Disposable? = null

    init {
        setlistsSubscription = setlistsRepository.getAllSetlists().subscribe { setlists ->
            setlistNames = setlists.map { setlist -> setlist.name }.toSet()
        }
    }

    override fun onCleared() {
        setlistsSubscription?.dispose()
        super.onCleared()
    }

    fun loadSetlist(setlistId: String, setlistName: String, presentation: List<String>) {
        this.setlistId = setlistId
        this.setlistName = setlistName

        val setlistSongs: List<SetlistSongItem> = presentation.map { songId ->
            SetlistSongItem(songsRepository.getSong(songId)!!, availableId++)
        }

        _songs = setlistSongs.toMutableList()
    }

    fun loadAdhocSetlist(presentation: List<String>) {
        _songs = presentation.map { songId ->
            SetlistSongItem(songsRepository.getSong(songId)!!, availableId++)
        }.toMutableList()
    }

    fun loadEditedSetlist(setlistId: String) {
        this.setlistId = setlistId
        editedSetlist = setlistsRepository.getSetlist(setlistId)!!.also { setlist ->
            _songs = setlist.presentation.map { SetlistSongItem(it, availableId++) }.toMutableList()
            setlistName = setlist.name
        }
    }

    suspend fun saveSetlist() {
        val presentation: Array<Song> = songs
            .map { it.song }
            .toTypedArray()

        val setlist = Setlist(setlistName, presentation.toList(), setlistId)

        withContext(Dispatchers.Main) {
            setlistsRepository.upsertSetlist(setlist)
        }
        Log.i(TAG, "Created setlist: $setlist")
    }

    fun resetSongSelection() {
        _songs.forEach {
            it.isSelected = false
            it.hasCheckbox = false
        }
        selectionTracker.reset()
    }

    fun moveSong(from: Int, to: Int) {
        val item = _songs.removeAt(from)
        _songs.add(to, item)
    }

    fun removeSelectedSongs() {
        @Suppress("ControlFlowWithEmptyBody")
        while (deleteSelectedItem()) {
        }
    }

    private fun deleteSelectedItem(): Boolean {
        val selectedItemIndex = _songs.indexOfFirst { item -> item.isSelected }
        if (selectedItemIndex == -1) {
            return false
        }
        _songs.removeAt(selectedItemIndex)
        _removedSongPosition.value = selectedItemIndex
        return true
    }

    fun duplicateSelectedSong(): Int {
        val selectedItemIndex = _songs.indexOfFirst { item -> item.isSelected }
        val selectedItem = _songs[selectedItemIndex].copy(id = availableId++)

        selectedItem.isSelected = false

        _songs.add(selectedItemIndex + 1, selectedItem)
        return selectedItemIndex + 1
    }

    fun validateSetlistName(name: String): NameValidationState {
        if (name.isBlank()) {
            return NameValidationState.EMPTY
        }

        val isAlreadyInUse = editedSetlist?.name != name && name in setlistNames

        return if (isAlreadyInUse) {
            NameValidationState.ALREADY_IN_USE
        } else {
            NameValidationState.VALID
        }
    }

    private fun onSongSelection(
        @Suppress("UNUSED_PARAMETER")
        holder: BaseViewHolder,
        position: Int,
        isLongClick: Boolean
    ): Boolean {
        if (!isLongClick && selectionTracker.count == 0) {
            return false
        }

        val item: SetlistSongItem = _songs[position]
        item.isSelected = !item.isSelected

        if (selectionTracker.count == 0 && selectionTracker.countAfter == 1) {
            _songs.forEach { it.hasCheckbox = true }
        } else if (selectionTracker.count == 1 && selectionTracker.countAfter == 0) {
            _songs.forEach { it.hasCheckbox = false }
        }

        val countPair = Pair(selectionTracker.count, selectionTracker.countAfter)
        _numberOfSelectedSongs.postValue(countPair)
        _selectedSongPosition.postValue(position)

        return true
    }

}