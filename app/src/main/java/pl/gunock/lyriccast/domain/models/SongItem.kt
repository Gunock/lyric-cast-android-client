/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 23:43
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 14:32
 */

package pl.gunock.lyriccast.domain.models

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.models.Song

data class SongItem(
    val song: Song
) : Comparable<SongItem> {

    var id: Long = 0

    val normalizedTitle by lazy { song.title.normalize() }
    val highlight: MutableLiveData<Boolean> = MutableLiveData(false)
    val isSelected: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun compareTo(other: SongItem): Int {
        return song.title.compareTo(other.song.title)
    }
}