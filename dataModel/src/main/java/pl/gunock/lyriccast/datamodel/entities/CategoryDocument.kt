/*
 * Created by Tomasz Kiljanczyk on 4/19/21 5:12 PM
 * Copyright (c) 2021 . All rights reserved.
 * Last modified 4/19/21 4:41 PM
 */

package pl.gunock.lyriccast.datamodel.entities

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey
import org.bson.types.ObjectId
import pl.gunock.lyriccast.datatransfer.models.CategoryDto

open class CategoryDocument(
    var name: String,
    var color: Int? = null,
    @field:PrimaryKey
    var id: ObjectId = ObjectId()
) : RealmObject(), Comparable<CategoryDocument> {

    @Ignore
    val idLong: Long = id.date.time

    constructor() : this("", null, ObjectId())

    fun toDto(): CategoryDto {
        return CategoryDto(name, color)
    }

    override fun compareTo(other: CategoryDocument): Int {
        return name.compareTo(other.name)
    }

}