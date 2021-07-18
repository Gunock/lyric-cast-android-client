/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 23:28
 */

package pl.gunock.lyriccast.datamodel.repositiories

import io.reactivex.Flowable
import pl.gunock.lyriccast.datamodel.models.Song
import java.io.Closeable

interface SongsRepository : Closeable {

    fun getAllSongs(): Flowable<List<Song>>

    fun getSong(id: String): Song?

    fun upsertSong(song: Song)

    fun deleteSongs(songIds: Collection<String>)

}