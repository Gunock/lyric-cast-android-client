/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 18:29
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 18:01
 */

package pl.gunock.lyriccast.datamodel.repositiories

import io.reactivex.Flowable
import pl.gunock.lyriccast.datamodel.models.Song

interface SongsRepository {

    fun getAllSongs(): Flowable<List<Song>>

    fun getSong(id: String): Song?

    suspend fun upsertSong(song: Song)

    suspend fun deleteSongs(songIds: Collection<String>)

}