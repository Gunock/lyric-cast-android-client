/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 19:46
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 19:44
 */

package pl.gunock.lyriccast.ui.setlist_controls

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.SessionStartedListener
import javax.inject.Inject

@HiltViewModel
class SetlistControlsViewModel @Inject constructor(
    val setlistsRepository: SetlistsRepository
) : ViewModel() {
    private companion object {
        private const val blankOnColor: Int = R.color.green
        private const val blankOffColor: Int = R.color.red
        private val currentBlankColor: Int
            get() = if (CastMessageHelper.isBlanked) {
                blankOffColor
            } else {
                blankOnColor
            }

        private const val blankOffText: Int = R.string.controls_off
        private const val blankOnText: Int = R.string.controls_on
        private val currentBlankText: Int
            get() = if (CastMessageHelper.isBlanked) {
                blankOffText
            } else {
                blankOnText
            }
    }

    val songs: List<SongItem> get() = _songs
    private val _songs: MutableList<SongItem> = mutableListOf()

    val currentSlideText: LiveData<String> get() = _currentSlideText
    private val _currentSlideText: MutableLiveData<String> = MutableLiveData("")

    val currentSongTitle: LiveData<String> get() = _currentSongTitle
    private val _currentSongTitle: MutableLiveData<String> = MutableLiveData("")

    val currentSongPosition: LiveData<Int> get() = _currentSongPosition
    private val _currentSongPosition: MutableLiveData<Int> = MutableLiveData(0)

    val changedSongPosition: LiveData<Int> get() = _changedSongPosition
    private val _changedSongPosition: MutableLiveData<Int> = MutableLiveData(0)

    val currentBlankTextAndColor: LiveData<Pair<Int, Int>> get() = _currentBlankTextAndColor
    private val _currentBlankTextAndColor: MutableLiveData<Pair<Int, Int>> =
        MutableLiveData(Pair(currentBlankText, currentBlankColor))


    private lateinit var setlistLyrics: List<String>

    private var songTitles: MutableMap<Int, String> = mutableMapOf()

    private var songStartPoints: MutableMap<String, Int> = mutableMapOf()

    private var currentLyricsPosition: Int = 0

    private var previousSongTitle: String = ""

    private val sessionStartedListener: SessionStartedListener

    init {
        sessionStartedListener = SessionStartedListener {
            sendConfiguration()
            sendSlide()
        }

        val sessionsManager: SessionManager = CastContext.getSharedInstance()!!.sessionManager
        sessionsManager.addSessionManagerListener(sessionStartedListener)

        if (sessionsManager.currentSession?.isConnected == true) {
            sendConfiguration()
            sendSlide()
        }
    }

    override fun onCleared() {
        CastContext.getSharedInstance()!!.sessionManager
            .removeSessionManagerListener(sessionStartedListener)

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
        highlightSong(previousSongTitle, isHighlighted = true, isCurrent = true)

        postSlide()
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
        CastMessageHelper.sendBlank(!CastMessageHelper.isBlanked)

        val textAndColor = Pair(
            currentBlankText,
            currentBlankColor
        )
        _currentBlankTextAndColor.postValue(textAndColor)
    }

    fun sendConfiguration() {
        CastMessageHelper.sendConfiguration()
    }

    fun selectSong(position: Int) {
        val title = _songs[position].song.title
        val indexedTitle = "[$position] $title"
        currentLyricsPosition = songStartPoints[indexedTitle]!!

        sendSlide()
    }

    fun sendSlide() {
        CastMessageHelper.sendContentMessage(setlistLyrics[currentLyricsPosition])
        postSlide()
    }

    private fun postSlide() {
        if (
            songTitles.containsKey(currentLyricsPosition)
            && songTitles[currentLyricsPosition]!! != previousSongTitle
        ) {
            val songTitle = songTitles[currentLyricsPosition]!!
            val songTitleWithoutNumber = songTitle.replace("^\\[[0-9]+] ".toRegex(), "")

            highlightSong(previousSongTitle, isHighlighted = false)
            highlightSong(songTitle, isHighlighted = true, isCurrent = true)
            previousSongTitle = songTitle

            _currentSongTitle.postValue(songTitleWithoutNumber)
        }

        _currentSlideText.postValue(setlistLyrics[currentLyricsPosition])
    }

    private fun highlightSong(title: String, isHighlighted: Boolean, isCurrent: Boolean = false) {
        if (title.isBlank()) {
            return
        }

        val songItemPosition: Int = songStartPoints.keys
            .indexOfFirst { songTitle -> songTitle == title }

        _songs[songItemPosition].isHighlighted = isHighlighted
        _changedSongPosition.value = songItemPosition
        if (isCurrent) {
            _currentSongPosition.postValue(songItemPosition)
        }
    }

}