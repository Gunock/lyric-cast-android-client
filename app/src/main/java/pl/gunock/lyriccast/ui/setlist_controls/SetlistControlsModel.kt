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

    private val _currentSlideText: MutableSharedFlow<String> = MutableSharedFlow(1)
    val currentSlideText: Flow<String> get() = _currentSlideText

    private val _currentSongTitle: MutableSharedFlow<String> = MutableSharedFlow(1)
    val currentSongTitle: Flow<String> get() = _currentSongTitle

    private val _currentSongPosition: MutableSharedFlow<Int> = MutableSharedFlow(1)
    val currentSongPosition: Flow<Int> get() = _currentSongPosition

    private val _changedSongPositions: MutableStateFlow<List<Int>> = MutableStateFlow(listOf())
    val changedSongPositions: Flow<List<Int>> get() = _changedSongPositions


    private lateinit var setlistLyrics: List<String>

    private var songTitles: MutableMap<Int, String> = mutableMapOf()

    private var songStartPoints: MutableMap<String, Int> = mutableMapOf()

    private var currentLyricsPosition: Int = 0

    private var previousSongTitle: String = ""

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

        var setlistLyricsIndex = 0
        setlistLyrics = setlist.presentation
            .flatMapIndexed { index: Int, song: Song ->
                val lyrics = song.lyricsList

                val indexedTitle = "[$index] ${song.title}"
                songTitles[setlistLyricsIndex] = indexedTitle
                songTitles[setlistLyricsIndex + lyrics.size - 1] = indexedTitle
                songStartPoints[indexedTitle] = setlistLyricsIndex

                setlistLyricsIndex += lyrics.size

                return@flatMapIndexed lyrics
            }

        _songs.clear()

        val songItems = setlist.presentation.map { SongItem(it) }
        _songs.addAll(songItems)

        currentLyricsPosition = 0
        previousSongTitle = songTitles[currentLyricsPosition]!!
        val itemPosition = highlightSong(previousSongTitle, isHighlighted = true, isCurrent = true)
        _changedSongPositions.value = listOf(itemPosition)

        changeTitle()
        _currentSlideText.tryEmit(setlistLyrics[currentLyricsPosition])
    }

    fun previousSlide() {
        if (currentLyricsPosition <= 0) {
            return
        }
        currentLyricsPosition--

        sendSlide()
    }

    fun nextSlide() {
        if (currentLyricsPosition >= setlistLyrics.size - 1) {
            return
        }
        currentLyricsPosition++

        sendSlide()
    }

    fun sendBlank() {
        CastMessageHelper.sendBlank(!CastMessageHelper.isBlanked.value)
    }

    fun sendConfiguration() {
        CastMessageHelper.sendConfiguration(castConfiguration!!)
    }

    fun selectSong(position: Int) {
        val title = _songs[position].song.title
        val indexedTitle = "[$position] $title"
        currentLyricsPosition = songStartPoints[indexedTitle]!!

        sendSlide()
    }

    fun sendSlide() {
        CastMessageHelper.sendContentMessage(setlistLyrics[currentLyricsPosition])

        val isNewSongTitle = songTitles.containsKey(currentLyricsPosition)
                && songTitles[currentLyricsPosition]!! != previousSongTitle

        if (isNewSongTitle) {
            changeTitle()
        }

        _currentSlideText.tryEmit(setlistLyrics[currentLyricsPosition])
    }

    private fun changeTitle() {
        val songTitle = songTitles[currentLyricsPosition]!!
        val songTitleWithoutNumber = songTitle.replace("^\\[[0-9]+] ".toRegex(), "")

        val itemPosition1 = highlightSong(previousSongTitle, isHighlighted = false)
        val itemPosition2 = highlightSong(songTitle, isHighlighted = true, isCurrent = true)

        _changedSongPositions.value = listOf(itemPosition1, itemPosition2)
        previousSongTitle = songTitle

        _currentSongTitle.tryEmit(songTitleWithoutNumber)
    }

    private fun highlightSong(
        title: String,
        isHighlighted: Boolean,
        isCurrent: Boolean = false
    ): Int {
        if (title.isBlank()) {
            throw IllegalArgumentException("Song title cannot be blank")
        }

        val songItemPosition: Int = songStartPoints.keys
            .indexOfFirst { songTitle -> songTitle == title }

        _songs[songItemPosition].isHighlighted = isHighlighted
        if (isCurrent) {
            _currentSongPosition.tryEmit(songItemPosition)
        }

        return songItemPosition
    }

}