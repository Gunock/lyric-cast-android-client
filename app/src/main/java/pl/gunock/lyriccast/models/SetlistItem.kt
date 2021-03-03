/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:02 PM
 */

package pl.gunock.lyriccast.models

class SetlistItem(setlist: Setlist) {

    val name: String = setlist.name
    val category: String = setlist.category

    var isSelected: Boolean = false

    override fun toString(): String {
        return StringBuilder().apply {
            append("(name: $name)")
        }.toString()
    }
}