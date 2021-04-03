/*
 * Created by Tomasz Kiljanczyk on 4/1/21 8:54 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/1/21 8:52 PM
 */

package pl.gunock.lyriccast.models

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.entities.Category
import pl.gunock.lyriccast.datamodel.entities.Song
import pl.gunock.lyriccast.datamodel.entities.relations.SongAndCategory

data class SongItem(
    val song: Song,
    val category: Category? = null
) : Comparable<SongItem> {

    val normalizedTitle by lazy { song.title.normalize() }
    val highlight: MutableLiveData<Boolean> = MutableLiveData(false)
    var isSelected: Boolean = false

    constructor(songAndCategory: SongAndCategory) : this(
        songAndCategory.song,
        songAndCategory.category
    )

    override fun compareTo(other: SongItem): Int {
        return song.title.compareTo(other.song.title)
    }
}