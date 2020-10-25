/*
 * Created by Tomasz Kiljańczyk on 10/25/20 10:05 PM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/25/20 9:39 PM
 */

package pl.gunock.lyriccast.models

class SongItemModel(songMetadataModel: SongMetadataModel) {
    val title: String = songMetadataModel.title
    val author: String = songMetadataModel.author
    val category: String = songMetadataModel.category

    var highlight: Boolean = false
    var selected: Boolean = false

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()

        builder.append("(title: $title, ")
        builder.append("author: $author)")

        return builder.toString()
    }

}