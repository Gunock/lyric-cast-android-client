/*
 * Created by Tomasz Kiljańczyk on 3/17/21 12:00 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/16/21 11:01 PM
 */

package pl.gunock.lyriccast.models

import androidx.lifecycle.MutableLiveData
import pl.gunock.lyriccast.CategoriesContext

class SongItem(songMetadata: SongMetadata) {

    val id: Long = songMetadata.id
    val title: String = songMetadata.title
    val category: Category? = CategoriesContext.getCategory(songMetadata.categoryId)

    val highlight: MutableLiveData<Boolean> = MutableLiveData(false)
    var isSelected: Boolean = false

    override fun toString(): String {
        return StringBuilder().apply {
            append("(title: $title)")
        }.toString()
    }

}