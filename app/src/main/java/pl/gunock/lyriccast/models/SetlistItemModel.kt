/*
 * Created by Tomasz Kiljańczyk on 2/26/21 9:36 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/25/21 10:49 PM
 */

package pl.gunock.lyriccast.models

class SetlistItemModel(setlistModel: SetlistModel) {
    val name: String = setlistModel.name
    val category: String = setlistModel.category

    var isSelected: Boolean = false

    override fun toString(): String {
        val builder: StringBuilder = StringBuilder()

        builder.append("(name: $name)")

        return builder.toString()
    }
}