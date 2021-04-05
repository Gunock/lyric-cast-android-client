/*
 * Created by Tomasz Kiljanczyk on 4/5/21 5:14 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 5:13 PM
 */

package pl.gunock.lyriccast.models

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.entities.Setlist

data class SetlistItem(
    val setlist: Setlist
) : Comparable<SetlistItem> {

    val normalizedName by lazy { setlist.name.normalize() }
    val isSelected: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun compareTo(other: SetlistItem): Int {
        return setlist.name.compareTo(other.setlist.name)
    }
}