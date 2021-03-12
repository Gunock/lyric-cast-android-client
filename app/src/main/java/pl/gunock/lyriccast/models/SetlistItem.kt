/*
 * Created by Tomasz Kiljańczyk on 3/12/21 4:03 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/12/21 1:24 PM
 */

package pl.gunock.lyriccast.models

class SetlistItem(setlist: Setlist) {

    val name: String = setlist.name

    var isSelected: Boolean = false

    override fun toString(): String {
        return StringBuilder().apply {
            append("(name: $name)")
        }.toString()
    }
}