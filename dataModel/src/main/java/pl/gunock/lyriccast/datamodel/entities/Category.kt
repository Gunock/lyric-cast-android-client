/*
 * Created by Tomasz Kiljanczyk on 4/4/21 2:00 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/4/21 1:04 AM
 */

package pl.gunock.lyriccast.datamodel.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONObject
import pl.gunock.lyriccast.datamodel.extensions.toNonNullable

@Entity(indices = [Index(value = ["name"], unique = true)])
data class Category(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Long? = null,
    val name: String = "",
    val color: Int? = null
) : Comparable<Category> {
    companion object {
        val ALL_CATEGORY = Category(Long.MIN_VALUE, "All")


    }

    val id: Long get() = categoryId.toNonNullable()

    constructor(json: JSONObject) : this(null, json.getString("name"), json.optInt("color"))

    override fun compareTo(other: Category): Int {
        return name.compareTo(other.name)
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("name", name)
            put("color", color ?: JSONObject.NULL)
        }
    }

}
