/*
 * Created by Tomasz Kiljanczyk on 05/10/2021, 19:46
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 05/10/2021, 19:17
 */

package pl.gunock.lyriccast.domain.models

import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.models.Song

data class SongItem(
    val song: Song
) : Comparable<SongItem> {

    var id: Long = 0

    val normalizedTitle: String = song.title.normalize()
    var isHighlighted: Boolean = false
    var isSelected: Boolean = false
    var hasCheckbox: Boolean = false

    override fun compareTo(other: SongItem): Int {
        return song.title.compareTo(other.song.title)
    }
}