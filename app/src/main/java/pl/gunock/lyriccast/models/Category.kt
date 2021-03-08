/*
 * Created by Tomasz Kiljańczyk on 3/8/21 11:19 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/8/21 11:18 PM
 */

package pl.gunock.lyriccast.models

open class Category(
    val id: Long,
    val name: String,
    val color: Int? = null
) : Comparable<Category> {

    constructor(id: Long, category: Category) : this(id, category.name, category.color)

    constructor(category: Category) : this(category.id, category.name, category.color)

    constructor(name: String) : this(Long.MIN_VALUE, name)

    override fun compareTo(other: Category): Int {
        return name.compareTo(other.name)
    }

    override fun toString(): String {
        return name
    }

}
