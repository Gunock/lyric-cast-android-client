/*
 * Created by Tomasz Kiljańczyk on 2/27/21 4:17 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 2/27/21 12:51 PM
 */

package pl.gunock.lyriccast.models

class SetlistItemModel(setlistModel: SetlistModel) {

    val name: String = setlistModel.name
    val category: String = setlistModel.category

    var isSelected: Boolean = false

    override fun toString(): String {
        return StringBuilder().apply {
            append("(name: $name)")
        }.toString()
    }
}