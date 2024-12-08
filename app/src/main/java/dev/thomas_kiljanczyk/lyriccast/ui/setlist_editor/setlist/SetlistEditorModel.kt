/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.setlist_editor.setlist

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Setlist
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SetlistsRepository
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import dev.thomas_kiljanczyk.lyriccast.shared.enums.NameValidationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SetlistEditorModel @Inject constructor(
    private val songsRepository: SongsRepository,
    private val setlistsRepository: SetlistsRepository
) : ViewModel() {

    private companion object {
        const val TAG = "SetlistEditorModel"
    }

    private var _songsFlow: MutableStateFlow<MutableList<SetlistSongItem>> =
        MutableStateFlow(mutableListOf())
    val songsFlow: StateFlow<List<SetlistSongItem>> = _songsFlow

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

        _songsFlow.value = setlistSongs.toMutableList()
    }

    fun loadAdhocSetlist(presentation: List<String>) {
        _songsFlow.value = presentation.map { songId ->
            SetlistSongItem(songsRepository.getSong(songId)!!, availableId++)
        }.toMutableList()
    }

    fun loadEditedSetlist(setlistId: String) {
        this.setlistId = setlistId
        editedSetlist = setlistsRepository.getSetlist(setlistId)!!.also { setlist ->
            _songsFlow.value = setlist.presentation
                .map { SetlistSongItem(it, availableId++) }
                .toMutableList()
            setlistName = setlist.name
        }
    }

    suspend fun saveSetlist() {
        val presentation: Array<Song> = _songsFlow.value
            .map { it.song }
            .toTypedArray()

        val setlist = Setlist(setlistId, setlistName, presentation.toList())
        setlistsRepository.upsertSetlist(setlist)
        Log.i(TAG, "Created setlist: $setlist")
    }

    fun hideSelectionCheckboxes() {
        _songsFlow.value.forEach {
            it.hasCheckbox = false
            it.isSelected = false
        }
    }

    fun showSelectionCheckboxes() {
        _songsFlow.value.forEach { it.hasCheckbox = true }
    }

    fun moveSong(from: Int, to: Int) {
        val item = _songsFlow.value.removeAt(from)
        _songsFlow.value.add(to, item)
    }

    fun removeSelectedSongs() {
        _songsFlow.value = _songsFlow.value.filter { !it.isSelected }.toMutableList()
    }

    fun duplicateSelectedSong() {
        val songsAfterDuplicate = _songsFlow.value.toMutableList()

        val selectedItemIndex = songsAfterDuplicate.indexOfFirst { item -> item.isSelected }
        val selectedItem = songsAfterDuplicate[selectedItemIndex].copy(id = availableId++)

        selectedItem.isSelected = false

        songsAfterDuplicate.add(selectedItemIndex + 1, selectedItem)
        _songsFlow.value = songsAfterDuplicate
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
        val item = _songsFlow.value
            .firstOrNull { it.id == songId } ?: return false

        item.isSelected = selected
        return true
    }

}