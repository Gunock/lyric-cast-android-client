/*
 * Created by Tomasz Kiljanczyk on 10/01/2023, 21:34
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 10/01/2023, 21:31
 */

package pl.gunock.lyriccast.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
import java.util.*
import javax.inject.Inject

class SongsRepositoryFakeImpl @Inject constructor() : SongsRepository {
    private val songs = mutableListOf<Song>()
    private val songFlow = MutableStateFlow(songs.toList())


    override fun getAllSongs(): Flow<List<Song>> {
        return songFlow
    }

    override fun getSong(id: String): Song? {
        return songs.firstOrNull { it.id == id }
    }

    override suspend fun upsertSong(song: Song) {
        val existingSong = songs.find { it.id == song.id }
        if (existingSong != null) {
            songs.remove(existingSong)
        } else {
            song.id = UUID.randomUUID().toString()
        }

        songs += song
        songFlow.emit(songs.toList())
    }

    override suspend fun deleteSongs(songIds: Collection<String>) {
        songs.removeIf { it.id in songIds }
        songFlow.emit(songs.toList())
    }
}