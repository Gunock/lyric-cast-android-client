/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 17:30
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 15:42
 */

package pl.gunock.lyriccast.ui.setlist_controls

import androidx.lifecycle.ViewModel
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.application.Settings
import pl.gunock.lyriccast.application.getCastConfigurationJson
import pl.gunock.lyriccast.datamodel.models.Setlist
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SetlistsRepository
import pl.gunock.lyriccast.domain.models.SongItem
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.SessionStartedListener
import javax.inject.Inject

@HiltViewModel
class SetlistControlsModel @Inject constructor(
    private val setlistsRepository: SetlistsRepository
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

    // TODO: Try to remove this field
    var settings: Settings? = null

    val songs: List<SongItem> get() = _songs
    private val _songs: MutableList<SongItem> = mutableListOf()

    val currentSlideText: StateFlow<String> get() = _currentSlideText
    private val _currentSlideText: MutableStateFlow<String> = MutableStateFlow("")

    val currentSongTitle: StateFlow<String> get() = _currentSongTitle
    private val _currentSongTitle: MutableStateFlow<String> = MutableStateFlow("")

    val currentSongPosition: StateFlow<Int> get() = _currentSongPosition
    private val _currentSongPosition: MutableStateFlow<Int> = MutableStateFlow(0)

    val changedSongPositions: StateFlow<List<Int>> get() = _changedSongPositions
    private val _changedSongPositions: MutableStateFlow<List<Int>> = MutableStateFlow(listOf(0))

    val currentBlankTextAndColor: StateFlow<Pair<Int, Int>> get() = _currentBlankTextAndColor
    private val _currentBlankTextAndColor: MutableStateFlow<Pair<Int, Int>> =
        MutableStateFlow(Pair(currentBlankText, currentBlankColor))


    private lateinit var setlistLyrics: List<String>

    private var songTitles: MutableMap<Int, String> = mutableMapOf()

    private var songStartPoints: MutableMap<String, Int> = mutableMapOf()

    private var currentLyricsPosition: Int = 0

    private var previousSongTitle: String = ""

    private val sessionStartedListener: SessionStartedListener = SessionStartedListener {
        postBlankColor()
        if (settings != null) {
            sendConfiguration()
        }
        sendSlide()
    }

    init {
        val sessionsManager: SessionManager = CastContext.getSharedInstance()!!.sessionManager
        sessionsManager.addSessionManagerListener(sessionStartedListener)
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
        val itemPosition = highlightSong(previousSongTitle, isHighlighted = true, isCurrent = true)
        _changedSongPositions.value = listOf(itemPosition)

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
        postBlankColor()
    }

    fun sendConfiguration() {
        CastMessageHelper.sendConfiguration(settings!!.getCastConfigurationJson())
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
        val isNewSongTitle = songTitles.containsKey(currentLyricsPosition)
                && songTitles[currentLyricsPosition]!! != previousSongTitle

        if (isNewSongTitle) {
            val songTitle = songTitles[currentLyricsPosition]!!
            val songTitleWithoutNumber = songTitle.replace("^\\[[0-9]+] ".toRegex(), "")

            val itemPosition1 = highlightSong(previousSongTitle, isHighlighted = false)
            val itemPosition2 = highlightSong(songTitle, isHighlighted = true, isCurrent = true)

            _changedSongPositions.value = listOf(itemPosition1, itemPosition2)
            previousSongTitle = songTitle

            _currentSongTitle.value = songTitleWithoutNumber
        }

        _currentSlideText.value = setlistLyrics[currentLyricsPosition]
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
            _currentSongPosition.value = songItemPosition
        }

        return songItemPosition
    }

    private fun postBlankColor() {
        val textAndColor = Pair(currentBlankText, currentBlankColor)
        _currentBlankTextAndColor.value = textAndColor
    }

}