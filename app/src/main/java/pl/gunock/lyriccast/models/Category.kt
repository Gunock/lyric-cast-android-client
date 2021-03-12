/*
 * Created by Tomasz Kiljańczyk on 3/12/21 4:03 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 3/12/21 12:00 AM
 */

package pl.gunock.lyriccast.models

import org.json.JSONObject
import pl.gunock.lyriccast.extensions.getIntOrNull

open class Category(
    val id: Long,
    val name: String,
    val color: Int? = null
) : Comparable<Category> {

    constructor(id: Long, category: Category) : this(id, category.name, category.color)

    constructor(category: Category) : this(category.id, category.name, category.color)

    constructor(name: String) : this(Long.MIN_VALUE, name)

    constructor(json: JSONObject) : this(
        json.getLong("id"),
        json.getString("name"),
        json.getIntOrNull("color")
    )

    override fun equals(other: Any?): Boolean {
        if (other == null || other.javaClass != Category::javaClass) {
            return false
        }
        val otherCategory = other as Category
        return id == otherCategory.id
    }

    override fun compareTo(other: Category): Int {
        return name.compareTo(other.name)
    }

    override fun toString(): String {
        return name
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("color", color ?: JSONObject.NULL)
        }
    }

}
