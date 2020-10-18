/*
 * Created by Tomasz Kiljańczyk on 10/19/20 12:26 AM
 * Copyright (c) 2020 . All rights reserved.
 * Last modified 10/18/20 11:29 PM
 */

package pl.gunock.lyriccast.models

class SongItemModel(position: Int, songMetadataModel: SongMetadataModel) {
    val title: String = songMetadataModel.title
    val author: String = songMetadataModel.author
    val category: String = songMetadataModel.category

    var originalPosition: Int = position
    var isSelected: Boolean = false

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()

        builder.append("(title: $title, ")
        builder.append("author: $author)")

        return builder.toString()
    }

}