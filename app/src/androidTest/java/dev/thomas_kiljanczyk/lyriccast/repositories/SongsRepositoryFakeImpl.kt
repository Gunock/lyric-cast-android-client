/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:35
 */

package dev.thomas_kiljanczyk.lyriccast.repositories

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song
import dev.thomas_kiljanczyk.lyriccast.datamodel.repositiories.SongsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID
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