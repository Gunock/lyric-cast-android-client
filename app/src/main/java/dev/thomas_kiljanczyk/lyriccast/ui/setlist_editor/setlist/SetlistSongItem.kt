/*
 * Created by Tomasz Kiljanczyk on 08/12/2024, 21:35
 * Copyright (c) 2024 . All rights reserved.
 * Last modified 08/12/2024, 21:07
 */

package dev.thomas_kiljanczyk.lyriccast.ui.setlist_editor.setlist

import dev.thomas_kiljanczyk.lyriccast.datamodel.models.Song

data class SetlistSongItem(
    val song: Song,
    val id: Long = 0
) : Comparable<SetlistSongItem> {

    var isSelected: Boolean = false
    var hasCheckbox: Boolean = false

    override fun compareTo(other: SetlistSongItem): Int {
        return song.title.compareTo(other.song.title)
    }
}