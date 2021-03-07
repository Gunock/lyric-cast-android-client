/*
 * Created by Tomasz Kiljańczyk on 3/7/21 11:44 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/7/21 11:16 PM
 */

package pl.gunock.lyriccast.models

open class Category(
    val name: String,
    val color: String = ""
) : Comparable<Category> {

    override fun compareTo(other: Category): Int {
        return name.compareTo(other.name)
    }

}
