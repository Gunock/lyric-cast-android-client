/*
 * Created by Tomasz Kiljanczyk on 19/07/2021, 00:22
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 19/07/2021, 00:22
 */

package pl.gunock.lyriccast.datamodel.repositiories

import io.reactivex.Flowable
import pl.gunock.lyriccast.datamodel.models.Song

interface SongsRepository {

    fun getAllSongs(): Flowable<List<Song>>

    fun getSong(id: String): Song?

    fun upsertSong(song: Song)

    fun deleteSongs(songIds: Collection<String>)

}