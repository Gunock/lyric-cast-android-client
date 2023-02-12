/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 19:17
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 19:07
 */

package pl.gunock.lyriccast.ui.setlist_editor.setlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.shared.enums.NameValidationState
import javax.inject.Inject

@HiltViewModel
class SetlistEditorModel @Inject constructor(
    private val songsRepository: SongsRepository,
    private val setlistsRepository: SetlistsRepository
) : ViewModel() {

    private companion object {
        const val TAG = "SetlistEditorModel"
    }

    private var _songs: MutableStateFlow<MutableList<SetlistSongItem>> =
        MutableStateFlow(mutableListOf())
    val songs: StateFlow<List<SetlistSongItem>> = _songs

    private var setlistNames: Set<String> = setOf()

    var setlistName: String = ""

    var setlistId: String = ""
        private set

    private var editedSetlist: Setlist? = null

    private var availableId: Long = 0L

    init {
        setlistsRepository.getAllSetlists()
            .onEach { setlists -> setlistNames = setlists.map { it.name }.toSet() }
            .flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    fun loadSetlist(setlistId: String, setlistName: String, presentation: List<String>) {
        this.setlistId = setlistId
        this.setlistName = setlistName

        val setlistSongs: List<SetlistSongItem> = presentation.map { songId ->
            SetlistSongItem(songsRepository.getSong(songId)!!, availableId++)
        }

        _songs.value = setlistSongs.toMutableList()
    }

    fun loadAdhocSetlist(presentation: List<String>) {
        _songs.value = presentation.map { songId ->
            SetlistSongItem(songsRepository.getSong(songId)!!, availableId++)
        }.toMutableList()
    }

    fun loadEditedSetlist(setlistId: String) {
        this.setlistId = setlistId
        editedSetlist = setlistsRepository.getSetlist(setlistId)!!.also { setlist ->
            _songs.value = setlist.presentation
                .map { SetlistSongItem(it, availableId++) }
                .toMutableList()
            setlistName = setlist.name
        }
    }

    suspend fun saveSetlist() {
        val presentation: Array<Song> = _songs.value
            .map { it.song }
            .toTypedArray()

        val setlist = Setlist(setlistId, setlistName, presentation.toList())
        setlistsRepository.upsertSetlist(setlist)
        Log.i(TAG, "Created setlist: $setlist")
    }

    fun hideSelectionCheckboxes() {
        _songs.value.forEach {
            it.hasCheckbox = false
            it.isSelected = false
        }
    }

    fun showSelectionCheckboxes() {
        _songs.value.forEach { it.hasCheckbox = true }
    }

    fun moveSong(from: Int, to: Int) {
        val item = _songs.value.removeAt(from)
        _songs.value.add(to, item)
    }

    fun removeSelectedSongs() {
        _songs.value = _songs.value.filter { !it.isSelected }.toMutableList()
    }

    fun duplicateSelectedSong() {
        val songsAfterDuplicate = _songs.value.toMutableList()

        val selectedItemIndex = songsAfterDuplicate.indexOfFirst { item -> item.isSelected }
        val selectedItem = songsAfterDuplicate[selectedItemIndex].copy(id = availableId++)

        selectedItem.isSelected = false

        songsAfterDuplicate.add(selectedItemIndex + 1, selectedItem)
        _songs.value = songsAfterDuplicate
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

    fun selectSong(songId: Long, selected: Boolean): Boolean {
        val item = _songs.value
            .firstOrNull { it.id == songId } ?: return false

        item.isSelected = selected
        return true
    }

}