/*
 * Created by Tomasz Kiljańczyk on 3/9/21 1:07 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/9/21 12:03 AM
 */

package pl.gunock.lyriccast.models

import pl.gunock.lyriccast.CategoriesContext

class SongItem(songMetadata: SongMetadata) {

    val id: Long = songMetadata.id
    val title: String = songMetadata.title
    val author: String = songMetadata.author
    val category: Category? = CategoriesContext.getCategory(songMetadata.categoryId)

    var highlight: Boolean = false
    var isSelected: Boolean = false

    override fun toString(): String {
        return StringBuilder().apply {
            append("(title: $title, ")
            append("author: $author)")
        }.toString()
    }

}