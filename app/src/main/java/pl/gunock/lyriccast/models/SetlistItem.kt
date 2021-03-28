/*
 * Created by Tomasz Kiljańczyk on 3/28/21 3:19 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/28/21 12:22 AM
 */

package pl.gunock.lyriccast.models

import pl.gunock.lyriccast.datamodel.entities.Setlist
import pl.gunock.lyriccast.extensions.normalize

data class SetlistItem(
    val setlist: Setlist
) : Comparable<Setlist> {

    val normalizedName by lazy { setlist.name.normalize() }
    var isSelected: Boolean = false

    override fun compareTo(other: Setlist): Int {
        if (setlist.setlistId == other.setlistId) {
            return 0
        }

        return setlist.name.compareTo(other.name)
    }
}