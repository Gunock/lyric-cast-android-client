/*
 * Created by Tomasz Kiljańczyk on 3/8/21 10:21 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/8/21 9:45 PM
 */

package pl.gunock.lyriccast.models

open class Category(
    val name: String,
    val color: Int? = null
) : Comparable<Category> {

    override fun compareTo(other: Category): Int {
        return name.compareTo(other.name)
    }

}
