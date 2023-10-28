/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 18:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 18:15
 */

package pl.gunock.lyriccast.ui.song_controls

import androidx.lifecycle.ViewModel
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import pl.gunock.lyriccast.shared.cast.CastMessageHelper
import pl.gunock.lyriccast.shared.cast.CastSessionListener
import javax.inject.Inject

@HiltViewModel
class SongControlsModel @Inject constructor(
    val songsRepository: SongsRepository
) : ViewModel() {
    var songTitle: String = ""

    var castConfiguration: JSONObject? = null

    val currentSlideText: Flow<String> get() = _currentSlideText
    private val _currentSlideText: MutableStateFlow<String> = MutableStateFlow("")

    val currentSlideNumber: Flow<String> get() = _currentSlideNumber
    private val _currentSlideNumber: MutableStateFlow<String> = MutableStateFlow("")

    private var currentSlide = 0
    private lateinit var lyrics: List<String>

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
        CastMessageHelper.sendConfiguration(castConfiguration!!)
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