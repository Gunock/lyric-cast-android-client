/*
 * Created by Tomasz Kiljańczyk on 3/3/21 11:07 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/3/21 11:02 PM
 */

package pl.gunock.lyriccast.models

class SongItem(songMetadata: SongMetadata) {

    val title: String = songMetadata.title
    val author: String = songMetadata.author
    val category: String? = songMetadata.category

    var highlight: Boolean = false
    var isSelected: Boolean = false

    override fun toString(): String {
        return StringBuilder().apply {
            append("(title: $title, ")
            append("author: $author)")
        }.toString()
    }

}