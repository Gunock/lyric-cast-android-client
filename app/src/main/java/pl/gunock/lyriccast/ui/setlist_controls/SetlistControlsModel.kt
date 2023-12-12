/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 18:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 18:15
 */

package pl.gunock.lyriccast.ui.setlist_controls

import androidx.lifecycle.ViewModel
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.CastSessionListener
import javax.inject.Inject

@HiltViewModel
class SetlistControlsModel @Inject constructor(
    private val setlistsRepository: SetlistsRepository
) : ViewModel() {

    var castConfiguration: JSONObject? = null

    val songs: List<SongItem> get() = _songs
    private val _songs: MutableList<SongItem> = mutableListOf()

    private val _currentSlideText = MutableSharedFlow<String>(1)
    val currentSlideText: Flow<String> get() = _currentSlideText

    private val _currentSlideNumber = MutableStateFlow("")
    val currentSlideNumber: Flow<String> get() = _currentSlideNumber

    private val _currentSongTitle = MutableSharedFlow<String>(1)
    val currentSongTitle: Flow<String> get() = _currentSongTitle

    private val _currentSongPosition = MutableStateFlow(0)
    val currentSongPosition: Flow<Int> get() = _currentSongPosition

    private val _changedSongItems: MutableStateFlow<List<SongItem>> = MutableStateFlow(listOf())
    val changedSongPositions: Flow<List<SongItem>> get() = _changedSongItems


    private var currentLyricsPosition: Int = 0
    private lateinit var currentSongItem: SongItem
    private lateinit var previousSongItem: SongItem
    private val currentSong: Song get() = currentSongItem.song

    private val castSessionListener: CastSessionListener = CastSessionListener(onStarted = {
        if (castConfiguration != null) sendConfiguration()
        sendSlide()
    })

    fun initialize(sessionManager: SessionManager) {
        sessionManager.addSessionManagerListener(castSessionListener)
    }

    override fun onCleared() {
        CastContext.getSharedInstance()!!.sessionManager
            .removeSessionManagerListener(castSessionListener)

        super.onCleared()
    }

    fun loadSetlist(setlistId: String) {
        val setlist: Setlist = setlistsRepository.getSetlist(setlistId)!!

        _songs.clear()

        val songItems = setlist.presentation.map { SongItem(it) }
        _songs.addAll(songItems)

        currentSongItem = songItems.first()
        currentSongItem.isHighlighted = true
        selectSong(0)
    }

    fun previousSlide() {
        val isFirstLyricsPage = currentLyricsPosition <= 0
        if (isFirstLyricsPage && _currentSongPosition.value > 0) {
            selectSong(_currentSongPosition.value - 1)
        } else if (!isFirstLyricsPage) {
            currentLyricsPosition--
            sendSlide()
        }
    }

    fun nextSlide() {
        val isLastLyricsPage = currentLyricsPosition >= currentSong.lyricsList.size - 1
        if (isLastLyricsPage && _currentSongPosition.value < songs.size - 1) {
            selectSong(_currentSongPosition.value + 1)
        } else if (!isLastLyricsPage) {
            currentLyricsPosition++
            sendSlide()
        }
    }

    fun sendBlank() {
        CastMessageHelper.sendBlank(!CastMessageHelper.isBlanked.value)
    }

    fun sendConfiguration() {
        CastMessageHelper.sendConfiguration(castConfiguration!!)
    }

    fun selectSong(position: Int, fromStart: Boolean = false) {
        previousSongItem = currentSongItem
        currentSongItem = _songs[position]
        currentLyricsPosition =
            if (fromStart) 0
            else if (position >= _songs.indexOf(previousSongItem)) 0
            else currentSongItem.song.lyricsList.size - 1
        _currentSongPosition.tryEmit(position)

        sendSlide()
    }

    fun sendSlide() {
        val lyricsText = currentSong.lyricsList[currentLyricsPosition]
        CastMessageHelper.sendContentMessage(lyricsText)
        _currentSlideText.tryEmit(lyricsText)
        _currentSlideNumber.tryEmit("${currentLyricsPosition + 1}/${currentSong.lyricsList.size}")

        val isNewSongTitle = previousSongItem != currentSongItem
        if (isNewSongTitle) {
            previousSongItem.isHighlighted = false
            currentSongItem.isHighlighted = true

            _changedSongItems.value = listOf(previousSongItem.copy(), currentSongItem.copy())
            _currentSongTitle.tryEmit(currentSong.title)
        }
    }

}