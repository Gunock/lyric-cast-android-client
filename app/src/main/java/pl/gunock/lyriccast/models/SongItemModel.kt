/*
 * Created by Tomasz Kiljańczyk on 2/25/21 10:00 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/25/21 9:53 PM
 */

package pl.gunock.lyriccast.models

class SongItemModel(songMetadataModel: SongMetadataModel) {
    val title: String = songMetadataModel.title
    val author: String = songMetadataModel.author
    val category: String = songMetadataModel.category

    var highlight: Boolean = false
    var isSelected: Boolean = false

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()

        builder.append("(title: $title, ")
        builder.append("author: $author)")

        return builder.toString()
    }
}