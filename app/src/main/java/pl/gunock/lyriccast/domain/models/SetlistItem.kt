/*
 * Created by Tomasz Kiljanczyk on 04/10/2021, 19:31
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 04/10/2021, 18:41
 */

package pl.gunock.lyriccast.domain.models

import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.models.Setlist

data class SetlistItem(
    val setlist: Setlist
) : Comparable<SetlistItem> {

    val normalizedName = setlist.name.normalize()
    var isSelected: Boolean = false
    var hasCheckbox: Boolean = false

    override fun compareTo(other: SetlistItem): Int {
        return setlist.name.compareTo(other.setlist.name)
    }
}