/*
 * Created by Tomasz Kiljańczyk on 3/13/21 3:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/12/21 9:28 PM
 */

package pl.gunock.lyriccast.models

import pl.gunock.lyriccast.CategoriesContext

class SongItem(songMetadata: SongMetadata) {

    val id: Long = songMetadata.id
    val title: String = songMetadata.title
    val category: Category? = CategoriesContext.getCategory(songMetadata.categoryId)

    var highlight: Boolean = false
    var isSelected: Boolean = false

    override fun toString(): String {
        return StringBuilder().apply {
            append("(title: $title)")
        }.toString()
    }

}