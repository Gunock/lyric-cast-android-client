/*
 * Created by Tomasz Kiljanczyk on 18/07/2021, 12:21
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 18/07/2021, 12:18
 */

package pl.gunock.lyriccast.domain.models

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.common.extensions.normalize
import pl.gunock.lyriccast.datamodel.documents.SetlistDocument

data class SetlistItem(
    val setlist: SetlistDocument
) : Comparable<SetlistItem> {

    val normalizedName by lazy { setlist.name.normalize() }
    val isSelected: MutableLiveData<Boolean> = MutableLiveData(false)

    override fun compareTo(other: SetlistItem): Int {
        return setlist.name.compareTo(other.setlist.name)
    }
}