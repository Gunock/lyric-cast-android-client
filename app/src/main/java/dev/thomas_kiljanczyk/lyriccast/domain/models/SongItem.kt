/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.domain.models

import dev.thomas_kiljanczyk.lyriccast.common.extensions.normalize
import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song

data class SongItem(
    val song: Song,
    var hasCheckbox: Boolean = false,
    var isSelected: Boolean = false
) : Comparable<SongItem> {

    val normalizedTitle: String = song.title.normalize()
    var isHighlighted: Boolean = false

    override fun compareTo(other: SongItem): Int {
        return song.title.compareTo(other.song.title)
    }
}