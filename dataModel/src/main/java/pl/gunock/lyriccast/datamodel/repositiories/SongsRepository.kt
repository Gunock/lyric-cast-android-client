/*
 * Created by Tomasz Kiljanczyk on 31/12/2021, 13:15
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 31/12/2021, 13:05
 */

package pl.gunock.lyriccast.datamodel.repositiories

import kotlinx.coroutines.flow.Flow
import pl.gunock.lyriccast.datamodel.models.Song

interface SongsRepository {

    fun getAllSongs(): Flow<List<Song>>

    fun getSong(id: String): Song?

    suspend fun upsertSong(song: Song)

    suspend fun deleteSongs(songIds: Collection<String>)

}