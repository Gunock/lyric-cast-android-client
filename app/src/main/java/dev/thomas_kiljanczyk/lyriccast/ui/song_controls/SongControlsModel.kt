/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.ui.song_controls

import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.thomas_kiljanczyk.lyriccast.application.AppSettings
import dev.thomas_kiljanczyk.lyriccast.application.getCastConfigurationJson
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastMessageHelper
import dev.thomas_kiljanczyk.lyriccast.shared.cast.CastSessionListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class SongControlsModel @Inject constructor(
    dataStore: DataStore<AppSettings>,
    val songsRepository: SongsRepository
) : ViewModel() {
    var songTitle: String = ""

    private var castConfiguration: JSONObject? = null

    val currentSlideText: Flow<String> get() = _currentSlideText
    private val _currentSlideText = MutableStateFlow("")

    val currentSlideNumber: Flow<String> get() = _currentSlideNumber
    private val _currentSlideNumber = MutableStateFlow("")

    private var currentSlide = 0
    private lateinit var lyrics: List<String>

    private val castSessionListener: CastSessionListener = CastSessionListener(onStarted = {
        if (castConfiguration != null) sendConfiguration()
        sendSlide()
    })

    init {
        dataStore.data
            .onEach { settings ->
                castConfiguration = settings.getCastConfigurationJson()
                sendConfiguration()
            }.flowOn(Dispatchers.Default)
            .launchIn(viewModelScope)
    }

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

    private fun sendConfiguration() {
        CastMessageHelper.sendConfiguration(castConfiguration!!)
    }

    fun sendSlide() {
        CastMessageHelper.sendContentMessage(lyrics[currentSlide])
        postSlide()
    }

    private fun postSlide() {
        _currentSlideNumber.tryEmit("${currentSlide + 1}/${lyrics.size}")
        _currentSlideText.tryEmit(lyrics[currentSlide])
    }
}