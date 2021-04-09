/*
 * Created by Tomasz Kiljanczyk on 4/9/21 11:51 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/9/21 11:03 PM
 */

package pl.gunock.lyriccast.models

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.relations.SongAndCategory

data class SongItem(
    val song: Song,
    val category: Category? = null,
    var id: Long = song.id
) : Comparable<SongItem> {

    val normalizedTitle by lazy { song.title.normalize() }
    val highlight: MutableLiveData<Boolean> = MutableLiveData(false)
    val isSelected: MutableLiveData<Boolean> = MutableLiveData(false)

    constructor(songAndCategory: SongAndCategory) : this(
        songAndCategory.song,
        songAndCategory.category
    )

    override fun compareTo(other: SongItem): Int {
        return song.title.compareTo(other.song.title)
    }
}