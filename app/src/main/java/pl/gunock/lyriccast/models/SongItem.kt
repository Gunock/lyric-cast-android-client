/*
 * Created by Tomasz Kiljanczyk on 4/20/21 1:10 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/20/21 12:11 AM
 */

package pl.gunock.lyriccast.models

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.documents.SongDocument

data class SongItem(
    val song: SongDocument
) : Comparable<SongItem> {

    var id: Long = 0

    val normalizedTitle by lazy { song.title.normalize() }
    val highlight: MutableLiveData<Boolean> = MutableLiveData(false)
    val isSelected: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun compareTo(other: SongItem): Int {
        return song.title.compareTo(other.song.title)
    }
}