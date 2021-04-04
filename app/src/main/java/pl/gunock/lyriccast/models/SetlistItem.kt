/*
 * Created by Tomasz Kiljanczyk on 4/3/21 9:09 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/3/21 7:45 PM
 */

package pl.gunock.lyriccast.models

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.entities.Setlist

data class SetlistItem(
    val setlist: Setlist
) : Comparable<SetlistItem> {

    val normalizedName by lazy { setlist.name.normalize() }
    var isSelected: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun compareTo(other: SetlistItem): Int {
        return setlist.name.compareTo(other.setlist.name)
    }
}