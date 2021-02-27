/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 12:51 PM
 */

package pl.gunock.lyriccast.models

class SongItemModel(songMetadataModel: SongMetadataModel) {

    val title: String = songMetadataModel.title
    val author: String = songMetadataModel.author
    val category: String = songMetadataModel.category

    var highlight: Boolean = false
    var isSelected: Boolean = false

    override fun toString(): String {
        return StringBuilder().apply {
            append("(title: $title, ")
            append("author: $author)")
        }.toString()
    }

}