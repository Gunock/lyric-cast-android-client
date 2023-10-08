/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 18:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 18:15
 */

package pl.gunock.lyriccast.ui.song_controls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.gunock.lyriccast.R
import pl.gunock.lyriccast.application.Settings
import pl.gunock.lyriccast.application.getCastConfigurationJson
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.CastSessionListener
import javax.inject.Inject

@HiltViewModel
class SongControlsModel @Inject constructor(
    val songsRepository: SongsRepository
) : ViewModel() {
    private companion object {
        private val blankOnColor: Int = R.color.green
        private val blankOffColor: Int = R.color.red

        private val blankOffText: Int = R.string.controls_off
        private val blankOnText: Int = R.string.controls_on
    }

    var songTitle: String = ""

    var settings: Settings? = null

    val currentSlideText: Flow<String> get() = _currentSlideText
    private val _currentSlideText: MutableStateFlow<String> = MutableStateFlow("")

    val currentSlideNumber: Flow<String> get() = _currentSlideNumber
    private val _currentSlideNumber: MutableStateFlow<String> = MutableStateFlow("")

    val currentBlankTextAndColor: Flow<Pair<Int, Int>> get() = _currentBlankTextAndColor
    private val _currentBlankTextAndColor: MutableStateFlow<Pair<Int, Int>> =
        MutableStateFlow(Pair(blankOffText, blankOffColor))

    private var currentSlide = 0
    private lateinit var lyrics: List<String>

    private val castSessionListener: CastSessionListener = CastSessionListener(onStarted = {
        if (settings != null) sendConfiguration()
        sendSlide()
    })

    init {
        val sessionsManager: SessionManager = CastContext.getSharedInstance()!!.sessionManager
        sessionsManager.addSessionManagerListener(castSessionListener)

        CastMessageHelper.isBlanked
            .onEach {
                val currentBlankText: Int = if (it) {
                    blankOffText
                } else {
                    blankOnText
                }

                val currentBlankColor: Int = if (it) {
                    blankOffColor
                } else {
                    blankOnColor
                }

                _currentBlankTextAndColor.value = Pair(currentBlankText, currentBlankColor)
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

    override fun onCleared() {
        CastContext.getSharedInstance()!!.sessionManager
            .removeSessionManagerListener(castSessionListener)

        super.onCleared()
    }

    fun loadSong(songId: String) {
        val song: Song = songsRepository.getSong(songId)!!

        lyrics = song.lyricsList
        songTitle = song.title
        postSlide()
    }

    fun previousSlide() {
        if (currentSlide <= 0) {
            return
        }
        currentSlide--

        sendSlide()
    }

    fun nextSlide() {
        if (currentSlide >= lyrics.size - 1) {
            return
        }
        currentSlide++

        sendSlide()
    }

    fun sendBlank() {
        CastMessageHelper.sendBlank(!CastMessageHelper.isBlanked.value)
    }

    fun sendConfiguration() {
        CastMessageHelper.sendConfiguration(settings!!.getCastConfigurationJson())
    }

    fun sendSlide() {
        CastMessageHelper.sendContentMessage(lyrics[currentSlide])
        postSlide()
    }

    private fun postSlide() {
        _currentSlideNumber.value = "${currentSlide + 1}/${lyrics.size}"
        _currentSlideText.value = lyrics[currentSlide]
    }
}