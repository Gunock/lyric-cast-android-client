/*
 * Created by Tomasz Kiljańczyk on 3/13/21 3:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/13/21 2:33 PM
 */

package pl.gunock.lyriccast.models

class SetlistItem(setlist: Setlist) {
    val id: Long = setlist.id
    val name: String = setlist.name

    var isSelected: Boolean = false

    override fun toString(): String {
        return StringBuilder().apply {
            append("(name: $name)")
        }.toString()
    }
}