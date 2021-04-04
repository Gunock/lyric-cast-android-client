/*
 * Created by Tomasz Kiljanczyk on 4/5/21 12:07 AM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/5/21 12:07 AM
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
        val ALL = Category(Long.MIN_VALUE, "All")
        val NONE = Category(Long.MIN_VALUE, "None")
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
