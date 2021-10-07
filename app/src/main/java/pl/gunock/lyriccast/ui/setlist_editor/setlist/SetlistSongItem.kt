/*
 * Created by Tomasz Kiljanczyk on 07/10/2021, 11:16
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 07/10/2021, 10:36
 */

package pl.gunock.lyriccast.ui.setlist_editor.setlist

import pl.gunock.lyriccast.datamodel.models.Song

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