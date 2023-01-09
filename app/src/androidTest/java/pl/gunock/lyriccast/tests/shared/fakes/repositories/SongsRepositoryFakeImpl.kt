/*
 * Created by Tomasz Kiljanczyk on 08/01/2023, 23:51
 * Copyright (c) 2023 . All rights reserved.
 * Last modified 08/01/2023, 23:51
 */

package pl.gunock.lyriccast.tests.shared.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pl.gunock.lyriccast.datamodel.models.Song
import pl.gunock.lyriccast.datamodel.repositiories.SongsRepository
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
        songs += song
        songFlow.emit(songs.toList())
    }

    override suspend fun deleteSongs(songIds: Collection<String>) {
        songs.removeIf { it.id in songIds }
        songFlow.emit(songs.toList())
    }
}